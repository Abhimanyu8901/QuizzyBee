package com.quizzybee.dao;

import com.quizzybee.database.DatabaseConnection;
import com.quizzybee.model.CategoryQuestionSummary;
import com.quizzybee.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QuestionDao {

    public List<Question> getQuestionsByCategory(String category) throws SQLException {
        String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, " +
                "correct_answer, category, difficulty_level FROM questions WHERE category = ?";
        List<Question> questions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    questions.add(mapQuestion(resultSet));
                }
            }
        }
        Collections.shuffle(questions);
        return questions;
    }

    public List<Question> getAllQuestions() throws SQLException {
        String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, " +
                "correct_answer, category, difficulty_level FROM questions ORDER BY category, question_id";
        List<Question> questions = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                questions.add(mapQuestion(resultSet));
            }
        }
        return questions;
    }

    public boolean addQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions(question_text, option_a, option_b, option_c, option_d, correct_answer, category, difficulty_level) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, question);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET question_text = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, " +
                "correct_answer = ?, category = ?, difficulty_level = ? WHERE question_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, question);
            statement.setInt(9, question.getQuestionId());
            return statement.executeUpdate() > 0;
        }
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, questionId);
            return statement.executeUpdate() > 0;
        }
    }

    public int deleteQuestions(List<Integer> questionIds) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer questionId : questionIds) {
                statement.setInt(1, questionId);
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            int deleted = 0;
            for (int value : results) {
                deleted += Math.max(value, 0);
            }
            return deleted;
        }
    }

    public Optional<Question> findByTextAndCategory(String questionText, String category) throws SQLException {
        String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, " +
                "correct_answer, category, difficulty_level FROM questions WHERE question_text = ? AND category = ? LIMIT 1";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, questionText);
            statement.setString(2, category);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapQuestion(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public boolean upsertQuestion(Question question) throws SQLException {
        Optional<Question> existing = findByTextAndCategory(question.getQuestionText(), question.getCategory());
        if (existing.isPresent()) {
            question.setQuestionId(existing.get().getQuestionId());
            return updateQuestion(question);
        }
        return addQuestion(question);
    }

    public List<CategoryQuestionSummary> getCategoryQuestionSummaries() throws SQLException {
        String sql = "SELECT category, " +
                "SUM(CASE WHEN difficulty_level = 'Easy' THEN 1 ELSE 0 END) AS easy_count, " +
                "SUM(CASE WHEN difficulty_level = 'Medium' THEN 1 ELSE 0 END) AS medium_count, " +
                "SUM(CASE WHEN difficulty_level = 'Hard' THEN 1 ELSE 0 END) AS hard_count " +
                "FROM questions GROUP BY category ORDER BY category";
        List<CategoryQuestionSummary> summaries = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            int serialNo = 1;
            while (resultSet.next()) {
                summaries.add(new CategoryQuestionSummary(
                        serialNo++,
                        resultSet.getString("category"),
                        resultSet.getInt("easy_count"),
                        resultSet.getInt("medium_count"),
                        resultSet.getInt("hard_count")
                ));
            }
        }
        return summaries;
    }

    private Question mapQuestion(ResultSet resultSet) throws SQLException {
        return new Question(
                resultSet.getInt("question_id"),
                resultSet.getString("question_text"),
                resultSet.getString("option_a"),
                resultSet.getString("option_b"),
                resultSet.getString("option_c"),
                resultSet.getString("option_d"),
                resultSet.getString("correct_answer"),
                resultSet.getString("category"),
                resultSet.getString("difficulty_level")
        );
    }

    private void fillStatement(PreparedStatement statement, Question question) throws SQLException {
        statement.setString(1, question.getQuestionText());
        statement.setString(2, question.getOptionA());
        statement.setString(3, question.getOptionB());
        statement.setString(4, question.getOptionC());
        statement.setString(5, question.getOptionD());
        statement.setString(6, question.getCorrectAnswer());
        statement.setString(7, question.getCategory());
        statement.setString(8, question.getDifficultyLevel());
    }
}
