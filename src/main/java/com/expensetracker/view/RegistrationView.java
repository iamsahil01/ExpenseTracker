package com.expensetracker.view;

import com.expensetracker.controller.UserController;
import com.expensetracker.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Registration view for new users
 */
public class RegistrationView extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private JButton registerButton;
    private JButton cancelButton;
    
    private UserController userController;
    private LoginView parentView;
    
    /**
     * Constructor
     */
    public RegistrationView(LoginView parentView) {
        this.parentView = parentView;
        userController = new UserController();
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set up the frame
        setTitle("Expense Tracker - Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(450, 350);
        setLocationRelativeTo(null);
        
        // Create the main panel with a nice gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                
                // Create a gradient paint from top to bottom
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255), 
                    0, getHeight(), new Color(176, 224, 230)
                );
                
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        mainPanel.setLayout(new BorderLayout());
        
        // Create the title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        titlePanel.add(titleLabel);
        
        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(new Color(44, 62, 80));
        constraints.gridx = 0;
        constraints.gridy = 0;
        formPanel.add(usernameLabel, constraints);
        
        usernameField = SwingUtils.createTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 0;
        formPanel.add(usernameField, constraints);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(new Color(44, 62, 80));
        constraints.gridx = 0;
        constraints.gridy = 1;
        formPanel.add(passwordLabel, constraints);
        
        passwordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        formPanel.add(passwordField, constraints);
        
        // Confirm Password field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setForeground(new Color(44, 62, 80));
        constraints.gridx = 0;
        constraints.gridy = 2;
        formPanel.add(confirmPasswordLabel, constraints);
        
        confirmPasswordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 2;
        formPanel.add(confirmPasswordField, constraints);
        
        // Email field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(new Color(44, 62, 80));
        constraints.gridx = 0;
        constraints.gridy = 3;
        formPanel.add(emailLabel, constraints);
        
        emailField = SwingUtils.createTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 3;
        formPanel.add(emailField, constraints);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        
        registerButton = SwingUtils.createButton("Register", SwingUtils.SECONDARY_COLOR);
        cancelButton = SwingUtils.createButton("Cancel", SwingUtils.ACCENT_COLOR);
        
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        formPanel.add(buttonPanel, constraints);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Add event listeners
        registerButton.addActionListener(this::register);
        cancelButton.addActionListener(this::cancel);
    }
    
    /**
     * Handle registration action
     */
    private void register(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
        String email = emailField.getText().trim();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty()) {
            SwingUtils.showErrorMessage(this, "Registration Error", "Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            SwingUtils.showErrorMessage(this, "Registration Error", "Passwords do not match");
            return;
        }
        
        if (!isValidEmail(email)) {
            SwingUtils.showErrorMessage(this, "Registration Error", "Please enter a valid email address");
            return;
        }
        
        try {
            // Register user
            boolean success = userController.registerUser(username, password, email);
            
            if (success) {
                // Show success message with login info
                SwingUtils.showInfoMessage(this, "Registration Successful", 
                    "Your account has been created successfully.\n\n" +
                    "Username: " + username + "\n" +
                    "Please remember your password.");
                
                // Auto-populate login screen with the new username
                returnToLoginWithUsername(username);
            } else {
                SwingUtils.showErrorMessage(this, "Registration Failed", 
                    "Username already exists or a database error occurred.\n" +
                    "Please try a different username.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showErrorMessage(this, "Error", 
                "An error occurred during registration: " + e.getMessage());
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Cancel registration and return to login
     */
    private void cancel(ActionEvent event) {
        returnToLogin();
    }
    
    /**
     * Return to login screen with username filled in
     */
    private void returnToLoginWithUsername(String username) {
        this.dispose();
        parentView.setUsername(username);
        parentView.setVisible(true);
    }
    
    /**
     * Return to login screen
     */
    private void returnToLogin() {
        this.dispose();
        parentView.setVisible(true);
    }
} 