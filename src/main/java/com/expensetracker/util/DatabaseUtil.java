package com.expensetracker.util;

import java.sql.*;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "expense_tracker";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "dbms123"; // Change this to your MySQL password
    
    // Connection string with extra parameters for compatibility
    private static final String FULL_DB_URL = 
        "jdbc:mysql://localhost:3306/" + DB_NAME + 
        "?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8" +
        "&createDatabaseIfNotExist=true";
    
    private static Connection connection;
    
    /**
     * Initializes the database by creating it if it doesn't exist
     * and creating all required tables
     */
    public static void initializeDatabase() throws SQLException {
        try {
            // Create database if needed
            try (Connection tempConnection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true", 
                    DB_USER, DB_PASSWORD);
                 Statement statement = tempConnection.createStatement()) {
                
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
                System.out.println("Database created or exists: " + DB_NAME);
            } catch (SQLException e) {
                System.out.println("Error creating database: " + e.getMessage());
                throw e;
            }
            
            // Connect to the database
            connection = getConnection();
            System.out.println("Connected to database: " + DB_NAME);
            
            // First, let's check if the tables exist with correct schema
            if (!tablesExistWithCorrectSchema()) {
                System.out.println("Tables don't exist or schema is incorrect. Creating tables...");
                // Drop tables if they exist with incorrect schema
                dropTables();
                // Create tables
                createTables();
            } else {
                System.out.println("Tables exist with correct schema.");
            }
            
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if tables exist with correct schema
     */
    private static boolean tablesExistWithCorrectSchema() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check tables existence
            boolean usersExist = false;
            boolean categoriesExist = false;
            boolean expensesExist = false;
            
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    if ("users".equalsIgnoreCase(tableName)) {
                        usersExist = true;
                    } else if ("categories".equalsIgnoreCase(tableName)) {
                        categoriesExist = true;
                    } else if ("expenses".equalsIgnoreCase(tableName)) {
                        expensesExist = true;
                    }
                }
            }
            
            if (!usersExist || !categoriesExist || !expensesExist) {
                return false;
            }
            
            // Verify columns in expenses table
            boolean categoryIdExists = false;
            
            try (ResultSet columns = metaData.getColumns(null, null, "expenses", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    if ("category_id".equalsIgnoreCase(columnName)) {
                        categoryIdExists = true;
                        break;
                    }
                }
            }
            
            return categoryIdExists;
            
        } catch (SQLException e) {
            System.out.println("Error checking schema: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Drops all tables if they exist
     */
    private static void dropTables() throws SQLException {
        Statement statement = connection.createStatement();
        
        // Disable foreign key checks to allow dropping tables with dependencies
        statement.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        try {
            // Drop tables in reverse order of dependencies
            statement.executeUpdate("DROP TABLE IF EXISTS expenses");
            statement.executeUpdate("DROP TABLE IF EXISTS categories");
            statement.executeUpdate("DROP TABLE IF EXISTS users");
        } finally {
            // Re-enable foreign key checks
            statement.execute("SET FOREIGN_KEY_CHECKS = 1");
            statement.close();
        }
    }
    
    /**
     * Creates all necessary tables for the expense tracker
     */
    private static void createTables() throws SQLException {
        Statement statement = connection.createStatement();
        
        // Create users table
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) UNIQUE NOT NULL," +
            "password VARCHAR(100) NOT NULL," +
            "email VARCHAR(100) UNIQUE NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );
        
        // Create expense categories table
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS categories (" +
            "category_id INT AUTO_INCREMENT PRIMARY KEY," +
            "name VARCHAR(50) NOT NULL," +
            "description VARCHAR(200)," +
            "user_id INT," +
            "is_default BOOLEAN DEFAULT FALSE," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
            ")"
        );
        
        // Create expenses table
        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS expenses (" +
            "expense_id INT AUTO_INCREMENT PRIMARY KEY," +
            "user_id INT NOT NULL," +
            "category_id INT NOT NULL," +
            "amount DECIMAL(10,2) NOT NULL," +
            "description VARCHAR(200)," +
            "expense_date DATE NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
            "FOREIGN KEY (category_id) REFERENCES categories(category_id)" +
            ")"
        );
        
        // Insert default categories
        insertDefaultCategories();
        
        statement.close();
    }
    
    /**
     * Inserts default expense categories
     */
    private static void insertDefaultCategories() throws SQLException {
        // Check if default categories already exist
        try (PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM categories WHERE is_default = TRUE")) {
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Default categories already exist
            }
        }
        
        // Insert default categories
        String[] defaultCategories = {"Food", "Transport", "Housing", "Entertainment", "Healthcare", "Education", "Shopping", "Utilities", "Other"};
        String sql = "INSERT INTO categories (name, description, is_default) VALUES (?, ?, TRUE)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (String category : defaultCategories) {
                pstmt.setString(1, category);
                pstmt.setString(2, category + " expenses");
                pstmt.executeUpdate();
            }
        }
    }
    
    /**
     * Returns a database connection
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Try to establish connection with full parameters
                connection = DriverManager.getConnection(FULL_DB_URL, DB_USER, DB_PASSWORD);
            } catch (SQLException e) {
                System.out.println("Error connecting with full URL: " + e.getMessage());
                // Fall back to simple URL
                connection = DriverManager.getConnection(DB_URL + DB_NAME, DB_USER, DB_PASSWORD);
            }
        }
        return connection;
    }
    
    /**
     * Closes the database connection
     */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Test the database connection and print diagnostic information
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("Database connection successful!");
            
            // Print MySQL version
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
                if (rs.next()) {
                    System.out.println("MySQL version: " + rs.getString(1));
                }
            }
            
            // Print table information
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Check users table
            try (ResultSet rs = metaData.getTables(null, null, "users", null)) {
                if (rs.next()) {
                    System.out.println("Users table exists");
                    printTableContent("users");
                } else {
                    System.out.println("Users table does not exist!");
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Print the content of a table for debugging
     */
    public static void printTableContent(String tableName) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName)) {
            
            System.out.println("Contents of " + tableName + " table:");
            
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            
            // Print column names
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + " | ");
            }
            System.out.println();
            
            // Print data
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + " | ");
                }
                System.out.println();
            }
            
        } catch (SQLException e) {
            System.out.println("Error printing table content: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 