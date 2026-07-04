package com.quizzybee.controller;

import com.quizzybee.dao.UserDao;
import com.quizzybee.model.User;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.PasswordUtil;
import com.quizzybee.util.ProfileImageUtil;
import com.quizzybee.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class SettingsController {

    @FXML
    private Label emailLabel;

    @FXML
    private Label registrationNumberLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label profileInitialsLabel;

    @FXML
    private TextField nameField;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button removePhotoButton;

    private final UserDao userDao = new UserDao();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        configureProfileImageView();
        User currentUser = sessionManager.getCurrentUser().orElse(null);
        if (currentUser == null) {
            NavigationManager.getInstance().showLogin();
            return;
        }

        emailLabel.setText(currentUser.getEmail());
        registrationNumberLabel.setText(String.valueOf(currentUser.getRegistrationNumber()));
        nameField.setText(currentUser.getName());
        nameField.textProperty().addListener((observable, oldValue, newValue) -> profileInitialsLabel.setText(
                newValue == null || newValue.trim().isBlank() ? "U" : newValue.trim().substring(0, 1).toUpperCase()
        ));
        updateProfileAvatar(currentUser);
    }

    private void configureProfileImageView() {
        Circle clip = new Circle(52, 52, 52);
        profileImageView.setClip(clip);
    }

    @FXML
    private void handleSaveChanges() {
        User currentUser = sessionManager.getCurrentUser().orElse(null);
        if (currentUser == null) {
            NavigationManager.getInstance().showLogin();
            return;
        }

        String newName = nameField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newName.isBlank() || currentPassword.isBlank()) {
            AlertUtil.showWarning("Validation", "Name and current password are required.");
            return;
        }

        if (!currentUser.getPassword().equals(currentPassword)) {
            AlertUtil.showError("Validation", "Current password is incorrect.");
            return;
        }

        String passwordToSave = currentUser.getPassword();
        if (!newPassword.isBlank() || !confirmPassword.isBlank()) {
            if (!PasswordUtil.isValid(newPassword)) {
                AlertUtil.showWarning("Validation", PasswordUtil.validationMessage());
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                AlertUtil.showWarning("Validation", "New password and confirm password do not match.");
                return;
            }
            passwordToSave = newPassword;
        }

        try {
            if (userDao.updateProfile(currentUser.getUserId(), newName, passwordToSave)) {
                currentUser.setName(newName);
                currentUser.setPassword(passwordToSave);
                AlertUtil.showInfo("Settings Updated", "Your profile changes were saved successfully.");
                NavigationManager.getInstance().showDashboard();
            } else {
                AlertUtil.showError("Update Failed", "Unable to update your settings.");
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleChangePhoto() {
        User currentUser = sessionManager.getCurrentUser().orElse(null);
        if (currentUser == null) {
            NavigationManager.getInstance().showLogin();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            String previousPath = currentUser.getProfileImagePath();
            String storedPath = ProfileImageUtil.saveProfileImage(currentUser.getUserId(), selectedFile);
            if (userDao.updateProfileImage(currentUser.getUserId(), storedPath)) {
                currentUser.setProfileImagePath(storedPath);
                ProfileImageUtil.deleteProfileImage(previousPath);
                updateProfileAvatar(currentUser);
                AlertUtil.showInfo("Profile Picture Updated", "Your profile picture has been updated.");
            } else {
                ProfileImageUtil.deleteProfileImage(storedPath);
                AlertUtil.showError("Update Failed", "Unable to save your profile picture.");
            }
        } catch (IOException exception) {
            AlertUtil.showError("File Error", exception.getMessage());
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleRemovePhoto() {
        User currentUser = sessionManager.getCurrentUser().orElse(null);
        if (currentUser == null) {
            NavigationManager.getInstance().showLogin();
            return;
        }

        if (currentUser.getProfileImagePath() == null || currentUser.getProfileImagePath().isBlank()) {
            AlertUtil.showWarning("No Profile Picture", "There is no profile picture to remove.");
            return;
        }

        try {
            String previousPath = currentUser.getProfileImagePath();
            if (userDao.updateProfileImage(currentUser.getUserId(), null)) {
                currentUser.setProfileImagePath(null);
                ProfileImageUtil.deleteProfileImage(previousPath);
                updateProfileAvatar(currentUser);
                AlertUtil.showInfo("Profile Picture Removed", "Your profile picture has been removed.");
            } else {
                AlertUtil.showError("Update Failed", "Unable to remove your profile picture.");
            }
        } catch (IOException exception) {
            AlertUtil.showError("File Error", exception.getMessage());
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    private void updateProfileAvatar(User user) {
        String initials = user.getName() == null || user.getName().isBlank()
                ? "U"
                : user.getName().trim().substring(0, 1).toUpperCase();
        profileInitialsLabel.setText(initials);

        String imageUri = ProfileImageUtil.toImageUri(user.getProfileImagePath());
        boolean hasImage = imageUri != null;
        profileImageView.setImage(hasImage ? ProfileImageUtil.loadImage(imageUri) : null);
        profileImageView.setVisible(hasImage);
        profileImageView.setManaged(hasImage);
        profileInitialsLabel.setVisible(!hasImage);
        profileInitialsLabel.setManaged(!hasImage);
        removePhotoButton.setDisable(!hasImage);
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().showDashboard();
    }
}
