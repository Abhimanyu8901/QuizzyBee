package com.quizzybee.main;

import com.quizzybee.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class QuizzyBeeLauncher extends Application {

    @Override
    public void start(Stage stage) {
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.init(stage);
        navigationManager.showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
