package com.quizzybee.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static volatile DatabaseConnection instance;

    private static final String URL = System.getProperty(
            "quizzybee.db.url",
            "jdbc:mysql://localhost:3306/quizzybee_db?useSSL=false&serverTimezone=UTC"
    );
    private static final String USERNAME = System.getProperty("quizzybee.db.user", "root");
    private static final String PASSWORD = System.getProperty("quizzybee.db.password", "");

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
