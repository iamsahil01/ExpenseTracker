package com.expensetracker.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Class representing an expense in the expense tracker system
 */
public class Expense {
    private int expenseId;
    private int userId;
    private int categoryId;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private Date expenseDate;
    private Date createdAt;
    
    // Constructors
    public Expense() {
    }
    
    public Expense(int expenseId, int userId, int categoryId, BigDecimal amount, 
                  String description, Date expenseDate, Date createdAt) {
        this.expenseId = expenseId;
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getExpenseId() {
        return expenseId;
    }
    
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Date getExpenseDate() {
        return expenseDate;
    }
    
    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "Expense{" +
                "expenseId=" + expenseId +
                ", userId=" + userId +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", expenseDate=" + expenseDate +
                ", createdAt=" + createdAt +
                '}';
    }
} 