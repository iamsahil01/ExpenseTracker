package com.expensetracker.view;

import com.expensetracker.util.DatabaseUtil;
import com.expensetracker.util.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Splash screen displayed when the application starts
 */
public class SplashScreen extends JWindow {
    
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Timer timer;
    private int progress = 0;
    
    /**
     * Constructor
     */
    public SplashScreen() {
        createUI();
        startProgress();
    }
    
    /**
     * Initialize the UI components
     */
    private void createUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(SwingUtils.PRIMARY_COLOR, 2));
        
        // Create a panel with gradient background
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(41, 128, 185),
                    0, getHeight(), new Color(44, 62, 80)
                );
                
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // App title
        JLabel titleLabel = new JLabel("Expense Tracker", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        
        // App version
        JLabel versionLabel = new JLabel("Version 1.0", SwingConstants.CENTER);
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionLabel.setForeground(new Color(200, 200, 200));
        
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(versionLabel, BorderLayout.SOUTH);
        
        // Create a placeholder icon panel instead of using an image
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setOpaque(false);
        
        // Create a label with a dollar sign as a placeholder
        JLabel dollarIconLabel = new JLabel("$", SwingConstants.CENTER);
        dollarIconLabel.setFont(new Font("Arial", Font.BOLD, 72));
        dollarIconLabel.setForeground(new Color(39, 174, 96));
        iconPanel.add(dollarIconLabel, BorderLayout.CENTER);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(new Color(39, 174, 96));
        progressBar.setBackground(new Color(44, 62, 80));
        progressBar.setPreferredSize(new Dimension(300, 8));
        
        // Status label
        statusLabel = new JLabel("Loading application...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(Color.WHITE);
        
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setOpaque(false);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add components to the content panel
        contentPanel.add(titlePanel, BorderLayout.NORTH);
        contentPanel.add(iconPanel, BorderLayout.CENTER);
        contentPanel.add(progressPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        
        // Set size and center
        setSize(400, 300);
        setLocationRelativeTo(null);
    }
    
    /**
     * Start the progress animation
     */
    private void startProgress() {
        timer = new Timer(30, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                progress += 1;
                progressBar.setValue(progress);
                
                // Update status text at certain points
                if (progress == 20) {
                    statusLabel.setText("Initializing components...");
                } else if (progress == 50) {
                    statusLabel.setText("Connecting to database...");
                    initializeDatabase();
                } else if (progress == 80) {
                    statusLabel.setText("Loading user interface...");
                } else if (progress >= 100) {
                    timer.stop();
                    SplashScreen.this.dispose();
                    showLoginView();
                }
            }
        });
        timer.start();
    }
    
    /**
     * Initialize the database connection
     */
    private void initializeDatabase() {
        try {
            DatabaseUtil.initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                "Error connecting to database: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
    
    /**
     * Show the login view
     */
    private void showLoginView() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            }
        });
    }
} 