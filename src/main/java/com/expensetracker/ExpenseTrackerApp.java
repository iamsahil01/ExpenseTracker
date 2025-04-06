package com.expensetracker;

import com.expensetracker.util.DatabaseUtil;
import com.expensetracker.view.SplashScreen;

import javax.swing.*;
import java.awt.Font;
import java.sql.SQLException;

/**
 * Main application class for the Expense Tracker System
 */
public class ExpenseTrackerApp {
    
    public static void main(String[] args) {
        try {
            // Set look and feel to system default
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // Set some global UI properties for a modern look
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            
            // Apply custom font settings
            setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));
            
            // Start the application with the splash screen
            SwingUtilities.invokeLater(() -> {
                try {
                    // Initialize database and ensure tables exist
                    DatabaseUtil.initializeDatabase();
                    
                    // Start the app
                    new SplashScreen();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null,
                        "Database error: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            String errorMessage = "Error starting application: " + e.getMessage();
            JOptionPane.showMessageDialog(null, 
                errorMessage,
                "Application Error", 
                JOptionPane.ERROR_MESSAGE);
            System.err.println(errorMessage);
            e.printStackTrace();
        }
    }
    
    /**
     * Set global UI font
     */
    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
} 