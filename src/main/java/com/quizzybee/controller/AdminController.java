package com.quizzybee.controller;

import com.quizzybee.dao.CategoryDao;
import com.quizzybee.dao.QuestionDao;
import com.quizzybee.dao.ResultDao;
import com.quizzybee.model.CategoryQuestionSummary;
import com.quizzybee.model.Question;
import com.quizzybee.model.QuizCategory;
import com.quizzybee.model.Result;
import com.quizzybee.util.AlertUtil;
import com.quizzybee.util.ExcelQuestionExporter;
import com.quizzybee.util.ExcelQuestionImporter;
import com.quizzybee.util.NavigationManager;
import com.quizzybee.util.ProfileImageUtil;
import com.quizzybee.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AdminController {
    private enum DuplicateAction {
        SKIP,
        REPLACE,
        CANCEL
    }

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label profileInitialsLabel;

    @FXML
    private TableColumn<Question, Boolean> questionSelectColumn;

    @FXML
    private TableColumn<Question, Number> questionIdColumn;

    @FXML
    private TableColumn<Question, String> questionCategoryColumn;

    @FXML
    private TableColumn<Question, String> questionDifficultyColumn;

    @FXML
    private TextArea questionTextArea;

    @FXML
    private TextField optionAField;

    @FXML
    private TextField optionBField;

    @FXML
    private TextField optionCField;

    @FXML
    private TextField optionDField;

    @FXML
    private ComboBox<String> correctAnswerBox;

    @FXML
    private ComboBox<String> categoryBox;

    @FXML
    private ComboBox<String> difficultyBox;

    @FXML
    private TableView<Result> scoresTable;

    @FXML
    private TextField scoreSearchField;

    @FXML
    private TableColumn<Result, Boolean> scoreSelectColumn;

    @FXML
    private TableView<CategoryQuestionSummary> categorySummaryTable;

    @FXML
    private TextField categorySearchField;

    @FXML
    private TableColumn<CategoryQuestionSummary, Boolean> summarySelectColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, Number> summarySerialColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, String> summaryCategoryColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, Number> summaryEasyColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, Number> summaryMediumColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, Number> summaryHardColumn;

    @FXML
    private TableColumn<CategoryQuestionSummary, Number> summaryTotalColumn;

    @FXML
    private TableColumn<Result, String> scoreUserColumn;

    @FXML
    private TableColumn<Result, String> scoreCategoryColumn;

    @FXML
    private TableColumn<Result, Number> scoreValueColumn;

    @FXML
    private TableColumn<Result, Number> scorePercentageColumn;

    @FXML
    private TextField categoryNameField;

    @FXML
    private TextField categoryDescriptionField;

    private final QuestionDao questionDao = new QuestionDao();
    private final ResultDao resultDao = new ResultDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final ExcelQuestionImporter excelQuestionImporter = new ExcelQuestionImporter();
    private final ExcelQuestionExporter excelQuestionExporter = new ExcelQuestionExporter();
    private FilteredList<CategoryQuestionSummary> filteredCategorySummaries;
    private FilteredList<Result> filteredScores;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().getCurrentUser().isEmpty() || !SessionManager.getInstance().getCurrentUser().get().isAdmin()) {
            NavigationManager.getInstance().showLogin();
            return;
        }

        configureProfileImageView();
        updateProfileAvatar();
        correctAnswerBox.getItems().addAll("Option A", "Option B", "Option C", "Option D");
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        questionSelectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        questionSelectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(questionSelectColumn));
        questionSelectColumn.setEditable(true);
        questionIdColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuestionId()));
        questionCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        questionDifficultyColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDifficultyLevel()));

        scoreSelectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        scoreSelectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(scoreSelectColumn));
        scoreSelectColumn.setEditable(true);
        scoreUserColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUserName() + " (" + data.getValue().getRegistrationNumber() + ")"
        ));
        scoreCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        scoreValueColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getScore()));
        scorePercentageColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPercentage()));

        summarySelectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        summarySelectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(summarySelectColumn));
        summarySelectColumn.setEditable(true);
        summarySerialColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSerialNo()));
        summaryCategoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategoryName()));
        summaryEasyColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getEasyCount()));
        summaryMediumColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getMediumCount()));
        summaryHardColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getHardCount()));
        summaryTotalColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotalCount()));

        questionTable.setEditable(true);
        scoresTable.setEditable(true);
        categorySummaryTable.setEditable(true);
        questionTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedQuestion) -> populateQuestionForm(selectedQuestion));
        refreshTables();
        loadCategories();
        configureCategorySearch();
        configureScoreSearch();
    }

    private void configureProfileImageView() {
        Circle clip = new Circle(46, 46, 46);
        profileImageView.setClip(clip);
    }

    private void updateProfileAvatar() {
        SessionManager.getInstance().getCurrentUser().ifPresent(user -> {
            String initials = user.getName() == null || user.getName().isBlank()
                    ? "A"
                    : user.getName().trim().substring(0, 1).toUpperCase();
            profileInitialsLabel.setText(initials);

            String imageUri = ProfileImageUtil.toImageUri(user.getProfileImagePath());
            boolean hasImage = imageUri != null;
            profileImageView.setImage(hasImage ? ProfileImageUtil.loadImage(imageUri) : null);
            profileImageView.setVisible(hasImage);
            profileImageView.setManaged(hasImage);
            profileInitialsLabel.setVisible(!hasImage);
            profileInitialsLabel.setManaged(!hasImage);
        });
    }

    private void refreshTables() {
        try {
            questionTable.getItems().setAll(questionDao.getAllQuestions());
            filteredScores = new FilteredList<>(javafx.collections.FXCollections.observableArrayList(
                    resultDao.getAllScores()
            ), item -> true);
            scoresTable.setItems(filteredScores);
            filteredCategorySummaries = new FilteredList<>(javafx.collections.FXCollections.observableArrayList(
                    questionDao.getCategoryQuestionSummaries()
            ), item -> true);
            categorySummaryTable.setItems(filteredCategorySummaries);
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    private void configureCategorySearch() {
        categorySearchField.textProperty().addListener((observable, oldValue, newValue) -> applyCategoryFilter(newValue));
    }

    private void applyCategoryFilter(String searchText) {
        if (filteredCategorySummaries == null) {
            return;
        }
        String filter = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ENGLISH);
        filteredCategorySummaries.setPredicate(summary ->
                filter.isBlank() || summary.getCategoryName().toLowerCase(Locale.ENGLISH).contains(filter)
        );
    }

    private void configureScoreSearch() {
        scoreSearchField.textProperty().addListener((observable, oldValue, newValue) -> applyScoreFilter(newValue));
    }

    private void applyScoreFilter(String searchText) {
        if (filteredScores == null) {
            return;
        }
        String filter = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ENGLISH);
        filteredScores.setPredicate(result ->
                filter.isBlank()
                        || result.getUserName().toLowerCase(Locale.ENGLISH).contains(filter)
                        || result.getCategory().toLowerCase(Locale.ENGLISH).contains(filter)
        );
    }

    private void loadCategories() {
        try {
            categoryBox.getItems().clear();
            for (QuizCategory category : categoryDao.getAllCategories()) {
                categoryBox.getItems().add(category.getName());
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    private void populateQuestionForm(Question question) {
        if (question == null) {
            return;
        }
        questionTextArea.setText(question.getQuestionText());
        optionAField.setText(question.getOptionA());
        optionBField.setText(question.getOptionB());
        optionCField.setText(question.getOptionC());
        optionDField.setText(question.getOptionD());
        correctAnswerBox.setValue(labelForCorrectAnswer(question.getCorrectAnswer(), question));
        categoryBox.setValue(question.getCategory());
        difficultyBox.setValue(question.getDifficultyLevel());
    }

    private String labelForCorrectAnswer(String correctAnswer, Question question) {
        if (correctAnswer.equals(question.getOptionA())) {
            return "Option A";
        }
        if (correctAnswer.equals(question.getOptionB())) {
            return "Option B";
        }
        if (correctAnswer.equals(question.getOptionC())) {
            return "Option C";
        }
        return "Option D";
    }

    @FXML
    private void handleAddQuestion() {
        Question question = buildQuestionFromForm();
        if (question == null) {
            return;
        }

        try {
            Optional<Question> existing = questionDao.findByTextAndCategory(question.getQuestionText(), question.getCategory());
            if (existing.isPresent()) {
                DuplicateAction action = promptDuplicateAction(
                        "Duplicate Question Found",
                        "This question already exists in the question bank for category \"" + question.getCategory() + "\"."
                );
                if (action == DuplicateAction.CANCEL || action == DuplicateAction.SKIP) {
                    if (action == DuplicateAction.SKIP) {
                        AlertUtil.showInfo("Skipped", "Duplicate question was skipped.");
                    }
                    return;
                }

                question.setQuestionId(existing.get().getQuestionId());
                if (questionDao.updateQuestion(question)) {
                    AlertUtil.showInfo("Replaced", "Existing question was replaced successfully.");
                    clearQuestionForm();
                    refreshTables();
                }
                return;
            }

            if (questionDao.addQuestion(question)) {
                AlertUtil.showInfo("Success", "Question added successfully.");
                clearQuestionForm();
                refreshTables();
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleUpdateQuestion() {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            AlertUtil.showWarning("Selection Required", "Select a question to update.");
            return;
        }

        Question updated = buildQuestionFromForm();
        if (updated == null) {
            return;
        }
        updated.setQuestionId(selectedQuestion.getQuestionId());

        try {
            if (questionDao.updateQuestion(updated)) {
                AlertUtil.showInfo("Success", "Question updated successfully.");
                clearQuestionForm();
                refreshTables();
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteQuestion() {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            AlertUtil.showWarning("Selection Required", "Select a question to delete.");
            return;
        }

        try {
            if (questionDao.deleteQuestion(selectedQuestion.getQuestionId())) {
                AlertUtil.showInfo("Deleted", "Question deleted successfully.");
                clearQuestionForm();
                refreshTables();
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleCreateCategory() {
        String name = categoryNameField.getText().trim();
        String description = categoryDescriptionField.getText().trim();
        if (name.isBlank() || description.isBlank()) {
            AlertUtil.showWarning("Validation", "Category name and description are required.");
            return;
        }

        try {
            if (categoryDao.createCategory(new QuizCategory(name, description, 10))) {
                AlertUtil.showInfo("Success", "Category created successfully.");
                categoryNameField.clear();
                categoryDescriptionField.clear();
                loadCategories();
            }
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleImportQuestions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Questions From Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Supported Files", "*.xlsx", "*.xls", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(questionTable.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            List<Question> importedQuestions = excelQuestionImporter.readQuestions(selectedFile);
            if (importedQuestions.isEmpty()) {
                AlertUtil.showWarning("Import", "No questions found in the selected Excel file.");
                return;
            }

            Set<String> existingCategories = new HashSet<>();
            for (QuizCategory category : categoryDao.getAllCategories()) {
                existingCategories.add(category.getName().toLowerCase(Locale.ENGLISH));
            }

            boolean hasDuplicates = false;
            for (Question question : importedQuestions) {
                if (questionDao.findByTextAndCategory(question.getQuestionText(), question.getCategory()).isPresent()) {
                    hasDuplicates = true;
                    break;
                }
            }

            DuplicateAction duplicateAction = DuplicateAction.REPLACE;
            if (hasDuplicates) {
                duplicateAction = promptDuplicateAction(
                        "Duplicate Questions Found",
                        "Some imported questions already exist in the question bank. Choose Skip to keep existing questions or Replace to update them."
                );
                if (duplicateAction == DuplicateAction.CANCEL) {
                    return;
                }
            }

            int inserted = 0;
            int updated = 0;
            int skipped = 0;
            for (Question question : importedQuestions) {
                ensureCategoryExists(question.getCategory(), existingCategories);
                Optional<Question> existing = questionDao.findByTextAndCategory(question.getQuestionText(), question.getCategory());
                if (existing.isPresent()) {
                    if (duplicateAction == DuplicateAction.SKIP) {
                        skipped++;
                    } else {
                        question.setQuestionId(existing.get().getQuestionId());
                        if (questionDao.updateQuestion(question)) {
                            updated++;
                        }
                    }
                } else if (questionDao.addQuestion(question)) {
                    inserted++;
                }
            }

            loadCategories();
            refreshTables();
            AlertUtil.showInfo(
                    "Import Complete",
                    "Excel scan finished successfully.\nImported rows: " + importedQuestions.size()
                            + "\nInserted: " + inserted
                            + "\nUpdated: " + updated
                            + "\nSkipped: " + skipped
            );
        } catch (IllegalArgumentException exception) {
            AlertUtil.showError("Import Failed", exception.getMessage());
        } catch (Exception exception) {
            AlertUtil.showError("Import Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleExportQuestions() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Question Bank");
        fileChooser.setInitialFileName("quizzybee_question_bank.xlsx");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Workbook", "*.xlsx")
        );

        File selectedFile = fileChooser.showSaveDialog(questionTable.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        File exportFile = selectedFile.getName().toLowerCase(Locale.ENGLISH).endsWith(".xlsx")
                ? selectedFile
                : new File(selectedFile.getAbsolutePath() + ".xlsx");

        try {
            List<Question> questions = questionDao.getAllQuestions();
            if (questions.isEmpty()) {
                AlertUtil.showWarning("Export", "There are no questions available to export.");
                return;
            }

            excelQuestionExporter.exportQuestions(exportFile, questions);
            AlertUtil.showInfo("Export Complete", "Question bank exported successfully to:\n" + exportFile.getAbsolutePath());
        } catch (Exception exception) {
            AlertUtil.showError("Export Failed", exception.getMessage());
        }
    }

    @FXML
    private void handleSetCategoryTimer() {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            AlertUtil.showWarning("Selection Required", "Select a question row from the question bank to set that category's timer.");
            return;
        }

        String categoryName = selectedQuestion.getCategory();
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Set Category Timer");
        dialog.setHeaderText("Set quiz timer for category: " + categoryName);
        dialog.setContentText("Enter duration in minutes:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String value = result.get().trim();
        try {
            int durationMinutes = Integer.parseInt(value);
            if (durationMinutes < 1 || durationMinutes > 180) {
                AlertUtil.showWarning("Validation", "Enter a timer between 1 and 180 minutes.");
                return;
            }

            if (categoryDao.updateCategoryDuration(categoryName, durationMinutes)) {
                AlertUtil.showInfo("Timer Updated", "Quiz timer for " + categoryName + " set to " + durationMinutes + " minute(s).");
            } else {
                AlertUtil.showError("Update Failed", "Unable to update quiz timer for the selected category.");
            }
        } catch (NumberFormatException exception) {
            AlertUtil.showWarning("Validation", "Please enter a valid whole number for minutes.");
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelectedQuestions() {
        List<Question> selectedQuestions = questionTable.getItems().stream()
                .filter(Question::isSelected)
                .collect(Collectors.toList());
        if (selectedQuestions.isEmpty()) {
            AlertUtil.showWarning("Selection Required", "Tick one or more checkboxes in Question Bank.");
            return;
        }

        if (!confirmBulkDelete("Delete Questions", "Delete " + selectedQuestions.size() + " selected question(s)?")) {
            return;
        }

        try {
            int deleted = questionDao.deleteQuestions(
                    selectedQuestions.stream().map(Question::getQuestionId).collect(Collectors.toList())
            );
            clearQuestionForm();
            refreshTables();
            AlertUtil.showInfo("Deleted", deleted + " question(s) deleted successfully.");
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelectedCategories() {
        List<CategoryQuestionSummary> selectedCategories = categorySummaryTable.getItems().stream()
                .filter(CategoryQuestionSummary::isSelected)
                .collect(Collectors.toList());
        if (selectedCategories.isEmpty()) {
            AlertUtil.showWarning("Selection Required", "Tick one or more checkboxes in the category summary table.");
            return;
        }

        if (!confirmBulkDelete("Delete Categories",
                "Delete " + selectedCategories.size() + " selected categor" + (selectedCategories.size() == 1 ? "y" : "ies")
                        + "? This will also remove the questions in those categories.")) {
            return;
        }

        try {
            int deleted = categoryDao.deleteCategories(
                    selectedCategories.stream().map(CategoryQuestionSummary::getCategoryName).collect(Collectors.toList())
            );
            loadCategories();
            refreshTables();
            AlertUtil.showInfo("Deleted", deleted + " categor" + (deleted == 1 ? "y" : "ies") + " deleted successfully.");
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelectedScores() {
        List<Result> selectedScores = scoresTable.getItems().stream()
                .filter(Result::isSelected)
                .collect(Collectors.toList());
        if (selectedScores.isEmpty()) {
            AlertUtil.showWarning("Selection Required", "Tick one or more checkboxes in User Scores.");
            return;
        }

        if (!confirmBulkDelete("Delete Score Rows",
                "Delete " + selectedScores.size() + " checked score row(s)?")) {
            return;
        }

        try {
            int deleted = resultDao.deleteResultsByIds(
                    selectedScores.stream().map(Result::getResultId).collect(Collectors.toList())
            );
            refreshTables();
            AlertUtil.showInfo("Deleted", deleted + " score row(s) deleted successfully.");
        } catch (SQLException exception) {
            AlertUtil.showError("Database Error", exception.getMessage());
        }
    }

    private void ensureCategoryExists(String categoryName, Set<String> existingCategories) throws SQLException {
        String normalized = categoryName.toLowerCase(Locale.ENGLISH);
        if (!existingCategories.contains(normalized)) {
            categoryDao.createCategory(new QuizCategory(categoryName, "Imported from Excel", 10));
            existingCategories.add(normalized);
        }
    }

    private DuplicateAction promptDuplicateAction(String title, String message) {
        ButtonType skipButton = new ButtonType("Skip");
        ButtonType replaceButton = new ButtonType("Replace");
        ButtonType cancelButton = new ButtonType("Cancel");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(skipButton, replaceButton, cancelButton);

        Optional<ButtonType> selected = alert.showAndWait();
        if (selected.isPresent()) {
            if (selected.get() == skipButton) {
                return DuplicateAction.SKIP;
            }
            if (selected.get() == replaceButton) {
                return DuplicateAction.REPLACE;
            }
        }
        return DuplicateAction.CANCEL;
    }

    private boolean confirmBulkDelete(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    private Question buildQuestionFromForm() {
        if (questionTextArea.getText().isBlank() || optionAField.getText().isBlank() || optionBField.getText().isBlank()
                || optionCField.getText().isBlank() || optionDField.getText().isBlank()
                || correctAnswerBox.getValue() == null || categoryBox.getValue() == null || difficultyBox.getValue() == null) {
            AlertUtil.showWarning("Validation", "Complete the full question form before saving.");
            return null;
        }

        Question question = new Question();
        question.setQuestionText(questionTextArea.getText().trim());
        question.setOptionA(optionAField.getText().trim());
        question.setOptionB(optionBField.getText().trim());
        question.setOptionC(optionCField.getText().trim());
        question.setOptionD(optionDField.getText().trim());
        question.setCorrectAnswer(resolveCorrectAnswerValue(question));
        question.setCategory(categoryBox.getValue());
        question.setDifficultyLevel(difficultyBox.getValue());
        return question;
    }

    private String resolveCorrectAnswerValue(Question question) {
        return switch (correctAnswerBox.getValue()) {
            case "Option A" -> question.getOptionA();
            case "Option B" -> question.getOptionB();
            case "Option C" -> question.getOptionC();
            default -> question.getOptionD();
        };
    }

    private void clearQuestionForm() {
        questionTextArea.clear();
        optionAField.clear();
        optionBField.clear();
        optionCField.clear();
        optionDField.clear();
        correctAnswerBox.setValue(null);
        categoryBox.setValue(null);
        difficultyBox.setValue(null);
        questionTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleBackToDashboard() {
        NavigationManager.getInstance().showDashboard();
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        NavigationManager.getInstance().showLogin();
    }
}
