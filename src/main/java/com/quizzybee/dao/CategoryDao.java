package com.quizzybee.dao;

import com.quizzybee.database.DatabaseConnection;
import com.quizzybee.model.QuizCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<QuizCategory> getAllCategories() throws SQLException {
        String sql = "SELECT category_id, name, description, quiz_duration_minutes FROM categories ORDER BY name";
        List<QuizCategory> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new QuizCategory(
                        resultSet.getInt("category_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getInt("quiz_duration_minutes")
                ));
            }
        }
        return categories;
    }

    public boolean createCategory(QuizCategory category) throws SQLException {
        String sql = "INSERT INTO categories(name, description, quiz_duration_minutes) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, category.getDurationMinutes());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateCategoryDuration(String categoryName, int durationMinutes) throws SQLException {
        String sql = "UPDATE categories SET quiz_duration_minutes = ? WHERE name = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, durationMinutes);
            statement.setString(2, categoryName);
            return statement.executeUpdate() > 0;
        }
    }

    public int deleteCategories(List<String> categoryNames) throws SQLException {
        String sql = "DELETE FROM categories WHERE name = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String categoryName : categoryNames) {
                statement.setString(1, categoryName);
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
}
