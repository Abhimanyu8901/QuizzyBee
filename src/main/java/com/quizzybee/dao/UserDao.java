package com.quizzybee.dao;

import com.quizzybee.database.DatabaseConnection;
import com.quizzybee.model.User;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class UserDao {
    private static final int REGISTRATION_MIN = 1;
    private static final int REGISTRATION_MAX = 50000;
    private static final Random RANDOM = new Random();
    private static volatile boolean userSchemaChecked;

    public boolean registerUser(User user) throws SQLException {
        ensureUserSchema();
        String sql = "INSERT INTO users(registration_number, name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int registrationNumber = generateUniqueRegistrationNumber(connection);
            statement.setInt(1, registrationNumber);
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getRole());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                user.setRegistrationNumber(registrationNumber);
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }
            }
            return rowsAffected > 0;
        }
    }

    public Optional<User> login(String email, String password, String role) throws SQLException {
        ensureUserSchema();
        String sql = "SELECT user_id, registration_number, name, email, password, role, profile_image_path FROM users WHERE email = ? AND password = ? AND role = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, password);
            statement.setString(3, role);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(
                            resultSet.getInt("user_id"),
                            resultSet.getInt("registration_number"),
                            resultSet.getString("name"),
                            resultSet.getString("email"),
                            resultSet.getString("password"),
                            resultSet.getString("role"),
                            resultSet.getString("profile_image_path")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public boolean existsByEmail(String email) throws SQLException {
        ensureUserSchema();
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean updateProfile(int userId, String newName, String newPassword) throws SQLException {
        ensureUserSchema();
        String sql = "UPDATE users SET name = ?, password = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newName);
            statement.setString(2, newPassword);
            statement.setInt(3, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateProfileImage(int userId, String profileImagePath) throws SQLException {
        ensureUserSchema();
        String sql = "UPDATE users SET profile_image_path = ? WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profileImagePath);
            statement.setInt(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    public int deleteUsersByIds(java.util.List<Integer> userIds) throws SQLException {
        ensureUserSchema();
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer userId : userIds) {
                statement.setInt(1, userId);
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            int deleted = 0;
            for (int value : results) {
                deleted += Math.max(value, 0);
            }
            return deleted;
        }
    }

    private void ensureUserSchema() throws SQLException {
        if (userSchemaChecked) {
            return;
        }

        synchronized (UserDao.class) {
            if (userSchemaChecked) {
                return;
            }

            try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "users", "profile_image_path")) {
                    if (!columns.next()) {
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate("ALTER TABLE users ADD COLUMN profile_image_path VARCHAR(500) NULL");
                        }
                    }
                }

                try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, "users", "registration_number")) {
                    if (!columns.next()) {
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate("ALTER TABLE users ADD COLUMN registration_number INT NULL");
                        }
                        assignRegistrationNumbersToExistingUsers(connection);
                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate("ALTER TABLE users MODIFY COLUMN registration_number INT NOT NULL");
                            statement.executeUpdate("ALTER TABLE users ADD CONSTRAINT uq_users_registration_number UNIQUE (registration_number)");
                        }
                    } else {
                        assignRegistrationNumbersToExistingUsers(connection);
                        ensureRegistrationNumberUniqueIndex(connection);
                    }
                }
            }

            userSchemaChecked = true;
        }
    }

    private void assignRegistrationNumbersToExistingUsers(Connection connection) throws SQLException {
        Set<Integer> usedNumbers = new HashSet<>();
        String selectExisting = "SELECT user_id, registration_number FROM users";
        try (PreparedStatement statement = connection.prepareStatement(selectExisting);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int registrationNumber = resultSet.getInt("registration_number");
                if (!resultSet.wasNull() && registrationNumber >= REGISTRATION_MIN && registrationNumber <= REGISTRATION_MAX) {
                    usedNumbers.add(registrationNumber);
                }
            }
        }

        String selectMissing = "SELECT user_id FROM users WHERE registration_number IS NULL OR registration_number < ? OR registration_number > ?";
        try (PreparedStatement statement = connection.prepareStatement(selectMissing)) {
            statement.setInt(1, REGISTRATION_MIN);
            statement.setInt(2, REGISTRATION_MAX);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("user_id");
                    int registrationNumber = nextAvailableRegistrationNumber(usedNumbers);
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE users SET registration_number = ? WHERE user_id = ?")) {
                        update.setInt(1, registrationNumber);
                        update.setInt(2, userId);
                        update.executeUpdate();
                    }
                    usedNumbers.add(registrationNumber);
                }
            }
        }
    }

    private void ensureRegistrationNumberUniqueIndex(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        boolean uniqueIndexExists = false;
        try (ResultSet indexes = metaData.getIndexInfo(connection.getCatalog(), null, "users", true, false)) {
            while (indexes.next()) {
                String columnName = indexes.getString("COLUMN_NAME");
                if ("registration_number".equalsIgnoreCase(columnName)) {
                    uniqueIndexExists = true;
                    break;
                }
            }
        }

        if (!uniqueIndexExists) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE users ADD CONSTRAINT uq_users_registration_number UNIQUE (registration_number)");
            }
        }
    }

    private int generateUniqueRegistrationNumber(Connection connection) throws SQLException {
        Set<Integer> usedNumbers = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT registration_number FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int registrationNumber = resultSet.getInt("registration_number");
                if (!resultSet.wasNull()) {
                    usedNumbers.add(registrationNumber);
                }
            }
        }

        return nextAvailableRegistrationNumber(usedNumbers);
    }

    private int nextAvailableRegistrationNumber(Set<Integer> usedNumbers) throws SQLException {
        int capacity = REGISTRATION_MAX - REGISTRATION_MIN + 1;
        if (usedNumbers.size() >= capacity) {
            throw new SQLException("Registration number limit reached. No IDs available between 1 and 50000.");
        }

        int candidate;
        do {
            candidate = RANDOM.nextInt(capacity) + REGISTRATION_MIN;
        } while (usedNumbers.contains(candidate));
        return candidate;
    }
}
