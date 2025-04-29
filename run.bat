@echo off
cd /d "%~dp0"
echo ====================================
echo Expense Tracker - Java 24 Application
echo ====================================

echo.
echo Building with Maven...
call mvn clean package
@echo off
echo ====================================
echo Expense Tracker - Java 24 Application
echo ====================================

echo.
echo Checking system requirements...

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 24 or higher from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    echo After installation, add Maven's bin directory to your PATH
    pause
    exit /b 1
)

echo.
echo Building with Maven...
call mvn clean package

if %ERRORLEVEL% neq 0 (
    echo Error: Maven build failed
    pause
    exit /b 1
)

echo.
echo Running application...
if not exist "target\expense-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar" (
    echo Error: JAR file not found
    echo Please make sure the build was successful
    pause
    exit /b 1
)

java -jar target\expense-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
pause 
echo.
echo Running application...
java -jar target/expense-tracker-1.0-SNAPSHOT-jar-with-dependencies.jar

echo.
pause 
