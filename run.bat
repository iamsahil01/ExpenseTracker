@echo off
echo ====================================
echo Expense Tracker - Java 24 Application
echo ====================================

echo.
echo Building with Maven...
call mvn clean package

echo.
echo Running application...
java -jar target/expense-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
pause 