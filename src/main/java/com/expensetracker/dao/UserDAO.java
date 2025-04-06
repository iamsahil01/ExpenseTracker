package com.expensetracker.dao;

import com.expensetracker.model.User;
import com.expensetracker.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User related database operations
 */
public class UserDAO {
    
    /**
     * Add a new user to the database
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update an existing user in the database
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, email = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete a user from the database by ID
     */
    public boolean deleteUser(int userId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            // Start a transaction
            conn.setAutoCommit(false);
            
            // First, delete all expenses for this user
            String deleteExpensesSQL = "DELETE FROM expenses WHERE user_id = ?";
            try (PreparedStatement expenseStmt = conn.prepareStatement(deleteExpensesSQL)) {
                expenseStmt.setInt(1, userId);
                expenseStmt.executeUpdate();
            }
            
            // Then, delete all user categories
            String deleteCategoriesSQL = "DELETE FROM categories WHERE user_id = ? AND is_default = FALSE";
            try (PreparedStatement categoryStmt = conn.prepareStatement(deleteCategoriesSQL)) {
                categoryStmt.setInt(1, userId);
                categoryStmt.executeUpdate();
            }
            
            // Finally, delete the user
            String deleteUserSQL = "DELETE FROM users WHERE user_id = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(deleteUserSQL)) {
                userStmt.setInt(1, userId);
                int affectedRows = userStmt.executeUpdate();
                
                // Commit the transaction if successful
                conn.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            // Rollback the transaction in case of error
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            // Reset auto-commit to true
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get a user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get a user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }
    
    /**
     * Authenticate a user with username and password
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    
                    // Compare passwords
                    if (user.getPassword().trim().equals(password.trim())) {
                        return user;
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Direct authentication using database query
     */
    public User directAuthenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // User found, check password manually
                    String storedPassword = rs.getString("password");
                    
                    if (storedPassword != null && storedPassword.trim().equals(password.trim())) {
                        return extractUserFromResultSet(rs);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Helper method to extract a User object from a ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        
        // Check if created_at column exists in the result set
        try {
            user.setCreatedAt(rs.getTimestamp("created_at"));
        } catch (SQLException e) {
            // If created_at column doesn't exist, set current time
            user.setCreatedAt(new java.util.Date());
        }
        
        return user;
    }
    
    /**
     * Count users in the database
     */
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.out.println("Error counting users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
} 