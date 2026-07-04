package com.quizzybee.util;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public final class NavigationManager {
    private static final NavigationManager INSTANCE = new NavigationManager();
    private static final double DEFAULT_WIDTH = 1366;
    private static final double DEFAULT_HEIGHT = 820;
    private Stage primaryStage;

    private NavigationManager() {
    }

    public static NavigationManager getInstance() {
        return INSTANCE;
    }

    public void init(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("QuizzyBee");
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        this.primaryStage.setMinWidth(Math.min(1180, bounds.getWidth()));
        this.primaryStage.setMinHeight(Math.min(760, bounds.getHeight()));
        this.primaryStage.setWidth(clampWidth(DEFAULT_WIDTH, bounds));
        this.primaryStage.setHeight(clampHeight(DEFAULT_HEIGHT, bounds));
        this.primaryStage.centerOnScreen();
    }

    public void showLogin() {
        setScene("/view/login.fxml");
    }

    public void showRegister() {
        setScene("/view/register.fxml");
    }

    public void showDashboard() {
        setScene("/view/dashboard.fxml");
    }

    public void showQuiz() {
        setScene("/view/quiz.fxml");
    }

    public void showResult() {
        setScene("/view/result.fxml");
    }

    public void showAdmin() {
        setScene("/view/admin.fxml");
    }

    public void showSettings() {
        setScene("/view/settings.fxml");
    }

    public void refreshCurrentScene() {
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    private void setScene(String fxmlPath) {
        try {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            double width = primaryStage.getWidth() > 0 ? primaryStage.getWidth() : DEFAULT_WIDTH;
            double height = primaryStage.getHeight() > 0 ? primaryStage.getHeight() : DEFAULT_HEIGHT;
            width = clampWidth(width, bounds);
            height = clampHeight(height, bounds);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Parent sceneRoot = BeeCursorEffect.wrapWithBeeOverlay(root);
            Scene scene = new Scene(sceneRoot, width, height);
            applyTheme(scene);
            primaryStage.setScene(scene);
            primaryStage.setWidth(width);
            primaryStage.setHeight(height);
            keepStageVisible(bounds);
            primaryStage.show();
            BeeCursorEffect.attach(scene);

            FadeTransition transition = new FadeTransition(Duration.millis(320), root);
            transition.setFromValue(0.35);
            transition.setToValue(1.0);
            transition.play();
        } catch (IOException exception) {
            AlertUtil.showError("Navigation Error", "Unable to load screen: " + fxmlPath + "\n" + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/view/styles/app.css").toExternalForm());
        scene.getRoot().getStyleClass().remove("dark");
        if (SessionManager.getInstance().isDarkMode()) {
            scene.getRoot().getStyleClass().add("dark");
        }
    }

    private double clampWidth(double requestedWidth, Rectangle2D bounds) {
        return Math.min(requestedWidth, bounds.getWidth());
    }

    private double clampHeight(double requestedHeight, Rectangle2D bounds) {
        return Math.min(requestedHeight, bounds.getHeight());
    }

    private void keepStageVisible(Rectangle2D bounds) {
        if (primaryStage.getWidth() > bounds.getWidth()) {
            primaryStage.setWidth(bounds.getWidth());
        }
        if (primaryStage.getHeight() > bounds.getHeight()) {
            primaryStage.setHeight(bounds.getHeight());
        }

        if (primaryStage.getX() < bounds.getMinX()) {
            primaryStage.setX(bounds.getMinX());
        }
        if (primaryStage.getY() < bounds.getMinY()) {
            primaryStage.setY(bounds.getMinY());
        }
        if (primaryStage.getX() + primaryStage.getWidth() > bounds.getMaxX()) {
            primaryStage.setX(bounds.getMaxX() - primaryStage.getWidth());
        }
        if (primaryStage.getY() + primaryStage.getHeight() > bounds.getMaxY()) {
            primaryStage.setY(bounds.getMaxY() - primaryStage.getHeight());
        }
    }
}
