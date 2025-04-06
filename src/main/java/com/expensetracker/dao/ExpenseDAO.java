package com.expensetracker.dao;

import com.expensetracker.model.Expense;
import com.expensetracker.util.DatabaseUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for Expense related database operations
 */
public class ExpenseDAO {
    
    /**
     * Add a new expense to the database
     */
    public boolean addExpense(Expense expense) {
        String sql = "INSERT INTO expenses (user_id, category_id, amount, description, expense_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, expense.getUserId());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setBigDecimal(3, expense.getAmount());
            pstmt.setString(4, expense.getDescription());
            pstmt.setDate(5, new java.sql.Date(expense.getExpenseDate().getTime()));
            
            System.out.println("Executing SQL: " + sql + " with parameters: " +
                              "userId=" + expense.getUserId() +
                              ", categoryId=" + expense.getCategoryId() +
                              ", amount=" + expense.getAmount() +
                              ", description=" + expense.getDescription() +
                              ", expenseDate=" + expense.getExpenseDate());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setExpenseId(rs.getInt(1));
                        System.out.println("Successfully added expense with ID: " + expense.getExpenseId());
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error adding expense: " + e.getMessage());
            
            // If there's a schema error, try to recreate the table structure
            if (e.getMessage().contains("Unknown column") || e.getMessage().contains("doesn't exist")) {
                System.out.println("Schema error detected. You may need to restart the application to rebuild the database.");
            }
            
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Update an existing expense in the database
     */
    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE expenses SET category_id = ?, amount = ?, description = ?, expense_date = ? WHERE expense_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, expense.getCategoryId());
            pstmt.setBigDecimal(2, expense.getAmount());
            pstmt.setString(3, expense.getDescription());
            pstmt.setDate(4, new java.sql.Date(expense.getExpenseDate().getTime()));
            pstmt.setInt(5, expense.getExpenseId());
            pstmt.setInt(6, expense.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Delete an expense from the database by ID
     */
    public boolean deleteExpense(int expenseId, int userId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ? AND user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, expenseId);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get an expense by ID
     */
    public Expense getExpenseById(int expenseId, int userId) {
        String sql = "SELECT e.*, c.name as category_name FROM expenses e " +
                     "JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.expense_id = ? AND e.user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, expenseId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractExpenseFromResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all expenses for a specific user
     */
    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> expenses = new ArrayList<>();
        
        // Try the more complete query first
        String sql = "SELECT e.*, c.name as category_name FROM expenses e " +
                     "JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.user_id = ? ORDER BY e.expense_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(extractExpenseFromResultSet(rs));
                }
                return expenses;
            }
            
        } catch (SQLException e) {
            // If the first query fails, try a simpler fallback query
            System.out.println("Error with join query: " + e.getMessage() + ". Trying fallback query.");
            
            String fallbackSql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC";
            
            try {
                Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(fallbackSql);
                
                pstmt.setInt(1, userId);
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Expense expense = new Expense();
                    expense.setExpenseId(rs.getInt("expense_id"));
                    expense.setUserId(rs.getInt("user_id"));
                    expense.setCategoryId(rs.getInt("category_id"));
                    expense.setAmount(rs.getBigDecimal("amount"));
                    expense.setDescription(rs.getString("description"));
                    expense.setExpenseDate(rs.getDate("expense_date"));
                    expense.setCreatedAt(rs.getTimestamp("created_at"));
                    expense.setCategoryName("Unknown"); // Since we couldn't join with categories
                    expenses.add(expense);
                }
                
                // Close resources manually
                rs.close();
                pstmt.close();
                conn.close();
                
            } catch (SQLException ex) {
                System.out.println("Error with fallback query: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
        
        return expenses;
    }
    
    /**
     * Get expenses for a specific user within a date range
     */
    public List<Expense> getExpensesByUserAndDateRange(int userId, Date startDate, Date endDate) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.*, c.name as category_name FROM expenses e " +
                     "JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.user_id = ? AND e.expense_date BETWEEN ? AND ? " +
                     "ORDER BY e.expense_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(extractExpenseFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    /**
     * Get expenses for a specific user by category
     */
    public List<Expense> getExpensesByUserAndCategory(int userId, int categoryId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT e.*, c.name as category_name FROM expenses e " +
                     "JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.user_id = ? AND e.category_id = ? " +
                     "ORDER BY e.expense_date DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, categoryId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    expenses.add(extractExpenseFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return expenses;
    }
    
    /**
     * Get expenses summary by category for a specific user within a date range
     */
    public List<Object[]> getExpenseSummaryByCategory(int userId, Date startDate, Date endDate) {
        List<Object[]> summary = new ArrayList<>();
        String sql = "SELECT c.name, SUM(e.amount) as total_amount " +
                     "FROM expenses e " +
                     "JOIN categories c ON e.category_id = c.category_id " +
                     "WHERE e.user_id = ? AND e.expense_date BETWEEN ? AND ? " +
                     "GROUP BY c.category_id, c.name " +
                     "ORDER BY total_amount DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("name"),
                        rs.getBigDecimal("total_amount")
                    };
                    summary.add(row);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return summary;
    }
    
    /**
     * Get monthly expense totals for a specific user
     */
    public List<Object[]> getMonthlyExpenseTotals(int userId, int year) {
        List<Object[]> summary = new ArrayList<>();
        String sql = "SELECT MONTH(e.expense_date) as month, SUM(e.amount) as total_amount " +
                     "FROM expenses e " +
                     "WHERE e.user_id = ? AND YEAR(e.expense_date) = ? " +
                     "GROUP BY MONTH(e.expense_date) " +
                     "ORDER BY month";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, year);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("month"),
                        rs.getBigDecimal("total_amount")
                    };
                    summary.add(row);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return summary;
    }
    
    /**
     * Get total expenses for a specific user within a date range
     */
    public BigDecimal getTotalExpenses(int userId, Date startDate, Date endDate) {
        String sql = "SELECT SUM(amount) as total FROM expenses " +
                     "WHERE user_id = ? AND expense_date BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setDate(2, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(3, new java.sql.Date(endDate.getTime()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Helper method to extract an Expense object from a ResultSet
     */
    private Expense extractExpenseFromResultSet(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setExpenseId(rs.getInt("expense_id"));
        expense.setUserId(rs.getInt("user_id"));
        expense.setCategoryId(rs.getInt("category_id"));
        expense.setCategoryName(rs.getString("category_name"));
        expense.setAmount(rs.getBigDecimal("amount"));
        expense.setDescription(rs.getString("description"));
        expense.setExpenseDate(rs.getDate("expense_date"));
        expense.setCreatedAt(rs.getTimestamp("created_at"));
        return expense;
    }
} 