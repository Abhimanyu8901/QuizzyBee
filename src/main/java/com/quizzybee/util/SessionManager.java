package com.quizzybee.util;

import com.quizzybee.model.QuizSessionData;
import com.quizzybee.model.Result;
import com.quizzybee.model.User;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;
    private final Set<String> attemptedCategories = new HashSet<>();
    private QuizSessionData activeQuiz;
    private Result latestResult;
    private boolean darkMode;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public void login(User user) {
        currentUser = user;
        attemptedCategories.clear();
        activeQuiz = null;
        latestResult = null;
    }

    public void logout() {
        currentUser = null;
        attemptedCategories.clear();
        activeQuiz = null;
        latestResult = null;
    }

    public Set<String> getAttemptedCategories() {
        return attemptedCategories;
    }

    public QuizSessionData getActiveQuiz() {
        return activeQuiz;
    }

    public void setActiveQuiz(QuizSessionData activeQuiz) {
        this.activeQuiz = activeQuiz;
    }

    public Result getLatestResult() {
        return latestResult;
    }

    public void setLatestResult(Result latestResult) {
        this.latestResult = latestResult;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }
}
