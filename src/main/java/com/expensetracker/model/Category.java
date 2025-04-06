package com.expensetracker.model;

/**
 * Class representing an expense category in the expense tracker system
 */
public class Category {
    private int categoryId;
    private String name;
    private String description;
    private int userId;
    private boolean isDefault;
    
    // Constructors
    public Category() {
    }
    
    public Category(int categoryId, String name, String description, int userId, boolean isDefault) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.userId = userId;
        this.isDefault = isDefault;
    }
    
    // Getters and Setters
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
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", userId=" + userId +
                ", isDefault=" + isDefault +
                '}';
    }
} 