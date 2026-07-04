package com.quizzybee.util;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static boolean isValid(String password) {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*");
    }

    public static String validationMessage() {
        return "Password must be at least 8 characters long and include uppercase, lowercase, and a number.";
    }
}
