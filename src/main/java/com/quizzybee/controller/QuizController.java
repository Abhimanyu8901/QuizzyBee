package com.quizzybee.controller;

import com.quizzybee.dao.ResultDao;
import com.quizzybee.model.Question;
import com.quizzybee.model.QuizSessionData;
import com.quizzybee.model.Result;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.AudioUtil;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QuizController {
    private static final int MAX_FOCUS_VIOLATIONS = 2;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label timerLabel;

    @FXML
    private Label questionCounterLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private ProgressBar quizProgressBar;

    @FXML
    private RadioButton optionOneRadio;

    @FXML
    private RadioButton optionTwoRadio;

    @FXML
    private RadioButton optionThreeRadio;

    @FXML
    private RadioButton optionFourRadio;

    private final ToggleGroup optionsGroup = new ToggleGroup();
    private final ResultDao resultDao = new ResultDao();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final Map<Integer, List<String>> optionMap = new HashMap<>();

    private QuizSessionData quizSession;
    private ScheduledExecutorService scheduler;
    private int remainingSeconds;
    private boolean submitted;
    private int focusViolations;
    private boolean suppressFocusHandling;

    @FXML
    public void initialize() {
        optionOneRadio.setToggleGroup(optionsGroup);
        optionTwoRadio.setToggleGroup(optionsGroup);
        optionThreeRadio.setToggleGroup(optionsGroup);
        optionFourRadio.setToggleGroup(optionsGroup);

        quizSession = sessionManager.getActiveQuiz();
        if (quizSession == null) {
            NavigationManager.getInstance().showDashboard();
            return;
        }

        categoryLabel.setText(quizSession.getCategory());
        quizSession.getQuestions().forEach(question -> optionMap.put(question.getQuestionId(), question.shuffledOptions()));
        remainingSeconds = quizSession.getDurationSeconds();
        renderQuestion();
        startTimer();
        Platform.runLater(this::attachFocusProtection);
    }

    private void attachFocusProtection() {
        Stage stage = (Stage) questionLabel.getScene().getWindow();
        if (stage == null) {
            return;
        }

        stage.focusedProperty().addListener((observable, oldValue, focused) -> {
            if (focused || submitted || suppressFocusHandling) {
                return;
            }

            handleFocusViolation();
        });
    }

    private void handleFocusViolation() {
        focusViolations++;
        storeCurrentSelection();

        if (focusViolations >= MAX_FOCUS_VIOLATIONS) {
            suppressFocusHandling = true;
            AlertUtil.showError(
                    "Quiz Auto-Submitted",
                    "You switched away from the quiz window " + focusViolations + " times. "
                            + "For security, the quiz has been submitted automatically."
            );
            suppressFocusHandling = false;
            submitQuiz();
            return;
        }

        suppressFocusHandling = true;
        AlertUtil.showWarning(
                "Tab Switching Detected",
                "Leaving the quiz window is not allowed.\n"
                        + "Violation " + focusViolations + " of " + MAX_FOCUS_VIOLATIONS + ".\n"
                        + "If this happens again, your quiz will be submitted automatically."
        );
        suppressFocusHandling = false;
    }

    private void startTimer() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            remainingSeconds--;
            Platform.runLater(() -> timerLabel.setText(formatTime(remainingSeconds)));
            if (remainingSeconds <= 0) {
                shutdownTimer();
                Platform.runLater(this::submitQuiz);
            }
        }, 1, 1, TimeUnit.SECONDS);
        timerLabel.setText(formatTime(remainingSeconds));
    }

    private String formatTime(int seconds) {
        int minutes = Math.max(seconds, 0) / 60;
        int remaining = Math.max(seconds, 0) % 60;
        return String.format("Time Left: %02d:%02d", minutes, remaining);
    }

    private void renderQuestion() {
        Question question = quizSession.getQuestions().get(quizSession.getCurrentIndex());
        questionCounterLabel.setText("Question " + (quizSession.getCurrentIndex() + 1) + " of " + quizSession.getQuestions().size());
        questionLabel.setText(question.getQuestionText());

        List<String> options = optionMap.get(question.getQuestionId());
        optionOneRadio.setText(options.get(0));
        optionTwoRadio.setText(options.get(1));
        optionThreeRadio.setText(options.get(2));
        optionFourRadio.setText(options.get(3));

        String selected = quizSession.getSelectedAnswers().get(question.getQuestionId());
        optionsGroup.selectToggle(null);
        if (selected != null) {
            if (selected.equals(optionOneRadio.getText())) {
                optionOneRadio.setSelected(true);
            } else if (selected.equals(optionTwoRadio.getText())) {
                optionTwoRadio.setSelected(true);
            } else if (selected.equals(optionThreeRadio.getText())) {
                optionThreeRadio.setSelected(true);
            } else if (selected.equals(optionFourRadio.getText())) {
                optionFourRadio.setSelected(true);
            }
        }

        quizProgressBar.setProgress((quizSession.getCurrentIndex() + 1.0) / quizSession.getQuestions().size());
    }

    private void storeCurrentSelection() {
        Question question = quizSession.getQuestions().get(quizSession.getCurrentIndex());
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected != null) {
            quizSession.getSelectedAnswers().put(question.getQuestionId(), selected.getText());
        }
    }

    @FXML
    private void handlePrevious() {
        storeCurrentSelection();
        if (quizSession.getCurrentIndex() > 0) {
            quizSession.setCurrentIndex(quizSession.getCurrentIndex() - 1);
            renderQuestion();
        }
    }

    @FXML
    private void handleNext() {
        storeCurrentSelection();
        if (quizSession.getCurrentIndex() < quizSession.getQuestions().size() - 1) {
            quizSession.setCurrentIndex(quizSession.getCurrentIndex() + 1);
            renderQuestion();
        }
    }

    @FXML
    private void handleSubmit() {
        storeCurrentSelection();
        submitQuiz();
    }

    private void submitQuiz() {
        if (submitted) {
            return;
        }
        submitted = true;
        shutdownTimer();
        if (quizSession == null) {
            return;
        }

        int correct = 0;
        int wrong = 0;
        for (Question question : quizSession.getQuestions()) {
            String answer = quizSession.getSelectedAnswers().get(question.getQuestionId());
            if (question.getCorrectAnswer().equals(answer)) {
                correct++;
            } else {
                wrong++;
            }
        }

        int total = quizSession.getQuestions().size();
        double percentage = total == 0 ? 0 : (correct * 100.0) / total;

        Result result = new Result();
        result.setUserId(sessionManager.getCurrentUser().orElseThrow().getUserId());
        result.setCategory(quizSession.getCategory());
        result.setScore(correct);
        result.setTotalQuestions(total);
        result.setCorrectAnswers(correct);
        result.setWrongAnswers(wrong);
        result.setPercentage(percentage);
        result.setDateTime(LocalDateTime.now());

        try {
            resultDao.saveResult(result);
            if (correct > 0) {
                AudioUtil.playCorrectAnswerSound();
            }
            sessionManager.getAttemptedCategories().add(quizSession.getCategory());
            sessionManager.setLatestResult(result);
            sessionManager.setActiveQuiz(null);
            AlertUtil.showInfo("Quiz Submitted", "Your responses were submitted successfully.");
            NavigationManager.getInstance().showResult();
        } catch (SQLException exception) {
            AlertUtil.showError("Save Failed", exception.getMessage());
        }
    }

    private void shutdownTimer() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
