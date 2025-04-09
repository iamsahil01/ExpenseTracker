# Expense Tracker

A Java Swing application for tracking personal expenses with categorization, reporting, and user management features.

## Features

- **User Management**: Register, login, and manage user accounts
- **Expense Tracking**: Add, edit, and delete expenses
- **Category Management**: Create custom expense categories
- **Reporting**: Generate visual reports (pie charts, bar charts, line charts)
- **Data Export**: Export reports to CSV
- **Settings**: Configure application preferences and manage your account

## Technologies Used

- Java 24
- Swing (UI Framework)
- MySQL (Database)
- JFreeChart (Charts and Reporting)
- Maven (Dependency Management)

## Requirements

- Java 24 or higher
- MySQL 8.0.30 or higher

## Installation

1. Clone this repository
2. Ensure you have MySQL installed and running
3. Build the project with Maven:
   ```
   mvn clean package
   ```
4. Run the application:
   ```
   java -jar target/expense-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

## Database Configuration

The application will automatically create the necessary database and tables on first run. By default, it connects to MySQL on `localhost:3306` with username `root` and password `password`.

To change these settings, modify the database connection parameters in `src/main/java/com/expensetracker/util/DatabaseUtil.java`.
