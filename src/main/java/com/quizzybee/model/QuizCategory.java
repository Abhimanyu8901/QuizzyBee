package com.quizzybee.model;

public class QuizCategory {
    private int categoryId;
    private String name;
    private String description;
    private int durationMinutes;

    public QuizCategory() {
    }

    public QuizCategory(int categoryId, String name, String description, int durationMinutes) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
    }

    public QuizCategory(String name, String description, int durationMinutes) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
