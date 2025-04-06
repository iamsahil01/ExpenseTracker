package com.expensetracker.view;

import com.expensetracker.controller.CategoryController;
import com.expensetracker.controller.ExpenseController;
import com.expensetracker.model.Category;
import com.expensetracker.model.User;
import com.expensetracker.util.SwingUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Dialog for adding or editing expenses
 */
public class AddExpenseDialog extends JDialog {
    
    private JTextField amountField;
    private JTextField descriptionField;
    private JTextField dateField;
    private JComboBox<Category> categoryComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    private User currentUser;
    private ExpenseController expenseController;
    private CategoryController categoryController;
    private int expenseId = -1; // -1 means new expense, otherwise editing existing expense
    
    /**
     * Constructor for adding a new expense
     */
    public AddExpenseDialog(Frame parent, User user) {
        super(parent, "Add Expense", true);
        this.currentUser = user;
        this.expenseController = new ExpenseController();
        this.categoryController = new CategoryController();
        initializeUI();
    }
    
    /**
     * Constructor for editing an existing expense
     */
    public AddExpenseDialog(Frame parent, User user, int expenseId) {
        super(parent, "Edit Expense", true);
        this.currentUser = user;
        this.expenseController = new ExpenseController();
        this.categoryController = new CategoryController();
        this.expenseId = expenseId;
        initializeUI();
        loadExpenseData();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set up the dialog
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        // Create the main panel with gradient background
        JPanel mainPanel = SwingUtils.createGradientPanel();
        mainPanel.setLayout(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Add title at the top
        JLabel titleLabel = new JLabel(expenseId > 0 ? "Edit Expense" : "Add New Expense", SwingConstants.CENTER);
        titleLabel.setFont(SwingUtils.SUBTITLE_FONT);
        titleLabel.setForeground(SwingUtils.TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 10, 8, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        
        // Amount field
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(SwingUtils.REGULAR_FONT);
        amountLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.weightx = 0.3;
        formPanel.add(amountLabel, constraints);
        
        amountField = SwingUtils.createTextField(10);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 0.7;
        formPanel.add(amountField, constraints);
        
        // Category dropdown
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(SwingUtils.REGULAR_FONT);
        categoryLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0.3;
        formPanel.add(categoryLabel, constraints);
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(SwingUtils.REGULAR_FONT);
        categoryComboBox.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        List<Category> categories = categoryController.getCategoriesByUser(currentUser.getUserId());
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
        
        categoryComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category category) {
                    setText(category.getName());
                }
                return this;
            }
        });
        
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.7;
        formPanel.add(categoryComboBox, constraints);
        
        // Description field
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(SwingUtils.REGULAR_FONT);
        descriptionLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        formPanel.add(descriptionLabel, constraints);
        
        descriptionField = SwingUtils.createTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 0.7;
        formPanel.add(descriptionField, constraints);
        
        // Date field
        JLabel dateLabel = new JLabel("Date (yyyy-MM-dd):");
        dateLabel.setFont(SwingUtils.REGULAR_FONT);
        dateLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 0.3;
        formPanel.add(dateLabel, constraints);
        
        dateField = SwingUtils.createTextField(10);
        dateField.setText(SwingUtils.formatDate(new Date())); // Default to today
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.weightx = 0.7;
        formPanel.add(dateField, constraints);
        
        // Add form panel to main panel
        JPanel formContainerPanel = new JPanel(new BorderLayout());
        formContainerPanel.setOpaque(false);
        formContainerPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(formContainerPanel, BorderLayout.CENTER);
        
        // Create the button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        saveButton = SwingUtils.createButton("Save", SwingUtils.SECONDARY_COLOR);
        cancelButton = SwingUtils.createButton("Cancel", SwingUtils.ACCENT_COLOR);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to dialog
        add(mainPanel);
        
        // Add action listeners
        saveButton.addActionListener(this::saveExpense);
        cancelButton.addActionListener(this::closeDialog);
        
        // Set enter key to trigger save
        getRootPane().setDefaultButton(saveButton);
    }
    
    /**
     * Load expense data for editing
     */
    private void loadExpenseData() {
        if (expenseId <= 0) {
            return;
        }
        
        // Get the expense from the database
        com.expensetracker.model.Expense expense = expenseController.getExpenseById(expenseId, currentUser.getUserId());
        
        if (expense != null) {
            // Populate the fields with expense data
            amountField.setText(expense.getAmount().toString());
            descriptionField.setText(expense.getDescription());
            dateField.setText(SwingUtils.formatDate(expense.getExpenseDate()));
            
            // Select the correct category
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category category = categoryComboBox.getItemAt(i);
                if (category.getCategoryId() == expense.getCategoryId()) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
    
    /**
     * Save the expense to the database
     */
    private void saveExpense(ActionEvent event) {
        try {
            // Show wait cursor during validation and saving
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            // Validate amount
            if (amountField.getText().trim().isEmpty()) {
                SwingUtils.showErrorMessage(this, "Validation Error", "Please enter an amount.");
                amountField.requestFocus();
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    SwingUtils.showErrorMessage(this, "Validation Error", "Amount must be greater than zero.");
                    amountField.requestFocus();
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
            } catch (NumberFormatException e) {
                SwingUtils.showErrorMessage(this, "Validation Error", "Please enter a valid number for amount.");
                amountField.requestFocus();
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // Validate category
            Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
            if (selectedCategory == null) {
                SwingUtils.showErrorMessage(this, "Validation Error", "Please select a category.");
                categoryComboBox.requestFocus();
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // Validate description
            String description = descriptionField.getText().trim();
            if (description.isEmpty()) {
                SwingUtils.showErrorMessage(this, "Validation Error", "Please enter a description.");
                descriptionField.requestFocus();
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            // Validate date
            Date expenseDate;
            try {
                expenseDate = SwingUtils.DATE_FORMAT.parse(dateField.getText().trim());
            } catch (ParseException e) {
                SwingUtils.showErrorMessage(this, "Validation Error", "Please enter a valid date in the format yyyy-MM-dd.");
                dateField.requestFocus();
                setCursor(Cursor.getDefaultCursor());
                return;
            }
            
            boolean success;
            
            // Use a string template for more readable error/success messages
            String action = expenseId > 0 ? "update" : "add";
            
            if (expenseId > 0) {
                // Update existing expense
                success = expenseController.updateExpense(
                    expenseId, 
                    currentUser.getUserId(), 
                    selectedCategory.getCategoryId(), 
                    amount, 
                    description, 
                    expenseDate
                );
            } else {
                // Add new expense
                success = expenseController.addExpense(
                    currentUser.getUserId(), 
                    selectedCategory.getCategoryId(), 
                    amount, 
                    description, 
                    expenseDate
                );
            }
            
            setCursor(Cursor.getDefaultCursor());
            
            if (success) {
                SwingUtils.showInfoMessage(this, "Success", "Expense " + (expenseId > 0 ? "updated" : "added") + " successfully.");
                dispose();
            } else {
                SwingUtils.showErrorMessage(this, "Error", "Failed to " + action + " expense.");
            }
            
        } catch (Exception e) {
            setCursor(Cursor.getDefaultCursor());
            SwingUtils.showErrorMessage(this, "Error", "An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Close the dialog
     */
    private void closeDialog(ActionEvent event) {
        dispose();
    }
} 