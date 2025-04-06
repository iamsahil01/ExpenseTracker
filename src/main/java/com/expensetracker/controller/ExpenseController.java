package com.expensetracker.controller;

import com.expensetracker.dao.ExpenseDAO;
import com.expensetracker.model.Expense;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Controller class for handling expense-related operations
 */
public class ExpenseController {
    
    private ExpenseDAO expenseDAO;
    
    /**
     * Constructor
     */
    public ExpenseController() {
        expenseDAO = new ExpenseDAO();
    }
    
    /**
     * Add a new expense
     */
    public boolean addExpense(int userId, int categoryId, BigDecimal amount, 
                             String description, Date expenseDate) {
        // Validate input
        if (userId <= 0 || categoryId <= 0 || amount == null || 
            description == null || expenseDate == null) {
            return false;
        }
        
        // Create new expense
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategoryId(categoryId);
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setExpenseDate(expenseDate);
        
        // Add expense to database
        return expenseDAO.addExpense(expense);
    }
    
    /**
     * Update an existing expense
     */
    public boolean updateExpense(int expenseId, int userId, int categoryId, 
                                BigDecimal amount, String description, Date expenseDate) {
        // Validate input
        if (expenseId <= 0 || userId <= 0 || categoryId <= 0 || 
            amount == null || description == null || expenseDate == null) {
            return false;
        }
        
        // Create expense object with updated values
        Expense expense = new Expense();
        expense.setExpenseId(expenseId);
        expense.setUserId(userId);
        expense.setCategoryId(categoryId);
        expense.setAmount(amount);
        expense.setDescription(description);
        expense.setExpenseDate(expenseDate);
        
        // Update expense in database
        return expenseDAO.updateExpense(expense);
    }
    
    /**
     * Delete an expense
     */
    public boolean deleteExpense(int expenseId, int userId) {
        // Validate input
        if (expenseId <= 0 || userId <= 0) {
            return false;
        }
        
        // Delete expense from database
        return expenseDAO.deleteExpense(expenseId, userId);
    }
    
    /**
     * Get an expense by ID
     */
    public Expense getExpenseById(int expenseId, int userId) {
        // Validate input
        if (expenseId <= 0 || userId <= 0) {
            return null;
        }
        
        return expenseDAO.getExpenseById(expenseId, userId);
    }
    
    /**
     * Get all expenses for a user
     */
    public List<Expense> getExpensesByUser(int userId) {
        // Validate input
        if (userId <= 0) {
            return null;
        }
        
        return expenseDAO.getExpensesByUser(userId);
    }
    
    /**
     * Get expenses for a user within a date range
     */
    public List<Expense> getExpensesByUserAndDateRange(int userId, Date startDate, Date endDate) {
        // Validate input
        if (userId <= 0 || startDate == null || endDate == null) {
            return null;
        }
        
        return expenseDAO.getExpensesByUserAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * Get expenses for a user by category
     */
    public List<Expense> getExpensesByUserAndCategory(int userId, int categoryId) {
        // Validate input
        if (userId <= 0 || categoryId <= 0) {
            return null;
        }
        
        return expenseDAO.getExpensesByUserAndCategory(userId, categoryId);
    }
    
    /**
     * Get expense summary by category for a user within a date range
     */
    public List<Object[]> getExpenseSummaryByCategory(int userId, Date startDate, Date endDate) {
        // Validate input
        if (userId <= 0 || startDate == null || endDate == null) {
            return null;
        }
        
        return expenseDAO.getExpenseSummaryByCategory(userId, startDate, endDate);
    }
    
    /**
     * Get monthly expense totals for a user
     */
    public List<Object[]> getMonthlyExpenseTotals(int userId, int year) {
        // Validate input
        if (userId <= 0 || year <= 0) {
            return null;
        }
        
        return expenseDAO.getMonthlyExpenseTotals(userId, year);
    }
    
    /**
     * Get total expenses for a user within a date range
     */
    public BigDecimal getTotalExpenses(int userId, Date startDate, Date endDate) {
        // Validate input
        if (userId <= 0 || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }
        
        return expenseDAO.getTotalExpenses(userId, startDate, endDate);
    }
} 