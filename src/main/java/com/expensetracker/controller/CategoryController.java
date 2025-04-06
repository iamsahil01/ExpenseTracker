package com.expensetracker.controller;

import com.expensetracker.dao.CategoryDAO;
import com.expensetracker.model.Category;

import java.util.List;

/**
 * Controller class for handling category-related operations
 */
public class CategoryController {
    
    private CategoryDAO categoryDAO;
    
    /**
     * Constructor
     */
    public CategoryController() {
        categoryDAO = new CategoryDAO();
    }
    
    /**
     * Add a new category
     */
    public boolean addCategory(String name, String description, int userId) {
        // Validate input
        if (name == null || name.trim().isEmpty() || userId <= 0) {
            return false;
        }
        
        // Create new category
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setUserId(userId);
        category.setDefault(false);
        
        // Add category to database
        return categoryDAO.addCategory(category);
    }
    
    /**
     * Update an existing category
     */
    public boolean updateCategory(int categoryId, String name, String description, int userId) {
        // Validate input
        if (categoryId <= 0 || name == null || name.trim().isEmpty() || userId <= 0) {
            return false;
        }
        
        // Get existing category
        Category category = categoryDAO.getCategoryById(categoryId);
        
        // Make sure the category exists and belongs to the user
        if (category == null || (category.getUserId() != userId && !category.isDefault())) {
            return false;
        }
        
        // Update category fields
        category.setName(name);
        category.setDescription(description);
        
        // Update category in database
        return categoryDAO.updateCategory(category);
    }
    
    /**
     * Delete a category
     */
    public boolean deleteCategory(int categoryId, int userId) {
        // Validate input
        if (categoryId <= 0 || userId <= 0) {
            return false;
        }
        
        // Get the category to check if it's a default one
        Category category = categoryDAO.getCategoryById(categoryId);
        
        // Cannot delete default categories
        if (category == null || category.isDefault()) {
            return false;
        }
        
        // Make sure the category belongs to the user
        if (category.getUserId() != userId) {
            return false;
        }
        
        // Delete category from database
        return categoryDAO.deleteCategory(categoryId);
    }
    
    /**
     * Get a category by ID
     */
    public Category getCategoryById(int categoryId) {
        // Validate input
        if (categoryId <= 0) {
            return null;
        }
        
        return categoryDAO.getCategoryById(categoryId);
    }
    
    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }
    
    /**
     * Get all categories for a specific user
     */
    public List<Category> getCategoriesByUser(int userId) {
        // Validate input
        if (userId <= 0) {
            return null;
        }
        
        return categoryDAO.getCategoriesByUser(userId);
    }
} 