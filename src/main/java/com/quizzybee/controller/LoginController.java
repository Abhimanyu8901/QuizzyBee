package com.quizzybee.controller;

import com.quizzybee.dao.UserDao;
import com.quizzybee.model.User;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleUserLogin() {
        authenticate("USER");
    }

    @FXML
    private void handleAdminLogin() {
        authenticate("ADMIN");
    }

    @FXML
    private void handleGoToRegister() {
        NavigationManager.getInstance().showRegister();
    }

    private void authenticate(String role) {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            AlertUtil.showWarning("Validation", "Email and password are required.");
            return;
        }

        try {
            Optional<User> user = userDao.login(email, password, role);
            if (user.isPresent()) {
                SessionManager.getInstance().login(user.get());
                if (user.get().isAdmin()) {
                    NavigationManager.getInstance().showAdmin();
                } else {
                    NavigationManager.getInstance().showDashboard();
                }
            } else {
                AlertUtil.showError("Login Failed", "Invalid credentials or role.");
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }
}
