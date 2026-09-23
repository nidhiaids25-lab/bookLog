@echo off
REM ==============================================================================
REM Smart Library System - Build & Run Script (Windows)
REM ==============================================================================

echo ==================================================================
echo           SMART LIBRARY SYSTEM - LAUNCHER
echo ==================================================================

if not exist "bin" mkdir bin

echo Compiling Java sources...
dir /s /B src\*.java > sources.txt
javac -cp "lib\*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo Starting Smart Library System...
echo.

java -cp "bin;lib\*" Main %*
pause
