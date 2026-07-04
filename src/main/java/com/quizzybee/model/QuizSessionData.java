package com.quizzybee.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizSessionData {
    private final String category;
    private final List<Question> questions;
    private final int durationSeconds;
    private final Map<Integer, String> selectedAnswers = new HashMap<>();
    private int currentIndex;

    public QuizSessionData(String category, List<Question> questions, int durationSeconds) {
        this.category = category;
        this.questions = questions;
        this.durationSeconds = durationSeconds;
    }

    public String getCategory() {
        return category;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Map<Integer, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
}
