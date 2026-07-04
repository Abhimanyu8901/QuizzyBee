package com.quizzybee.controller;

import com.quizzybee.dao.CategoryDao;
import com.quizzybee.dao.QuestionDao;
import com.quizzybee.dao.ResultDao;
import com.quizzybee.model.Question;
import com.quizzybee.model.QuizCategory;
import com.quizzybee.model.QuizSessionData;
import com.quizzybee.model.Result;
import com.quizzybee.model.User;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.ProfileImageUtil;
import com.quizzybee.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label profileInitialsLabel;

    @FXML
    private Label attemptCountLabel;

    @FXML
    private Label lastScoreLabel;

    @FXML
    private VBox categoryContainer;

    @FXML
    private TableView<Result> historyTable;

    @FXML
    private TableColumn<Result, String> historyCategoryColumn;

    @FXML
    private TableColumn<Result, Number> historyScoreColumn;

    @FXML
    private TableColumn<Result, String> historyDateColumn;

    @FXML
    private TableView<Result> leaderboardTable;

    @FXML
    private TableColumn<Result, String> leaderboardRankColumn;

    @FXML
    private TableColumn<Result, Number> leaderboardUserIdColumn;

    @FXML
    private TableColumn<Result, String> leaderboardUserColumn;

    @FXML
    private TableColumn<Result, String> leaderboardCategoryColumn;

    @FXML
    private TableColumn<Result, Number> leaderboardPercentageColumn;

    @FXML
    private ToggleButton darkModeToggle;

    @FXML
    private Button adminButton;

    private final CategoryDao categoryDao = new CategoryDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final ResultDao resultDao = new ResultDao();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        configureTables();
        configureProfileImageView();
        sessionManager.getCurrentUser().ifPresentOrElse(this::loadDashboard, () -> NavigationManager.getInstance().showLogin());
        darkModeToggle.setSelected(sessionManager.isDarkMode());
    }

    private void configureProfileImageView() {
        Circle clip = new Circle(46, 46, 46);
        profileImageView.setClip(clip);
    }

    private void configureTables() {
        historyCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        historyScoreColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getScore()));
        historyDateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        ));

        leaderboardRankColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRankLabel()));
        leaderboardUserIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getRegistrationNumber()));
        leaderboardUserColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        leaderboardCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        leaderboardPercentageColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPercentage()));
    }

    private void loadDashboard(User user) {
        welcomeLabel.setText("Welcome back, " + user.getName());
        updateProfileAvatar(user);
        adminButton.setVisible(user.isAdmin());
        adminButton.setManaged(user.isAdmin());

        try {
            int attempts = resultDao.getAttemptCountByUser(user.getUserId());
            attemptCountLabel.setText(String.valueOf(attempts));

            Result latestResult = resultDao.getLatestResultByUser(user.getUserId()).orElse(null);
            sessionManager.setLatestResult(latestResult);
            lastScoreLabel.setText(latestResult == null ? "No attempts yet" :
                    latestResult.getScore() + "/" + latestResult.getTotalQuestions() + " (" + latestResult.getCategory() + ")");

            categoryContainer.getChildren().clear();
            for (QuizCategory category : categoryDao.getAllCategories()) {
                categoryContainer.getChildren().add(buildCategoryCard(category));
            }

            historyTable.getItems().setAll(resultDao.getHistoryByUser(user.getUserId()));
            List<Result> leaderboardResults = resultDao.getLeaderboard();
            for (int index = 0; index < leaderboardResults.size(); index++) {
                leaderboardResults.get(index).setRank(index + 1);
            }
            leaderboardTable.getItems().setAll(leaderboardResults);
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
    }

    private HBox buildCategoryCard(QuizCategory category) {
        Label name = new Label(category.getName());
        name.getStyleClass().add("card-title");
        Label description = new Label(category.getDescription());
        description.getStyleClass().add("muted-label");

        VBox details = new VBox(6, name, description);
        HBox.setHgrow(details, Priority.ALWAYS);

        Button startButton = new Button("Start Quiz");
        startButton.getStyleClass().add("primary-button");
        boolean attempted = sessionManager.getAttemptedCategories().contains(category.getName());
        startButton.setDisable(attempted);
        startButton.setText(attempted ? "Attempted" : "Start Quiz");
        startButton.setOnAction(event -> startQuiz(category));

        HBox card = new HBox(18, details, startButton);
        card.getStyleClass().add("category-card");
        return card;
    }

    private void startQuiz(QuizCategory category) {
        String categoryName = category.getName();
        if (sessionManager.getAttemptedCategories().contains(categoryName)) {
            AlertUtil.showWarning("Quiz Locked", "You already attempted this category in the current session.");
            return;
        }

        try {
            List<Question> questions = questionDao.getQuestionsByCategory(categoryName);
            if (questions.isEmpty()) {
                AlertUtil.showWarning("No Questions", "This category does not have any questions yet.");
                return;
            }
            sessionManager.setActiveQuiz(new QuizSessionData(categoryName, questions, category.getDurationMinutes() * 60));
            NavigationManager.getInstance().showQuiz();
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleDarkModeToggle() {
        sessionManager.setDarkMode(darkModeToggle.isSelected());
        NavigationManager.getInstance().refreshCurrentScene();
    }

    @FXML
    private void handleOpenAdmin() {
        NavigationManager.getInstance().showAdmin();
    }

    @FXML
    private void handleLogout() {
        sessionManager.logout();
        NavigationManager.getInstance().showLogin();
    }

    @FXML
    private void handleOpenSettings() {
        NavigationManager.getInstance().showSettings();
    }
}
