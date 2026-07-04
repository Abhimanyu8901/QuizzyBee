package com.quizzybee.controller;

import com.quizzybee.model.Result;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ResultController {

    @FXML
    private Label scoreLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label percentageLabel;

    @FXML
    private Label performanceLabel;

    @FXML
    public void initialize() {
        Result result = SessionManager.getInstance().getLatestResult();
        if (result == null) {
            NavigationManager.getInstance().showDashboard();
            return;
        }

        scoreLabel.setText(result.getScore() + " / " + result.getTotalQuestions());
        summaryLabel.setText("Correct: " + result.getCorrectAnswers() + " | Wrong: " + result.getWrongAnswers() + " | Category: " + result.getCategory());
        percentageLabel.setText(String.format("%.2f%%", result.getPercentage()));
        performanceLabel.setText(getPerformanceMessage(result.getPercentage()));
    }

    private String getPerformanceMessage(double percentage) {
        if (percentage >= 85) {
            return "Excellent performance. You are quiz-master material.";
        }
        if (percentage >= 60) {
            return "Good job. A little more practice will push you even higher.";
        }
        return "Improve and try again. Reviewing the basics will help a lot.";
    }

    @FXML
    private void handleBackToDashboard() {
        NavigationManager.getInstance().showDashboard();
    }
}
