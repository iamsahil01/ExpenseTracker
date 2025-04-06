package com.expensetracker.view;

import com.expensetracker.controller.UserController;
import com.expensetracker.model.User;
import com.expensetracker.util.SwingUtils;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * The login view of the expense tracker application
 */
public class LoginView extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    private UserController userController;
    
    /**
     * Constructor
     */
    public LoginView() {
        userController = new UserController();
        initializeUI();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set up the frame
        setTitle("Expense Tracker - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Create the main panel with a gradient background
        JPanel mainPanel = SwingUtils.createGradientPanel();
        mainPanel.setLayout(new BorderLayout(0, 20));
        
        // Create the title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setOpaque(false);
        
        // Add a text-based logo instead of an image
        JLabel logoLabel = new JLabel("$");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
        logoLabel.setForeground(SwingUtils.SECONDARY_COLOR);
        titlePanel.add(logoLabel);
        
        JLabel titleLabel = new JLabel("Expense Tracker");
        titleLabel.setFont(SwingUtils.TITLE_FONT);
        titleLabel.setForeground(SwingUtils.TEXT_COLOR);
        titlePanel.add(titleLabel);
        
        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setOpaque(false);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        
        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome! Please sign in to continue");
        welcomeLabel.setFont(SwingUtils.SUBTITLE_FONT);
        welcomeLabel.setForeground(SwingUtils.TEXT_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        formPanel.add(welcomeLabel, constraints);
        
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(SwingUtils.REGULAR_FONT);
        usernameLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        formPanel.add(usernameLabel, constraints);
        
        usernameField = SwingUtils.createTextField(20);
        constraints.gridx = 1;
        constraints.gridy = 1;
        formPanel.add(usernameField, constraints);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(SwingUtils.REGULAR_FONT);
        passwordLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 2;
        formPanel.add(passwordLabel, constraints);
        
        passwordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 2;
        formPanel.add(passwordField, constraints);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        loginButton = SwingUtils.createButton("Login", SwingUtils.PRIMARY_COLOR);
        registerButton = SwingUtils.createButton("Register", SwingUtils.SECONDARY_COLOR);
        
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        formPanel.add(buttonPanel, constraints);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Add event listeners
        loginButton.addActionListener(this::login);
        registerButton.addActionListener(this::openRegistrationView);
        
        // Set enter key to trigger login
        getRootPane().setDefaultButton(loginButton);
    }
    
    /**
     * Handle login action
     */
    private void login(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            SwingUtils.showErrorMessage(this, "Login Error", "Please enter both username and password");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        try {
            // Authenticate user
            User user = userController.authenticateUser(username, password);
            
            if (user != null) {
                // Open the main dashboard
                SwingUtils.showInfoMessage(this, "Login Successful", "Welcome " + user.getUsername() + "!");
                openDashboard(user);
            } else {
                SwingUtils.showErrorMessage(this, "Login Failed", 
                    "Invalid username or password. Please try again or register a new account.");
                passwordField.setText("");
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            e.printStackTrace();
            SwingUtils.showErrorMessage(this, "Login Error", 
                "An error occurred while logging in: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * Open the registration view
     */
    private void openRegistrationView(ActionEvent event) {
        this.setVisible(false);
        RegistrationView registrationView = new RegistrationView(this);
        registrationView.setVisible(true);
    }
    
    /**
     * Open the dashboard with the logged in user
     */
    private void openDashboard(User user) {
        this.dispose();
        DashboardView dashboardView = new DashboardView(user);
        dashboardView.setVisible(true);
    }
    
    /**
     * Set the username field value
     */
    public void setUsername(String username) {
        if (usernameField != null) {
            usernameField.setText(username);
            // Focus on the password field
            if (passwordField != null) {
                passwordField.requestFocus();
            }
        }
    }
} 