package com.expensetracker.controller;

import com.expensetracker.dao.UserDAO;
import com.expensetracker.model.User;


/**
 * Controller class for handling user-related operations
 */
public class UserController {
    
    private UserDAO userDAO;
    
    /**
     * Constructor
     */
    public UserController() {
        userDAO = new UserDAO();
    }
    
    /**
     * Register a new user
     */
    public boolean registerUser(String username, String password, String email) {
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return false;
        }
        
        // Clean inputs
        username = username.trim();
        password = password.trim();
        email = email.trim();
        
        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            return false;
        }
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        
        // Add user to database
        return userDAO.addUser(user);
    }
    
    /**
     * Authenticate a user
     */
    public User authenticateUser(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return null;
        }
        
        // Clean inputs
        username = username.trim();
        password = password.trim();
        
        // Try direct database authentication 
        User user = userDAO.directAuthenticate(username, password);
        if (user != null) {
            return user;
        }
        
        // Fall back to standard authentication if direct method fails
        return userDAO.authenticateUser(username, password);
    }
    
    /**
     * Update user information
     */
    public boolean updateUser(User user) {
        if (user == null) {
            return false;
        }
        
        return userDAO.updateUser(user);
    }
    
    /**
     * Get a user by ID
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }
    
    /**
     * Get a user by username
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        return userDAO.getUserByUsername(username);
    }
    
    /**
     * Check if any users exist in the database
     */
    public boolean doUsersExist() {
        return userDAO.countUsers() > 0;
    }
    
    /**
     * Delete a user account
     */
    public boolean deleteUser(int userId) {
        if (userId <= 0) {
            return false;
        }
        
        return userDAO.deleteUser(userId);
    }
} 