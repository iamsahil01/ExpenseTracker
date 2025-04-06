package com.expensetracker.view;

import com.expensetracker.controller.CategoryController;
import com.expensetracker.controller.ExpenseController;
import com.expensetracker.controller.UserController;
import com.expensetracker.model.Category;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;
import com.expensetracker.util.SwingUtils;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Calendar;
import java.util.prefs.Preferences;
import java.text.SimpleDateFormat;

/**
 * Main dashboard view for the expense tracker application
 */
public class DashboardView extends JFrame {
    
    private User currentUser;
    private ExpenseController expenseController;
    private CategoryController categoryController;
    
    private JTabbedPane tabbedPane;
    private JTable expensesTable;
    private DefaultTableModel tableModel;
    private JButton addExpenseButton;
    private JButton editExpenseButton;
    private JButton deleteExpenseButton;
    private JComboBox<Category> categoryFilterComboBox;
    private JLabel totalExpensesLabel;
    
    /**
     * Constructor
     */
    public DashboardView(User user) {
        this.currentUser = user;
        this.expenseController = new ExpenseController();
        this.categoryController = new CategoryController();
        initializeUI();
        loadExpenses();
    }
    
    /**
     * Initialize the UI components
     */
    private void initializeUI() {
        // Set up the frame
        setTitle("Expense Tracker - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        // Create main container with subtle background
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        // Create header panel with user info
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SwingUtils.PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getUsername() + "!");
        welcomeLabel.setFont(SwingUtils.SUBTITLE_FONT);
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JLabel dateLabel = new JLabel("Today: " + SwingUtils.formatDate(new Date()));
        dateLabel.setFont(SwingUtils.REGULAR_FONT);
        dateLabel.setForeground(Color.WHITE);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Create the tabbed pane with modern styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(SwingUtils.REGULAR_FONT);
        tabbedPane.setBackground(SwingUtils.BACKGROUND_COLOR);
        tabbedPane.setForeground(SwingUtils.TEXT_COLOR);
        
        // Create the expenses panel
        JPanel expensesPanel = createExpensesPanel();
        tabbedPane.addTab("Expenses", expensesPanel);
        
        // Create the reports panel
        JPanel reportsPanel = createReportsPanel();
        tabbedPane.addTab("Reports", reportsPanel);
        
        // Create the categories panel
        JPanel categoriesPanel = createCategoriesPanel();
        tabbedPane.addTab("Categories", categoriesPanel);
        
        // Create the settings panel
        JPanel settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Settings", settingsPanel);
        
        // Add the tabbed pane to the main container
        mainContainer.add(tabbedPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(SwingUtils.LIGHT_ACCENT);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(SwingUtils.SMALL_FONT);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        
        mainContainer.add(statusPanel, BorderLayout.SOUTH);
        
        // Add the main container to the frame
        add(mainContainer);
        
        // Create the menu bar
        createMenuBar();
    }
    
    /**
     * Create the expenses panel
     */
    private JPanel createExpensesPanel() {
        JPanel panel = SwingUtils.createTitledPanel("Your Expenses");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(SwingUtils.BACKGROUND_COLOR);
        panel.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            panel.getBorder()
        ));
        
        // Filter panel at top
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JLabel categoryFilterLabel = new JLabel("Filter by Category:");
        categoryFilterLabel.setFont(SwingUtils.REGULAR_FONT);
        categoryFilterLabel.setForeground(SwingUtils.TEXT_COLOR);
        filterPanel.add(categoryFilterLabel);
        
        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.setFont(SwingUtils.REGULAR_FONT);
        categoryFilterComboBox.addItem(new Category(0, "All Categories", "", 0, false));
        
        // Add the categories to the filter
        List<Category> categories = categoryController.getCategoriesByUser(currentUser.getUserId());
        for (Category category : categories) {
            categoryFilterComboBox.addItem(category);
        }
        
        // Custom renderer to show only category name
        categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });
        
        filterPanel.add(categoryFilterComboBox);
        
        JButton applyFilterButton = SwingUtils.createButton("Apply Filter", SwingUtils.PRIMARY_COLOR);
        applyFilterButton.addActionListener(this::loadExpensesFromAction);
        filterPanel.add(applyFilterButton);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        
        // Create the expense table
        String[] columnNames = {"ID", "Date", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // Make the table non-editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;  // ID column
                }
                return String.class;  // All other columns are Strings (including formatted date and amount)
            }
        };
        
        expensesTable = new JTable(tableModel);
        expensesTable.setFont(SwingUtils.REGULAR_FONT);
        expensesTable.getTableHeader().setFont(SwingUtils.REGULAR_FONT);
        expensesTable.setRowHeight(25);
        expensesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        expensesTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        expensesTable.getColumnModel().getColumn(0).setMaxWidth(50);  // ID column
        expensesTable.getColumnModel().getColumn(1).setPreferredWidth(120);  // Date column
        expensesTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Category column
        expensesTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Amount column
        expensesTable.getColumnModel().getColumn(4).setPreferredWidth(300);  // Description column
        
        // Set custom cell renderer for the amount column to right-align
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        expensesTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        
        // Hide the ID column
        expensesTable.getColumnModel().getColumn(0).setMinWidth(0);
        expensesTable.getColumnModel().getColumn(0).setMaxWidth(0);
        expensesTable.getColumnModel().getColumn(0).setWidth(0);
        
        JScrollPane scrollPane = new JScrollPane(expensesTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Bottom panel with action buttons and total
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionButtonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        addExpenseButton = SwingUtils.createButton("Add Expense", SwingUtils.SECONDARY_COLOR);
        editExpenseButton = SwingUtils.createButton("Edit Expense", SwingUtils.PRIMARY_COLOR);
        deleteExpenseButton = SwingUtils.createButton("Delete Expense", SwingUtils.ACCENT_COLOR);
        
        addExpenseButton.addActionListener(this::openAddExpenseDialog);
        editExpenseButton.addActionListener(this::openEditExpenseDialog);
        deleteExpenseButton.addActionListener(this::deleteExpense);
        
        actionButtonPanel.add(addExpenseButton);
        actionButtonPanel.add(editExpenseButton);
        actionButtonPanel.add(deleteExpenseButton);
        
        bottomPanel.add(actionButtonPanel, BorderLayout.WEST);
        
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summaryPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JLabel totalLabel = new JLabel("Total: ");
        totalLabel.setFont(new Font(SwingUtils.REGULAR_FONT.getName(), Font.BOLD, 16));
        totalLabel.setForeground(SwingUtils.TEXT_COLOR);
        
        totalExpensesLabel = new JLabel(getCurrencySymbolFromSelection(getUserPreference("currency", "USD ($)")) + " 0.00");
        totalExpensesLabel.setFont(new Font(SwingUtils.REGULAR_FONT.getName(), Font.BOLD, 16));
        totalExpensesLabel.setForeground(SwingUtils.ACCENT_COLOR);
        
        summaryPanel.add(totalLabel);
        summaryPanel.add(totalExpensesLabel);
        
        bottomPanel.add(summaryPanel, BorderLayout.EAST);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Create the reports panel
     */
    private JPanel createReportsPanel() {
        JPanel panel = SwingUtils.createTitledPanel("Expense Reports");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(SwingUtils.BACKGROUND_COLOR);
        panel.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            panel.getBorder()
        ));
        
        // Add a toolbar at the top for report options
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbarPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JLabel reportTypeLabel = new JLabel("Report Type:");
        reportTypeLabel.setFont(SwingUtils.REGULAR_FONT);
        reportTypeLabel.setForeground(SwingUtils.TEXT_COLOR);
        toolbarPanel.add(reportTypeLabel);
        
        String[] reportTypes = {"Monthly Summary", "Category Breakdown", "Yearly Comparison"};
        JComboBox<String> reportTypeCombo = new JComboBox<>(reportTypes);
        reportTypeCombo.setFont(SwingUtils.REGULAR_FONT);
        toolbarPanel.add(reportTypeCombo);
        
        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(SwingUtils.REGULAR_FONT);
        periodLabel.setForeground(SwingUtils.TEXT_COLOR);
        toolbarPanel.add(periodLabel);
        
        String[] periods = {"This Month", "Last Month", "Last 3 Months", "This Year", "Custom..."};
        JComboBox<String> periodCombo = new JComboBox<>(periods);
        periodCombo.setFont(SwingUtils.REGULAR_FONT);
        toolbarPanel.add(periodCombo);
        
        // Date range panel (initially hidden)
        JPanel dateRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateRangePanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        dateRangePanel.setVisible(false);
        
        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(SwingUtils.REGULAR_FONT);
        dateRangePanel.add(fromLabel);
        
        JTextField fromDateField = SwingUtils.createTextField(10);
        fromDateField.setText(SwingUtils.formatDate(getStartDateForPeriod("This Month")));
        dateRangePanel.add(fromDateField);
        
        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(SwingUtils.REGULAR_FONT);
        dateRangePanel.add(toLabel);
        
        JTextField toDateField = SwingUtils.createTextField(10);
        toDateField.setText(SwingUtils.formatDate(new Date()));
        dateRangePanel.add(toDateField);
        
        toolbarPanel.add(dateRangePanel);
        
        // Show date range panel if "Custom..." is selected
        periodCombo.addActionListener(e -> {
            String selectedPeriod = (String) periodCombo.getSelectedItem();
            if ("Custom...".equals(selectedPeriod)) {
                dateRangePanel.setVisible(true);
            } else {
                dateRangePanel.setVisible(false);
                // Update the hidden date fields with the selected period's date range
                fromDateField.setText(SwingUtils.formatDate(getStartDateForPeriod(selectedPeriod)));
                toDateField.setText(SwingUtils.formatDate(new Date()));
            }
            toolbarPanel.revalidate();
            toolbarPanel.repaint();
        });
        
        JButton generateButton = SwingUtils.createButton("Generate Report", SwingUtils.PRIMARY_COLOR);
        toolbarPanel.add(generateButton);
        
        panel.add(toolbarPanel, BorderLayout.NORTH);
        
        // Content panel for chart and data
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT));
        
        // Chart panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Default chart message
        JLabel chartPlaceholder = new JLabel("Select report type and period, then click 'Generate Report'", SwingConstants.CENTER);
        chartPlaceholder.setFont(SwingUtils.REGULAR_FONT);
        chartPlaceholder.setForeground(SwingUtils.TEXT_COLOR);
        chartPanel.add(chartPlaceholder, BorderLayout.CENTER);
        
        contentPanel.add(chartPanel, BorderLayout.CENTER);
        
        // Data panel at the bottom of the content panel
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(Color.WHITE);
        dataPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, SwingUtils.LIGHT_ACCENT),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Table for displaying report data
        String[] columns = {"Category", "Amount", "Percentage"};
        DefaultTableModel reportTableModel = new DefaultTableModel(columns, 0);
        JTable reportTable = new JTable(reportTableModel);
        SwingUtils.applyTableStyling(reportTable);
        
        JScrollPane tableScrollPane = new JScrollPane(reportTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 150));
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        dataPanel.add(tableScrollPane, BorderLayout.CENTER);
        contentPanel.add(dataPanel, BorderLayout.SOUTH);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        // Export options at the bottom
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        exportPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JButton exportPdfButton = SwingUtils.createButton("Export PDF", SwingUtils.SECONDARY_COLOR);
        JButton exportCsvButton = SwingUtils.createButton("Export CSV", SwingUtils.SECONDARY_COLOR);
        
        exportPanel.add(exportPdfButton);
        exportPanel.add(exportCsvButton);
        
        panel.add(exportPanel, BorderLayout.SOUTH);
        
        // Action to generate reports
        generateButton.addActionListener(e -> {
            String reportType = (String) reportTypeCombo.getSelectedItem();
            Date startDate;
            Date endDate;
            
            try {
                if ("Custom...".equals(periodCombo.getSelectedItem())) {
                    startDate = SwingUtils.DATE_FORMAT.parse(fromDateField.getText());
                    endDate = SwingUtils.DATE_FORMAT.parse(toDateField.getText());
                } else {
                    startDate = getStartDateForPeriod((String) periodCombo.getSelectedItem());
                    endDate = new Date(); // Current date
                }
                
                // Clear existing chart and data
                chartPanel.removeAll();
                reportTableModel.setRowCount(0);
                
                // Generate the report based on type
                if ("Category Breakdown".equals(reportType)) {
                    generateCategoryBreakdownReport(chartPanel, reportTableModel, startDate, endDate);
                } else if ("Monthly Summary".equals(reportType)) {
                    generateMonthlySummaryReport(chartPanel, reportTableModel, startDate, endDate);
                } else if ("Yearly Comparison".equals(reportType)) {
                    generateYearlyComparisonReport(chartPanel, reportTableModel, startDate, endDate);
                }
                
                // Refresh UI
                chartPanel.revalidate();
                chartPanel.repaint();
                reportTable.revalidate();
                reportTable.repaint();
                
            } catch (Exception ex) {
                SwingUtils.showErrorMessage(DashboardView.this, "Report Error", 
                    "Error generating report: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Export actions
        exportCsvButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(DashboardView.this, 
                "CSV Export functionality will be implemented in the next version.", 
                "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });
        
        exportPdfButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(DashboardView.this, 
                "PDF Export functionality will be implemented in the next version.", 
                "Feature Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        });
        
        return panel;
    }
    
    /**
     * Generate a pie chart for category breakdown
     */
    private void generateCategoryBreakdownReport(JPanel chartPanel, DefaultTableModel tableModel, 
                                               Date startDate, Date endDate) {
        // Get expense summary by category from the controller
        List<Object[]> categorySummary = expenseController.getExpenseSummaryByCategory(
            currentUser.getUserId(), startDate, endDate);
        
        if (categorySummary.isEmpty()) {
            JLabel noDataLabel = new JLabel("No expense data available for the selected period", SwingConstants.CENTER);
            noDataLabel.setFont(SwingUtils.REGULAR_FONT);
            chartPanel.add(noDataLabel, BorderLayout.CENTER);
            return;
        }
        
        // Create dataset for pie chart
        org.jfree.data.general.DefaultPieDataset<String> dataset = new org.jfree.data.general.DefaultPieDataset<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Add data to dataset
        for (Object[] row : categorySummary) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            dataset.setValue(category, amount.doubleValue());
            totalAmount = totalAmount.add(amount);
        }
        
        // Create chart
        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
            "Expense Distribution by Category", 
            dataset, 
            true,  // legend
            true,  // tooltips
            false  // URLs
        );
        
        // Customize chart
        org.jfree.chart.plot.PiePlot<?> plot = (org.jfree.chart.plot.PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(SwingUtils.SMALL_FONT);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        
        // Create chart panel
        org.jfree.chart.ChartPanel chartPanel2 = new org.jfree.chart.ChartPanel(chart);
        chartPanel2.setPreferredSize(new Dimension(500, 300));
        chartPanel2.setBackground(Color.WHITE);
        chartPanel.add(chartPanel2, BorderLayout.CENTER);
        
        // Populate table model with data
        for (Object[] row : categorySummary) {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            // Calculate percentage
            double percentage = amount.doubleValue() * 100 / totalAmount.doubleValue();
            String percentageStr = String.format("%.2f%%", percentage);
            
            tableModel.addRow(new Object[]{
                category,
                SwingUtils.formatCurrency(amount.doubleValue()),
                percentageStr
            });
        }
        
        // Add total row
        tableModel.addRow(new Object[]{
            "TOTAL",
            SwingUtils.formatCurrency(totalAmount.doubleValue()),
            "100.00%"
        });
    }
    
    /**
     * Generate a bar chart for monthly summary
     */
    private void generateMonthlySummaryReport(JPanel chartPanel, DefaultTableModel tableModel, 
                                            Date startDate, Date endDate) {
        // Calculate year based on the date range
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int year = calendar.get(Calendar.YEAR);
        
        // Get monthly expense totals
        List<Object[]> monthlyTotals = expenseController.getMonthlyExpenseTotals(
            currentUser.getUserId(), year);
        
        if (monthlyTotals.isEmpty()) {
            JLabel noDataLabel = new JLabel("No expense data available for " + year, SwingConstants.CENTER);
            noDataLabel.setFont(SwingUtils.REGULAR_FONT);
            chartPanel.add(noDataLabel, BorderLayout.CENTER);
            return;
        }
        
        // Create dataset for bar chart
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        BigDecimal yearlyTotal = BigDecimal.ZERO;
        
        // Convert the data for the chart
        for (Object[] row : monthlyTotals) {
            int month = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            dataset.setValue(amount.doubleValue(), "Expenses", monthNames[month - 1]);
            yearlyTotal = yearlyTotal.add(amount);
        }
        
        // Create chart
        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            "Monthly Expenses for " + year,
            "Month",
            "Amount",
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Customize chart
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(SwingUtils.LIGHT_ACCENT);
        
        // Create chart panel
        org.jfree.chart.ChartPanel chartPanel2 = new org.jfree.chart.ChartPanel(chart);
        chartPanel2.setPreferredSize(new Dimension(500, 300));
        chartPanel2.setBackground(Color.WHITE);
        chartPanel.add(chartPanel2, BorderLayout.CENTER);
        
        // Populate table model with data
        for (Object[] row : monthlyTotals) {
            int month = (int) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            
            // Calculate percentage
            double percentage = amount.doubleValue() * 100 / yearlyTotal.doubleValue();
            String percentageStr = String.format("%.2f%%", percentage);
            
            tableModel.addRow(new Object[]{
                monthNames[month - 1] + " " + year,
                SwingUtils.formatCurrency(amount.doubleValue()),
                percentageStr
            });
        }
        
        // Add total row
        tableModel.addRow(new Object[]{
            "TOTAL " + year,
            SwingUtils.formatCurrency(yearlyTotal.doubleValue()),
            "100.00%"
        });
    }
    
    /**
     * Generate a line chart for yearly comparison
     */
    private void generateYearlyComparisonReport(JPanel chartPanel, DefaultTableModel tableModel, 
                                              Date startDate, Date endDate) {
        // Calculate years range
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        int startYear = calendar.get(Calendar.YEAR);
        
        calendar.setTime(endDate);
        int endYear = calendar.get(Calendar.YEAR);
        
        // Create dataset for line chart
        org.jfree.data.category.DefaultCategoryDataset dataset = new org.jfree.data.category.DefaultCategoryDataset();
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        BigDecimal grandTotal = BigDecimal.ZERO;
        
        // Get data for each year
        for (int year = startYear; year <= endYear; year++) {
            List<Object[]> monthlyTotals = expenseController.getMonthlyExpenseTotals(
                currentUser.getUserId(), year);
            
            BigDecimal yearlyTotal = BigDecimal.ZERO;
            
            // For table data, store monthly values
            BigDecimal[] monthlyValues = new BigDecimal[12];
            for (int i = 0; i < 12; i++) {
                monthlyValues[i] = BigDecimal.ZERO;
            }
            
            // Add data points to dataset
            for (Object[] row : monthlyTotals) {
                int month = (int) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                dataset.setValue(amount.doubleValue(), String.valueOf(year), monthNames[month - 1]);
                yearlyTotal = yearlyTotal.add(amount);
                
                // Store for table
                monthlyValues[month - 1] = amount;
            }
            
            // Add yearly total to table
            tableModel.addRow(new Object[]{
                year + " Total",
                SwingUtils.formatCurrency(yearlyTotal.doubleValue()),
                "-"
            });
            
            grandTotal = grandTotal.add(yearlyTotal);
        }
        
        // Create chart
        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
            "Yearly Expense Comparison",
            "Month",
            "Amount",
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            true,
            true,
            false
        );
        
        // Customize chart
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinePaint(SwingUtils.LIGHT_ACCENT);
        
        // Create chart panel
        org.jfree.chart.ChartPanel chartPanel2 = new org.jfree.chart.ChartPanel(chart);
        chartPanel2.setPreferredSize(new Dimension(500, 300));
        chartPanel2.setBackground(Color.WHITE);
        chartPanel.add(chartPanel2, BorderLayout.CENTER);
        
        // Add grand total row
        tableModel.addRow(new Object[]{
            "GRAND TOTAL",
            SwingUtils.formatCurrency(grandTotal.doubleValue()),
            "100.00%"
        });
    }
    
    /**
     * Calculate start date based on selected period
     */
    private Date getStartDateForPeriod(String period) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        switch (period) {
            case "This Month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Last Month":
                calendar.add(Calendar.MONTH, -1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Last 3 Months":
                calendar.add(Calendar.MONTH, -3);
                break;
            case "This Year":
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                break;
            default:
                // Default to this month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        
        return calendar.getTime();
    }
    
    /**
     * Create the categories panel
     */
    private JPanel createCategoriesPanel() {
        JPanel panel = SwingUtils.createTitledPanel("Expense Categories");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(SwingUtils.BACKGROUND_COLOR);
        panel.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            panel.getBorder()
        ));
        
        // Actions toolbar
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JButton addCategoryButton = SwingUtils.createButton("Add Category", SwingUtils.SECONDARY_COLOR);
        JButton editCategoryButton = SwingUtils.createButton("Edit Category", SwingUtils.PRIMARY_COLOR);
        JButton deleteCategoryButton = SwingUtils.createButton("Delete Category", SwingUtils.ACCENT_COLOR);
        
        // Add event listeners
        addCategoryButton.addActionListener(this::addCategory);
        editCategoryButton.addActionListener(this::editCategory);
        deleteCategoryButton.addActionListener(this::deleteCategory);
        
        actionsPanel.add(addCategoryButton);
        actionsPanel.add(editCategoryButton);
        actionsPanel.add(deleteCategoryButton);
        
        panel.add(actionsPanel, BorderLayout.NORTH);
        
        // Category list in the center with two-column layout
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        // Category list on the left
        JPanel categoryListPanel = new JPanel(new BorderLayout());
        categoryListPanel.setBackground(Color.WHITE);
        categoryListPanel.setBorder(BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT));
        
        JLabel categoryListTitle = new JLabel("Your Categories", SwingConstants.CENTER);
        categoryListTitle.setFont(SwingUtils.SUBTITLE_FONT);
        categoryListTitle.setForeground(SwingUtils.TEXT_COLOR);
        categoryListTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        categoryListPanel.add(categoryListTitle, BorderLayout.NORTH);
        
        // Create a list of categories
        DefaultListModel<Category> categoryModel = new DefaultListModel<>();
        List<Category> categories = categoryController.getCategoriesByUser(currentUser.getUserId());
        for (Category category : categories) {
            categoryModel.addElement(category);
        }
        
        JList<Category> categoryList = new JList<>(categoryModel);
        categoryList.setFont(SwingUtils.REGULAR_FONT);
        categoryList.setFixedCellHeight(40);
        categoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category) {
                    setText(((Category) value).getName());
                }
                return this;
            }
        });
        
        // Fields for category details
        JTextField categoryNameField = SwingUtils.createTextField(20);
        JTextField categoryDescriptionField = SwingUtils.createTextField(20);
        JCheckBox defaultCheckbox = new JCheckBox("Default Category");
        defaultCheckbox.setFont(SwingUtils.REGULAR_FONT);
        defaultCheckbox.setForeground(SwingUtils.TEXT_COLOR);
        defaultCheckbox.setBackground(Color.WHITE);
        JButton saveDetailsButton = SwingUtils.createButton("Save Details", SwingUtils.SECONDARY_COLOR);
        saveDetailsButton.setEnabled(false);
        
        // Listen for selection changes
        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Category selectedCategory = categoryList.getSelectedValue();
                if (selectedCategory != null) {
                    // Update details panel
                    categoryNameField.setText(selectedCategory.getName());
                    categoryDescriptionField.setText(selectedCategory.getDescription());
                    defaultCheckbox.setSelected(selectedCategory.isDefault());
                    
                    // Disable editing for default categories
                    boolean isDefault = selectedCategory.isDefault();
                    categoryNameField.setEnabled(!isDefault);
                    categoryDescriptionField.setEnabled(!isDefault);
                    defaultCheckbox.setEnabled(false); // Default flag can only be set internally
                    saveDetailsButton.setEnabled(!isDefault);
                    
                    saveDetailsButton.setActionCommand(String.valueOf(selectedCategory.getCategoryId()));
                }
            }
        });
        
        JScrollPane categoryScrollPane = new JScrollPane(categoryList);
        categoryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        categoryListPanel.add(categoryScrollPane, BorderLayout.CENTER);
        
        contentPanel.add(categoryListPanel);
        
        // Category details on the right
        JPanel categoryDetailsPanel = new JPanel(new BorderLayout());
        categoryDetailsPanel.setBackground(Color.WHITE);
        categoryDetailsPanel.setBorder(BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT));
        
        JLabel detailsTitle = new JLabel("Category Details", SwingConstants.CENTER);
        detailsTitle.setFont(SwingUtils.SUBTITLE_FONT);
        detailsTitle.setForeground(SwingUtils.TEXT_COLOR);
        detailsTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        categoryDetailsPanel.add(detailsTitle, BorderLayout.NORTH);
        
        JPanel detailsForm = new JPanel(new GridBagLayout());
        detailsForm.setBackground(Color.WHITE);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 15, 10, 15);
        
        // When no category is selected
        JLabel selectCategoryLabel = new JLabel("Select a category to view details");
        selectCategoryLabel.setFont(SwingUtils.REGULAR_FONT);
        selectCategoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        detailsForm.add(selectCategoryLabel, constraints);
        
        // Name field
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(SwingUtils.REGULAR_FONT);
        nameLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        detailsForm.add(nameLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 0.7;
        detailsForm.add(categoryNameField, constraints);
        
        // Description field
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setFont(SwingUtils.REGULAR_FONT);
        descriptionLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.weightx = 0.3;
        detailsForm.add(descriptionLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.weightx = 0.7;
        detailsForm.add(categoryDescriptionField, constraints);
        
        // Default checkbox
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        detailsForm.add(defaultCheckbox, constraints);
        
        // Save button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveDetailsButton);
        
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        detailsForm.add(buttonPanel, constraints);
        
        // Save button action
        saveDetailsButton.addActionListener(e -> {
            try {
                int categoryId = Integer.parseInt(e.getActionCommand());
                String name = categoryNameField.getText().trim();
                String description = categoryDescriptionField.getText().trim();
                
                if (name.isEmpty()) {
                    SwingUtils.showErrorMessage(this, "Invalid Input", "Category name cannot be empty");
                    return;
                }
                
                // Get the category from the list
                Category category = null;
                for (int i = 0; i < categoryModel.getSize(); i++) {
                    Category c = categoryModel.getElementAt(i);
                    if (c.getCategoryId() == categoryId) {
                        category = c;
                        break;
                    }
                }
                
                if (category != null) {
                    category.setName(name);
                    category.setDescription(description);
                    
                    // Update the category
                    boolean success = categoryController.updateCategory(
                        category.getCategoryId(), 
                        category.getName(), 
                        category.getDescription(), 
                        currentUser.getUserId()
                    );
                    
                    if (success) {
                        SwingUtils.showInfoMessage(this, "Success", "Category updated successfully");
                        refreshCategoryList(categoryModel);
                        refreshCategoryFilters();
                    } else {
                        SwingUtils.showErrorMessage(this, "Error", "Failed to update category");
                    }
                }
            } catch (NumberFormatException ex) {
                SwingUtils.showErrorMessage(this, "Error", "Invalid category ID");
            }
        });
        
        categoryDetailsPanel.add(detailsForm, BorderLayout.CENTER);
        
        contentPanel.add(categoryDetailsPanel);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Open dialog to add a new category
     */
    private void addCategory(ActionEvent event) {
        // Create an input dialog for the new category
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(20);
        JLabel descriptionLabel = new JLabel("Description:");
        JTextField descriptionField = new JTextField(20);
        
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(descriptionLabel);
        panel.add(descriptionField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add New Category", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String description = descriptionField.getText().trim();
            
            if (name.isEmpty()) {
                SwingUtils.showErrorMessage(this, "Invalid Input", "Category name cannot be empty");
                return;
            }
            
            // Create a new category
            Category category = new Category();
            category.setName(name);
            category.setDescription(description);
            category.setUserId(currentUser.getUserId());
            category.setDefault(false);
            
            // Add the category
            boolean success = categoryController.addCategory(
                category.getName(),
                category.getDescription(),
                category.getUserId()
            );
            
            if (success) {
                SwingUtils.showInfoMessage(this, "Success", "Category added successfully");
                
                // Refresh the categories list in the UI and comboboxes
                refreshCategoryList(null);
                refreshCategoryFilters();
            } else {
                SwingUtils.showErrorMessage(this, "Error", "Failed to add category");
            }
        }
    }
    
    /**
     * Edit the selected category
     */
    private void editCategory(ActionEvent event) {
        // Get the selected category from the list
        JList<?> categoryList = null;
        
        // Find the category list in the category panel
        Component component = tabbedPane.getComponentAt(2);
        if (component instanceof Container) {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] innerComponents = panel.getComponents();
                    for (Component inner : innerComponents) {
                        if (inner instanceof JPanel) {
                            JPanel innerPanel = (JPanel) inner;
                            Component[] scrollComponents = innerPanel.getComponents();
                            for (Component scrollPane : scrollComponents) {
                                if (scrollPane instanceof JScrollPane) {
                                    Component viewport = ((JScrollPane) scrollPane).getViewport().getView();
                                    if (viewport instanceof JList) {
                                        categoryList = (JList<?>) viewport;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (categoryList != null) {
            Category selectedCategory = (Category) categoryList.getSelectedValue();
            
            if (selectedCategory == null) {
                SwingUtils.showErrorMessage(this, "No Selection", "Please select a category to edit");
                return;
            }
            
            if (selectedCategory.isDefault()) {
                SwingUtils.showErrorMessage(this, "Cannot Edit", "Default categories cannot be edited");
                return;
            }
            
            // Create an input dialog for editing
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            JLabel nameLabel = new JLabel("Name:");
            JTextField nameField = new JTextField(selectedCategory.getName(), 20);
            JLabel descriptionLabel = new JLabel("Description:");
            JTextField descriptionField = new JTextField(selectedCategory.getDescription(), 20);
            
            panel.add(nameLabel);
            panel.add(nameField);
            panel.add(descriptionLabel);
            panel.add(descriptionField);
            
            int result = JOptionPane.showConfirmDialog(
                this, panel, "Edit Category", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                
                if (name.isEmpty()) {
                    SwingUtils.showErrorMessage(this, "Invalid Input", "Category name cannot be empty");
                    return;
                }
                
                // Update the category
                selectedCategory.setName(name);
                selectedCategory.setDescription(description);
                
                boolean success = categoryController.updateCategory(
                    selectedCategory.getCategoryId(),
                    selectedCategory.getName(),
                    selectedCategory.getDescription(),
                    currentUser.getUserId()
                );
                
                if (success) {
                    SwingUtils.showInfoMessage(this, "Success", "Category updated successfully");
                    
                    // Refresh the categories list in the UI and comboboxes
                    refreshCategoryList(null);
                    refreshCategoryFilters();
                } else {
                    SwingUtils.showErrorMessage(this, "Error", "Failed to update category");
                }
            }
        }
    }
    
    /**
     * Delete the selected category
     */
    private void deleteCategory(ActionEvent event) {
        // Get the selected category from the list
        JList<?> categoryList = null;
        
        // Find the category list in the category panel
        Component component = tabbedPane.getComponentAt(2);
        if (component instanceof Container) {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] innerComponents = panel.getComponents();
                    for (Component inner : innerComponents) {
                        if (inner instanceof JPanel) {
                            JPanel innerPanel = (JPanel) inner;
                            Component[] scrollComponents = innerPanel.getComponents();
                            for (Component scrollPane : scrollComponents) {
                                if (scrollPane instanceof JScrollPane) {
                                    Component viewport = ((JScrollPane) scrollPane).getViewport().getView();
                                    if (viewport instanceof JList) {
                                        categoryList = (JList<?>) viewport;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (categoryList != null) {
            Category selectedCategory = (Category) categoryList.getSelectedValue();
            
            if (selectedCategory == null) {
                SwingUtils.showErrorMessage(this, "No Selection", "Please select a category to delete");
                return;
            }
            
            if (selectedCategory.isDefault()) {
                SwingUtils.showErrorMessage(this, "Cannot Delete", "Default categories cannot be deleted");
                return;
            }
            
            // Ask for confirmation
            boolean confirmed = SwingUtils.showConfirmDialog(
                this, "Delete Category", 
                "Are you sure you want to delete the category '" + selectedCategory.getName() + "'?\n" +
                "All expenses in this category will be reassigned to 'Other'.");
            
            if (confirmed) {
                boolean success = categoryController.deleteCategory(
                    selectedCategory.getCategoryId(),
                    currentUser.getUserId()
                );
                
                if (success) {
                    SwingUtils.showInfoMessage(this, "Success", "Category deleted successfully");
                    
                    // Refresh the categories list in the UI and comboboxes
                    refreshCategoryList(null);
                    refreshCategoryFilters();
                } else {
                    SwingUtils.showErrorMessage(this, "Error", "Failed to delete category");
                }
            }
        }
    }
    
    /**
     * Refresh the category list in the UI
     */
    private void refreshCategoryList(DefaultListModel<Category> existingModel) {
        // Get fresh categories from the database
        List<Category> categories = categoryController.getCategoriesByUser(currentUser.getUserId());
        
        // If we received a specific model, update it
        if (existingModel != null) {
            existingModel.clear();
            for (Category category : categories) {
                existingModel.addElement(category);
            }
            return;
        }
        
        // Otherwise, find the list in the UI and update it
        JList<?> categoryList = null;
        
        // Find the category list in the category panel
        Component component = tabbedPane.getComponentAt(2);
        if (component instanceof Container) {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Component[] innerComponents = panel.getComponents();
                    for (Component inner : innerComponents) {
                        if (inner instanceof JPanel) {
                            JPanel innerPanel = (JPanel) inner;
                            Component[] scrollComponents = innerPanel.getComponents();
                            for (Component scrollPane : scrollComponents) {
                                if (scrollPane instanceof JScrollPane) {
                                    Component viewport = ((JScrollPane) scrollPane).getViewport().getView();
                                    if (viewport instanceof JList) {
                                        categoryList = (JList<?>) viewport;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (categoryList != null) {
            DefaultListModel<Category> model = (DefaultListModel<Category>) categoryList.getModel();
            model.clear();
            for (Category category : categories) {
                model.addElement(category);
            }
        }
    }
    
    /**
     * Refresh category filter comboboxes
     */
    private void refreshCategoryFilters() {
        // Refresh the category filter in the expenses panel
        if (categoryFilterComboBox != null) {
            // Save selected category ID
            Category selectedCategory = (Category) categoryFilterComboBox.getSelectedItem();
            int selectedId = selectedCategory != null ? selectedCategory.getCategoryId() : 0;
            
            // Refresh the combobox
            categoryFilterComboBox.removeAllItems();
            categoryFilterComboBox.addItem(new Category(0, "All Categories", "", 0, false));
            
            List<Category> categories = categoryController.getCategoriesByUser(currentUser.getUserId());
            for (Category category : categories) {
                categoryFilterComboBox.addItem(category);
                
                // Re-select the previously selected category if found
                if (category.getCategoryId() == selectedId) {
                    categoryFilterComboBox.setSelectedItem(category);
                }
            }
        }
    }
    
    /**
     * Create the settings panel
     */
    private JPanel createSettingsPanel() {
        JPanel panel = SwingUtils.createTitledPanel("Application Settings");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(SwingUtils.BACKGROUND_COLOR);
        panel.setBorder(new CompoundBorder(
            new EmptyBorder(15, 15, 15, 15),
            panel.getBorder()
        ));
        
        // Create tabs for different settings
        JTabbedPane settingsTabs = new JTabbedPane();
        settingsTabs.setFont(SwingUtils.REGULAR_FONT);
        
        // User settings panel
        JPanel userSettingsPanel = new JPanel(new BorderLayout());
        userSettingsPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JPanel userFormPanel = new JPanel(new GridBagLayout());
        userFormPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 15, 10, 15);
        constraints.weightx = 0.3;
        
        // User profile heading
        JLabel profileHeading = new JLabel("User Profile", SwingConstants.LEFT);
        profileHeading.setFont(SwingUtils.SUBTITLE_FONT);
        profileHeading.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        userFormPanel.add(profileHeading, constraints);
        
        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(SwingUtils.REGULAR_FONT);
        usernameLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        userFormPanel.add(usernameLabel, constraints);
        
        JTextField usernameField = SwingUtils.createTextField(20);
        usernameField.setText(currentUser.getUsername());
        constraints.gridx = 1;
        constraints.gridy = 1;
        userFormPanel.add(usernameField, constraints);
        
        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(SwingUtils.REGULAR_FONT);
        emailLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 2;
        userFormPanel.add(emailLabel, constraints);
        
        JTextField emailField = SwingUtils.createTextField(20);
        emailField.setText(currentUser.getEmail());
        constraints.gridx = 1;
        constraints.gridy = 2;
        userFormPanel.add(emailField, constraints);
        
        // Change Password heading
        JLabel passwordHeading = new JLabel("Change Password", SwingConstants.LEFT);
        passwordHeading.setFont(SwingUtils.SUBTITLE_FONT);
        passwordHeading.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        userFormPanel.add(passwordHeading, constraints);
        
        // Current password
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        currentPasswordLabel.setFont(SwingUtils.REGULAR_FONT);
        currentPasswordLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        userFormPanel.add(currentPasswordLabel, constraints);
        
        JPasswordField currentPasswordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 4;
        userFormPanel.add(currentPasswordField, constraints);
        
        JLabel newPasswordLabel = new JLabel("New Password:");
        newPasswordLabel.setFont(SwingUtils.REGULAR_FONT);
        newPasswordLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 5;
        userFormPanel.add(newPasswordLabel, constraints);
        
        JPasswordField newPasswordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 5;
        userFormPanel.add(newPasswordField, constraints);
        
        // Confirm new password
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(SwingUtils.REGULAR_FONT);
        confirmPasswordLabel.setForeground(SwingUtils.TEXT_COLOR);
        constraints.gridx = 0;
        constraints.gridy = 6;
        userFormPanel.add(confirmPasswordLabel, constraints);
        
        JPasswordField confirmPasswordField = SwingUtils.createPasswordField(20);
        constraints.gridx = 1;
        constraints.gridy = 6;
        userFormPanel.add(confirmPasswordField, constraints);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JButton saveButton = SwingUtils.createButton("Save Changes", SwingUtils.SECONDARY_COLOR);
        saveButton.addActionListener(e -> {
            // Validate current password if trying to change password
            String currentPwd = new String(currentPasswordField.getPassword());
            String newPwd = new String(newPasswordField.getPassword());
            String confirmPwd = new String(confirmPasswordField.getPassword());

            // Set up the data to update
            User updatedUser = new User();
            updatedUser.setUserId(currentUser.getUserId());
            updatedUser.setUsername(usernameField.getText().trim());
            updatedUser.setEmail(emailField.getText().trim());
            
            // Check if trying to change password
            boolean changingPassword = !currentPwd.isEmpty() || !newPwd.isEmpty() || !confirmPwd.isEmpty();
            
            if (changingPassword) {
                // Validate current password
                UserController userController = new UserController();
                User authenticated = userController.authenticateUser(currentUser.getUsername(), currentPwd);
                
                if (authenticated == null) {
                    SwingUtils.showErrorMessage(this, "Error", "Current password is incorrect");
                    return;
                }
                
                // Check if new passwords match
                if (!newPwd.equals(confirmPwd)) {
                    SwingUtils.showErrorMessage(this, "Error", "New passwords do not match");
                    return;
                }
                
                // Check new password length
                if (newPwd.length() < 6) {
                    SwingUtils.showErrorMessage(this, "Error", "New password must be at least 6 characters");
                    return;
                }
                
                // Set the new password
                updatedUser.setPassword(newPwd);
            } else {
                // Keep existing password
                updatedUser.setPassword(currentUser.getPassword());
            }
            
            // Validate username and email
            if (updatedUser.getUsername().isEmpty()) {
                SwingUtils.showErrorMessage(this, "Error", "Username cannot be empty");
                return;
            }
            
            if (updatedUser.getEmail().isEmpty()) {
                SwingUtils.showErrorMessage(this, "Error", "Email cannot be empty");
                return;
            }
            
            // Update user in database
            UserController userController = new UserController();
            boolean success = userController.updateUser(updatedUser);
            
            if (success) {
                // Update current user
                currentUser.setUsername(updatedUser.getUsername());
                currentUser.setEmail(updatedUser.getEmail());
                if (changingPassword) {
                    currentUser.setPassword(updatedUser.getPassword());
                }
                
                // Clear password fields
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                
                // Update UI
                updateWelcomeMessage();
                
                SwingUtils.showInfoMessage(this, "Success", "User profile updated successfully");
            } else {
                SwingUtils.showErrorMessage(this, "Error", "Failed to update user profile");
            }
        });
        
        buttonPanel.add(saveButton);
        
        userSettingsPanel.add(userFormPanel, BorderLayout.CENTER);
        userSettingsPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        settingsTabs.addTab("User Profile", userSettingsPanel);
        
        // App preferences panel
        JPanel preferencesPanel = new JPanel(new BorderLayout());
        preferencesPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JPanel preferencesFormPanel = new JPanel(new GridBagLayout());
        preferencesFormPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        GridBagConstraints prefConstraints = new GridBagConstraints();
        prefConstraints.fill = GridBagConstraints.HORIZONTAL;
        prefConstraints.insets = new Insets(10, 15, 10, 15);
        
        // Currency setting
        JLabel currencyLabel = new JLabel("Default Currency:");
        currencyLabel.setFont(SwingUtils.REGULAR_FONT);
        currencyLabel.setForeground(SwingUtils.TEXT_COLOR);
        prefConstraints.gridx = 0;
        prefConstraints.gridy = 0;
        prefConstraints.weightx = 0.3;
        preferencesFormPanel.add(currencyLabel, prefConstraints);
        
        // Added Indian Rupee to the currencies
        String[] currencies = {"USD ($)", "EUR (€)", "GBP (£)", "INR (₹)", "JPY (¥)", "CNY (¥)"};
        JComboBox<String> currencyComboBox = new JComboBox<>(currencies);
        currencyComboBox.setFont(SwingUtils.REGULAR_FONT);
        
        // Load saved currency preference if it exists
        String savedCurrency = getUserPreference("currency", "USD ($)");
        currencyComboBox.setSelectedItem(savedCurrency);
        
        prefConstraints.gridx = 1;
        prefConstraints.weightx = 0.7;
        preferencesFormPanel.add(currencyComboBox, prefConstraints);
        
        // Currency symbol preview
        JLabel currencySymbolLabel = new JLabel("Currency Symbol:");
        currencySymbolLabel.setFont(SwingUtils.REGULAR_FONT);
        currencySymbolLabel.setForeground(SwingUtils.TEXT_COLOR);
        prefConstraints.gridx = 0;
        prefConstraints.gridy = 1;
        prefConstraints.weightx = 0.3;
        preferencesFormPanel.add(currencySymbolLabel, prefConstraints);
        
        JLabel currencySymbolPreview = new JLabel(getCurrencySymbolFromSelection(currencyComboBox.getSelectedItem().toString()));
        currencySymbolPreview.setFont(SwingUtils.SUBTITLE_FONT);
        currencySymbolPreview.setForeground(SwingUtils.PRIMARY_COLOR);
        prefConstraints.gridx = 1;
        prefConstraints.weightx = 0.7;
        preferencesFormPanel.add(currencySymbolPreview, prefConstraints);
        
        // Update preview when selection changes
        currencyComboBox.addActionListener(e -> {
            String selectedCurrency = currencyComboBox.getSelectedItem().toString();
            currencySymbolPreview.setText(getCurrencySymbolFromSelection(selectedCurrency));
        });
        
        // Date format
        JLabel dateFormatLabel = new JLabel("Date Format:");
        dateFormatLabel.setFont(SwingUtils.REGULAR_FONT);
        dateFormatLabel.setForeground(SwingUtils.TEXT_COLOR);
        prefConstraints.gridx = 0;
        prefConstraints.gridy = 2;
        prefConstraints.weightx = 0.3;
        preferencesFormPanel.add(dateFormatLabel, prefConstraints);
        
        String[] dateFormats = {"yyyy-MM-dd", "MM/dd/yyyy", "dd/MM/yyyy", "dd-MMM-yyyy"};
        JComboBox<String> dateFormatComboBox = new JComboBox<>(dateFormats);
        dateFormatComboBox.setFont(SwingUtils.REGULAR_FONT);
        
        // Load saved date format preference if it exists
        String savedDateFormat = getUserPreference("dateFormat", "yyyy-MM-dd");
        dateFormatComboBox.setSelectedItem(savedDateFormat);
        
        prefConstraints.gridx = 1;
        prefConstraints.weightx = 0.7;
        preferencesFormPanel.add(dateFormatComboBox, prefConstraints);
        
        // Date format preview
        JLabel dateFormatPreviewLabel = new JLabel("Preview:");
        dateFormatPreviewLabel.setFont(SwingUtils.REGULAR_FONT);
        dateFormatPreviewLabel.setForeground(SwingUtils.TEXT_COLOR);
        prefConstraints.gridx = 0;
        prefConstraints.gridy = 3;
        prefConstraints.weightx = 0.3;
        preferencesFormPanel.add(dateFormatPreviewLabel, prefConstraints);
        
        JLabel dateFormatPreview = new JLabel(formatDateWithPattern(new Date(), dateFormatComboBox.getSelectedItem().toString()));
        dateFormatPreview.setFont(SwingUtils.REGULAR_FONT);
        dateFormatPreview.setForeground(SwingUtils.PRIMARY_COLOR);
        prefConstraints.gridx = 1;
        prefConstraints.weightx = 0.7;
        preferencesFormPanel.add(dateFormatPreview, prefConstraints);
        
        // Update preview when selection changes
        dateFormatComboBox.addActionListener(e -> {
            String selectedFormat = dateFormatComboBox.getSelectedItem().toString();
            dateFormatPreview.setText(formatDateWithPattern(new Date(), selectedFormat));
        });
        
        // Theme panel like PDF export - coming soon
        JPanel themePanel = new JPanel(new BorderLayout(10, 10));
        themePanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        themePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT), 
            "Theme Settings", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            SwingUtils.REGULAR_FONT, 
            SwingUtils.TEXT_COLOR
        ));
        
        JLabel themeInfoLabel = new JLabel("<html>Theme customization will be available in a future version.<br>" +
            "This feature will allow you to customize the appearance of the application<br>" +
            "with different color schemes and visual styles.</html>");
        themeInfoLabel.setFont(SwingUtils.REGULAR_FONT);
        themeInfoLabel.setForeground(SwingUtils.TEXT_COLOR);
        themeInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        themePanel.add(themeInfoLabel, BorderLayout.CENTER);
        
        JButton themeButton = SwingUtils.createButton("Coming Soon", SwingUtils.SECONDARY_COLOR);
        themeButton.addActionListener(e -> {
            SwingUtils.showInfoMessage(this, "Coming Soon", 
                "Theme customization will be implemented in a future version of the application.");
        });
        
        JPanel themeButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        themeButtonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        themeButtonPanel.add(themeButton);
        themePanel.add(themeButtonPanel, BorderLayout.SOUTH);
        
        prefConstraints.gridx = 0;
        prefConstraints.gridy = 4;
        prefConstraints.gridwidth = 2;
        prefConstraints.weightx = 1.0;
        prefConstraints.fill = GridBagConstraints.BOTH;
        preferencesFormPanel.add(themePanel, prefConstraints);
        
        // Reset constraints
        prefConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel prefButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        prefButtonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JButton savePrefButton = SwingUtils.createButton("Save Preferences", SwingUtils.SECONDARY_COLOR);
        savePrefButton.addActionListener(e -> {
            // Save preferences to user-specific settings
            String selectedCurrency = currencyComboBox.getSelectedItem().toString();
            String selectedDateFormat = dateFormatComboBox.getSelectedItem().toString();
            
            // Save to user preferences (we'll store in user properties in database)
            saveUserPreference("currency", selectedCurrency);
            saveUserPreference("dateFormat", selectedDateFormat);
            
            // Update UI elements that use these preferences
            updateUIWithPreferences();
            
            SwingUtils.showInfoMessage(this, "Success", "Preferences saved successfully");
        });
        prefButtonPanel.add(savePrefButton);
        
        preferencesPanel.add(preferencesFormPanel, BorderLayout.CENTER);
        preferencesPanel.add(prefButtonPanel, BorderLayout.SOUTH);
        
        settingsTabs.addTab("Preferences", preferencesPanel);
        
        // Account actions panel
        JPanel accountActionsPanel = new JPanel(new BorderLayout());
        accountActionsPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        JPanel actionsFormPanel = new JPanel(new GridBagLayout());
        actionsFormPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        
        GridBagConstraints actionConstraints = new GridBagConstraints();
        actionConstraints.fill = GridBagConstraints.HORIZONTAL;
        actionConstraints.insets = new Insets(10, 15, 10, 15);
        
        // Account actions heading
        JLabel actionsHeading = new JLabel("Account Actions", SwingConstants.LEFT);
        actionsHeading.setFont(SwingUtils.SUBTITLE_FONT);
        actionsHeading.setForeground(SwingUtils.TEXT_COLOR);
        actionConstraints.gridx = 0;
        actionConstraints.gridy = 0;
        actionConstraints.gridwidth = 2;
        actionsFormPanel.add(actionsHeading, actionConstraints);
        
        // Logout
        JPanel logoutPanel = new JPanel(new BorderLayout(10, 10));
        logoutPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        logoutPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT), 
            "Logout", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            SwingUtils.REGULAR_FONT, 
            SwingUtils.TEXT_COLOR
        ));
        
        JLabel logoutLabel = new JLabel("<html>Click the button below to logout from your account.<br>You will be returned to the login screen.</html>");
        logoutLabel.setFont(SwingUtils.REGULAR_FONT);
        logoutLabel.setForeground(SwingUtils.TEXT_COLOR);
        logoutPanel.add(logoutLabel, BorderLayout.CENTER);
        
        JButton logoutButton = SwingUtils.createButton("Logout", SwingUtils.SECONDARY_COLOR);
        logoutButton.addActionListener(this::logout);
        JPanel logoutButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutButtonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        logoutButtonPanel.add(logoutButton);
        logoutPanel.add(logoutButtonPanel, BorderLayout.SOUTH);
        
        actionConstraints.gridx = 0;
        actionConstraints.gridy = 1;
        actionConstraints.gridwidth = 2;
        actionsFormPanel.add(logoutPanel, actionConstraints);
        
        // Delete account
        JPanel deletePanel = new JPanel(new BorderLayout(10, 10));
        deletePanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        deletePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(SwingUtils.LIGHT_ACCENT), 
            "Delete Account", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            SwingUtils.REGULAR_FONT, 
            SwingUtils.TEXT_COLOR
        ));
        
        JLabel deleteLabel = new JLabel("<html><span style='color:red;'>Warning: This action cannot be undone.</span><br>" +
            "Deleting your account will permanently remove all your data, including categories, expenses, and reports.<br>" +
            "Click the button below to delete your account.</html>");
        deleteLabel.setFont(SwingUtils.REGULAR_FONT);
        deleteLabel.setForeground(SwingUtils.TEXT_COLOR);
        deletePanel.add(deleteLabel, BorderLayout.CENTER);
        
        JButton deleteButton = SwingUtils.createButton("Delete Account", SwingUtils.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteAccount());
        JPanel deleteButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deleteButtonPanel.setBackground(SwingUtils.BACKGROUND_COLOR);
        deleteButtonPanel.add(deleteButton);
        deletePanel.add(deleteButtonPanel, BorderLayout.SOUTH);
        
        actionConstraints.gridx = 0;
        actionConstraints.gridy = 2;
        actionConstraints.gridwidth = 2;
        actionsFormPanel.add(deletePanel, actionConstraints);
        
        accountActionsPanel.add(actionsFormPanel, BorderLayout.CENTER);
        
        settingsTabs.addTab("Account", accountActionsPanel);
        
        panel.add(settingsTabs, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Get the currency symbol from the selected currency string
     */
    private String getCurrencySymbolFromSelection(String currencyString) {
        if (currencyString.contains("USD")) {
            return "$";
        } else if (currencyString.contains("EUR")) {
            return "€";
        } else if (currencyString.contains("GBP")) {
            return "£";
        } else if (currencyString.contains("INR")) {
            return "₹";
        } else if (currencyString.contains("JPY")) {
            return "¥";
        } else if (currencyString.contains("CNY")) {
            return "¥";
        } else {
            return "$"; // Default to USD
        }
    }
    
    /**
     * Format a date with the specified pattern
     */
    private String formatDateWithPattern(Date date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return new SimpleDateFormat("yyyy-MM-dd").format(date); // Default format
        }
    }
    
    /**
     * Save a user preference
     */
    private void saveUserPreference(String key, String value) {
        // In a real application, these would be stored in a database
        // For simplicity, we'll use Java Preferences API
        Preferences prefs = Preferences.userRoot().node("com.expensetracker.user." + currentUser.getUserId());
        prefs.put(key, value);
    }
    
    /**
     * Get a user preference
     */
    private String getUserPreference(String key, String defaultValue) {
        // In a real application, these would be retrieved from a database
        // For simplicity, we'll use Java Preferences API
        Preferences prefs = Preferences.userRoot().node("com.expensetracker.user." + currentUser.getUserId());
        return prefs.get(key, defaultValue);
    }
    
    /**
     * Update UI elements with current preferences
     */
    private void updateUIWithPreferences() {
        // Update currency display in expenses table and totals
        loadExpenses(); // This will reload the expenses with the current currency format
        
        // Additional UI updates based on preferences could be added here
    }
    
    /**
     * Delete the user account after password verification
     */
    private void deleteAccount() {
        // Create confirmation dialog with password field
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        JLabel confirmLabel = new JLabel("Enter your password to confirm account deletion:");
        JPasswordField passwordField = new JPasswordField(20);
        
        panel.add(confirmLabel);
        panel.add(passwordField);
        
        int result = JOptionPane.showConfirmDialog(
            this, panel, "Confirm Account Deletion",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String password = new String(passwordField.getPassword());
            
            // Verify password
            UserController userController = new UserController();
            User authenticated = userController.authenticateUser(currentUser.getUsername(), password);
            
            if (authenticated == null) {
                SwingUtils.showErrorMessage(this, "Error", "Incorrect password. Account deletion canceled.");
                return;
            }
            
            // Final confirmation
            boolean confirmed = SwingUtils.showConfirmDialog(
                this, "Final Warning", 
                "Are you absolutely sure you want to delete your account? This action CANNOT be undone!"
            );
            
            if (confirmed) {
                // Delete the user
                boolean success = userController.deleteUser(currentUser.getUserId());
                
                if (success) {
                    SwingUtils.showInfoMessage(this, "Account Deleted", "Your account has been successfully deleted.");
                    logout(null); // Log out after deletion
                } else {
                    SwingUtils.showErrorMessage(this, "Error", "Failed to delete account. Please try again later.");
                }
            }
        }
    }
    
    /**
     * Update the welcome message in the header
     */
    private void updateWelcomeMessage() {
        // Find the welcome label in the header panel
        if (this.getContentPane() instanceof JPanel) {
            JPanel mainContainer = (JPanel) this.getContentPane();
            for (Component comp : mainContainer.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel headerPanel = (JPanel) comp;
                    if (headerPanel.getLayout() instanceof BorderLayout) {
                        for (Component headerComp : headerPanel.getComponents()) {
                            if (headerComp instanceof JLabel) {
                                JLabel label = (JLabel) headerComp;
                                if (label.getText().startsWith("Welcome")) {
                                    label.setText("Welcome, " + currentUser.getUsername() + "!");
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create the menu bar
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(SwingUtils.PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        
        // Style for all menu items
        UIManager.put("Menu.selectionBackground", SwingUtils.PRIMARY_COLOR.darker());
        UIManager.put("Menu.selectionForeground", Color.WHITE);
        UIManager.put("Menu.foreground", Color.WHITE);
        UIManager.put("Menu.font", SwingUtils.REGULAR_FONT);
        UIManager.put("MenuItem.font", SwingUtils.REGULAR_FONT);
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        
        JMenuItem exportItem = new JMenuItem("Export Data");
        JMenuItem importItem = new JMenuItem("Import Data");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        exitItem.addActionListener(this::exitApplication);
        
        fileMenu.add(exportItem);
        fileMenu.add(importItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // User menu
        JMenu userMenu = new JMenu("User");
        userMenu.setForeground(Color.WHITE);
        
        JMenuItem profileItem = new JMenuItem("Profile");
        JMenuItem logoutItem = new JMenuItem("Logout");
        
        logoutItem.addActionListener(this::logout);
        
        userMenu.add(profileItem);
        userMenu.add(logoutItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(Color.WHITE);
        
        JMenuItem helpContentsItem = new JMenuItem("Help Contents");
        JMenuItem aboutItem = new JMenuItem("About");
        
        helpMenu.add(helpContentsItem);
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Load expenses from the database and populate the table
     */
    private void loadExpenses() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Get the selected category filter
        int categoryFilterId = 0;
        if (categoryFilterComboBox != null && categoryFilterComboBox.getSelectedItem() != null) {
            Category selectedCategory = (Category) categoryFilterComboBox.getSelectedItem();
            categoryFilterId = selectedCategory.getCategoryId();
        }
        
        // Get expenses for the current user
        List<Expense> expenses = expenseController.getExpensesByUser(currentUser.getUserId());
        
        // Get the user's preferred currency symbol
        String currencySymbol = getCurrencySymbolFromSelection(getUserPreference("currency", "USD ($)"));
        
        // Get the user's preferred date format
        String dateFormat = getUserPreference("dateFormat", "yyyy-MM-dd");
        
        if (expenses != null) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (Expense expense : expenses) {
                // Apply category filter if selected
                if (categoryFilterId != 0 && expense.getCategoryId() != categoryFilterId) {
                    continue;
                }
                
                // Format the date according to user preference
                String formattedDate = formatDateWithPattern(expense.getExpenseDate(), dateFormat);
                
                // Format the amount with the currency symbol
                String formattedAmount = currencySymbol + " " + expense.getAmount().toString();
                
                // Get category name
                String categoryName = getCategoryName(expense.getCategoryId());
                
                // Add row to table
                tableModel.addRow(new Object[]{
                    expense.getExpenseId(),
                    formattedDate,
                    categoryName,
                    formattedAmount,
                    expense.getDescription()
                });
                
                // Add to total
                totalAmount = totalAmount.add(expense.getAmount());
            }
            
            // Update total expenses label
            totalExpensesLabel.setText(currencySymbol + " " + totalAmount.toString());
        }
    }
    
    /**
     * Open dialog to add a new expense
     */
    private void openAddExpenseDialog(ActionEvent event) {
        AddExpenseDialog dialog = new AddExpenseDialog(this, currentUser);
        dialog.setVisible(true);
        loadExpenses(); // Refresh the table after adding
    }
    
    /**
     * Open dialog to edit an existing expense
     */
    private void openEditExpenseDialog(ActionEvent event) {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow == -1) {
            SwingUtils.showErrorMessage(this, "Edit Expense", "Please select an expense to edit.");
            return;
        }
        
        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
        AddExpenseDialog dialog = new AddExpenseDialog(this, currentUser, expenseId);
        dialog.setVisible(true);
        loadExpenses(); // Refresh the table after editing
    }
    
    /**
     * Delete the selected expense
     */
    private void deleteExpense(ActionEvent event) {
        int selectedRow = expensesTable.getSelectedRow();
        if (selectedRow == -1) {
            SwingUtils.showErrorMessage(this, "Delete Expense", "Please select an expense to delete.");
            return;
        }
        
        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);
        
        boolean confirmed = SwingUtils.showConfirmDialog(
            this, "Delete Expense", "Are you sure you want to delete this expense?");
        
        if (confirmed) {
            boolean success = expenseController.deleteExpense(expenseId, currentUser.getUserId());
            
            if (success) {
                SwingUtils.showInfoMessage(this, "Delete Expense", "Expense deleted successfully.");
                loadExpenses();
            } else {
                SwingUtils.showErrorMessage(this, "Delete Expense", "Failed to delete expense.");
            }
        }
    }
    
    /**
     * Method to exit application
     */
    private void exitApplication(ActionEvent event) {
        System.exit(0);
    }
    
    /**
     * Logout the user and return to login screen
     */
    private void logout(ActionEvent event) {
        this.dispose();
        LoginView loginView = new LoginView();
        loginView.setVisible(true);
    }

    private void loadExpensesFromAction(ActionEvent event) {
        loadExpenses();
    }

    /**
     * Get the category name for a given category ID
     */
    private String getCategoryName(int categoryId) {
        // Get the category from the controller
        Category category = categoryController.getCategoryById(categoryId);
        
        // Return the name if found, otherwise return "Unknown"
        return (category != null) ? category.getName() : "Unknown";
    }
} 