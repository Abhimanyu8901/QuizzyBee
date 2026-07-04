package com.quizzybee.model;

public class User {
    private int userId;
    private int registrationNumber;
    private String name;
    private String email;
    private String password;
    private String role;
    private String profileImagePath;

    public User() {
    }

    public User(int userId, String name, String email, String password, String role) {
        this(userId, 0, name, email, password, role, null);
    }

    public User(int userId, String name, String email, String password, String role, String profileImagePath) {
        this(userId, 0, name, email, password, role, profileImagePath);
    }

    public User(int userId, int registrationNumber, String name, String email, String password, String role, String profileImagePath) {
        this.userId = userId;
        this.registrationNumber = registrationNumber;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImagePath = profileImagePath;
    }

    public User(String name, String email, String password, String role) {
        this(0, name, email, password, role, null);
    }

    public User(String name, String email, String password, String role, String profileImagePath) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.profileImagePath = profileImagePath;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(int registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
