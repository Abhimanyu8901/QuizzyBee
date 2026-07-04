package com.quizzybee.dao;

import com.quizzybee.database.DatabaseConnection;
import com.quizzybee.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResultDao {

    public boolean saveResult(Result result) throws SQLException {
        String sql = "INSERT INTO results(user_id, category, score, total_questions, correct_answers, wrong_answers, percentage, date_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, result.getUserId());
            statement.setString(2, result.getCategory());
            statement.setInt(3, result.getScore());
            statement.setInt(4, result.getTotalQuestions());
            statement.setInt(5, result.getCorrectAnswers());
            statement.setInt(6, result.getWrongAnswers());
            statement.setDouble(7, result.getPercentage());
            statement.setTimestamp(8, Timestamp.valueOf(result.getDateTime()));
            return statement.executeUpdate() > 0;
        }
    }

    public int getAttemptCountByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM results WHERE user_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    public Optional<Result> getLatestResultByUser(int userId) throws SQLException {
        String sql = "SELECT r.result_id, r.user_id, u.registration_number, u.name AS user_name, r.category, r.score, r.total_questions, " +
                "r.correct_answers, r.wrong_answers, r.percentage, r.date_time " +
                "FROM results r JOIN users u ON u.user_id = r.user_id WHERE r.user_id = ? ORDER BY r.date_time DESC LIMIT 1";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResult(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public List<Result> getHistoryByUser(int userId) throws SQLException {
        String sql = "SELECT r.result_id, r.user_id, u.registration_number, u.name AS user_name, r.category, r.score, r.total_questions, " +
                "r.correct_answers, r.wrong_answers, r.percentage, r.date_time " +
                "FROM results r JOIN users u ON u.user_id = r.user_id WHERE r.user_id = ? ORDER BY r.date_time DESC";
        List<Result> history = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(mapResult(resultSet));
                }
            }
        }
        return history;
    }

    public List<Result> getLeaderboard() throws SQLException {
        String sql = "SELECT r.result_id, r.user_id, u.registration_number, u.name AS user_name, r.category, r.score, r.total_questions, " +
                "r.correct_answers, r.wrong_answers, r.percentage, r.date_time " +
                "FROM results r JOIN users u ON u.user_id = r.user_id " +
                "ORDER BY r.percentage DESC, r.score DESC, r.date_time ASC LIMIT 10";
        List<Result> leaderboard = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                leaderboard.add(mapResult(resultSet));
            }
        }
        for (int index = 0; index < leaderboard.size(); index++) {
            leaderboard.get(index).setRank(index + 1);
        }
        return leaderboard;
    }

    public List<Result> getAllScores() throws SQLException {
        String sql = "SELECT r.result_id, r.user_id, u.registration_number, u.name AS user_name, r.category, r.score, r.total_questions, " +
                "r.correct_answers, r.wrong_answers, r.percentage, r.date_time " +
                "FROM results r JOIN users u ON u.user_id = r.user_id ORDER BY r.date_time DESC";
        List<Result> scores = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                scores.add(mapResult(resultSet));
            }
        }
        return scores;
    }

    public int deleteResultsByIds(List<Integer> resultIds) throws SQLException {
        String sql = "DELETE FROM results WHERE result_id = ?";
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer resultId : resultIds) {
                statement.setInt(1, resultId);
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

    private Result mapResult(ResultSet resultSet) throws SQLException {
        Result result = new Result();
        result.setResultId(resultSet.getInt("result_id"));
        result.setUserId(resultSet.getInt("user_id"));
        result.setRegistrationNumber(resultSet.getInt("registration_number"));
        result.setUserName(resultSet.getString("user_name"));
        result.setCategory(resultSet.getString("category"));
        result.setScore(resultSet.getInt("score"));
        result.setTotalQuestions(resultSet.getInt("total_questions"));
        result.setCorrectAnswers(resultSet.getInt("correct_answers"));
        result.setWrongAnswers(resultSet.getInt("wrong_answers"));
        result.setPercentage(resultSet.getDouble("percentage"));
        result.setDateTime(resultSet.getTimestamp("date_time").toLocalDateTime());
        return result;
    }
}
