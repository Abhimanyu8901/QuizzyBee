package com.quizzybee.controller;

import com.quizzybee.dao.UserDao;
import com.quizzybee.model.User;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private final UserDao userDao = new UserDao();

    @FXML
    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            AlertUtil.showWarning("Validation", "All fields are required.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            AlertUtil.showWarning("Validation", "Enter a valid email address.");
            return;
        }

        if (!PasswordUtil.isValid(password)) {
            AlertUtil.showWarning("Validation", PasswordUtil.validationMessage());
            return;
        }

        if (!password.equals(confirmPassword)) {
            AlertUtil.showWarning("Validation", "Passwords do not match.");
            return;
        }

        try {
            if (userDao.existsByEmail(email)) {
                AlertUtil.showWarning("Validation", "An account with this email already exists.");
                return;
            }

            User newUser = new User(name, email, password, "USER");
            boolean created = userDao.registerUser(newUser);
            if (created) {
                AlertUtil.showInfo("Registration Successful",
                        "Your account is ready.\nRegistration ID: " + newUser.getRegistrationNumber() + "\nPlease log in.");
                NavigationManager.getInstance().showLogin();
            } else {
                AlertUtil.showError("Registration Failed", "Unable to create account.");
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        NavigationManager.getInstance().showLogin();
    }
}
