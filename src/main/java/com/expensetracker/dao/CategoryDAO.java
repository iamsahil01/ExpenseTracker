package com.expensetracker.dao;

import com.expensetracker.model.Category;
import com.expensetracker.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Category related database operations
 */
public class CategoryDAO {
    
    /**
     * Add a new category to the database
     */
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO categories (name, description, user_id, is_default) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getUserId());
            pstmt.setBoolean(4, category.isDefault());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt(1));
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
     * Update an existing category in the database
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, user_id = ?, is_default = ? WHERE category_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getDescription());
            pstmt.setInt(3, category.getUserId());
            pstmt.setBoolean(4, category.isDefault());
            pstmt.setInt(5, category.getCategoryId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete a category from the database by ID
     */
    public boolean deleteCategory(int categoryId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);  // Start transaction
            
            // First, find the default "Other" category or any default category
            int defaultCategoryId = -1;
            String findDefaultSql = "SELECT category_id FROM categories WHERE name = 'Other' AND is_default = TRUE LIMIT 1";
            try (PreparedStatement defaultStmt = conn.prepareStatement(findDefaultSql)) {
                ResultSet rs = defaultStmt.executeQuery();
                if (rs.next()) {
                    defaultCategoryId = rs.getInt("category_id");
                } else {
                    // If "Other" not found, find any default category
                    String anyDefaultSql = "SELECT category_id FROM categories WHERE is_default = TRUE LIMIT 1";
                    try (PreparedStatement anyDefaultStmt = conn.prepareStatement(anyDefaultSql)) {
                        ResultSet defaultRs = anyDefaultStmt.executeQuery();
                        if (defaultRs.next()) {
                            defaultCategoryId = defaultRs.getInt("category_id");
                        } else {
                            // No default category found, cannot proceed
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }
            
            // Next, reassign all expenses from this category to the default category
            String updateExpensesSql = "UPDATE expenses SET category_id = ? WHERE category_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateExpensesSql)) {
                updateStmt.setInt(1, defaultCategoryId);
                updateStmt.setInt(2, categoryId);
                updateStmt.executeUpdate();
            }
            
            // Finally, delete the category
            String deleteCategorySql = "DELETE FROM categories WHERE category_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCategorySql)) {
                deleteStmt.setInt(1, categoryId);
                int affectedRows = deleteStmt.executeUpdate();
                
                // Commit the transaction if successful
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            // If any exception occurs, rollback the transaction
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
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);  // Reset auto-commit to default
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Get a category by ID
     */
    public Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCategoryFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categories.add(extractCategoryFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Get all categories for a specific user
     */
    public List<Category> getCategoriesByUser(int userId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE user_id = ? OR is_default = TRUE";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    categories.add(extractCategoryFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return categories;
    }
    
    /**
     * Helper method to extract a Category object from a ResultSet
     */
    private Category extractCategoryFromResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setUserId(rs.getInt("user_id"));
        category.setDefault(rs.getBoolean("is_default"));
        return category;
    }
} 