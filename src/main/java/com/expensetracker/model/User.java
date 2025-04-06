package com.expensetracker.model;

import java.util.Date;

/**
 * Class representing a user in the expense tracker system
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private Date createdAt;
    
    // Constructors
    public User() {
    }
    
    public User(int userId, String username, String password, String email, Date createdAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        // Ensure the password is never null or empty
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Warning: Attempted to set empty password");
            this.password = "";
        } else {
            this.password = password.trim();
        }
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 