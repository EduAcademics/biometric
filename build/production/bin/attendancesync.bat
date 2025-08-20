@echo off
REM =================================================================
REM AttendanceSync Production Startup Script for Windows
REM =================================================================

setlocal enabledelayedexpansion

REM Set the application directory
set "APP_DIR=%~dp0.."
set "JAVA_OPTS=-Xms256m -Xmx512m -XX:+UseG1GC"
set "LOG_DIR=%APP_DIR%\logs"
set "PID_FILE=%APP_DIR%\logs\attendancesync.pid"

REM Ensure logs directory exists
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM Check command line argument
if "%1"=="" goto usage
if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="restart" goto restart
if "%1"=="status" goto status
if "%1"=="logs" goto logs
if "%1"=="test-connection" goto test_connection
if "%1"=="test-api" goto test_api
goto usage

:start
echo Starting AttendanceSync...

REM Check if already running
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo AttendanceSync is already running (PID: !PID!)
        goto end
    ) else (
        echo Removing stale PID file...
        del "%PID_FILE%" 2>nul
    )
)

REM Load environment variables if they exist
if exist "%APP_DIR%\config\environment.env" (
    echo Loading environment variables from config\environment.env
    for /f "usebackq tokens=1,2 delims==" %%a in ("%APP_DIR%\config\environment.env") do (
        if not "%%a"=="" if not "%%a:~0,1%"=="#" (
            set "%%a=%%b"
        )
    )
)

REM Start the application in background
cd /d "%APP_DIR%"
start /b java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync > logs\application.out 2>&1

REM Get the PID of the started process
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo csv ^| find "java.exe"') do (
    set "NEW_PID=%%~i"
    goto found_pid
)
:found_pid

REM Save PID
echo !NEW_PID! > "%PID_FILE%"
echo AttendanceSync started with PID: !NEW_PID!
echo Logs: %LOG_DIR%\attendance-sync.log
echo Application output: %LOG_DIR%\application.out
echo.
echo Waiting for application to initialize...
timeout /t 3 /nobreak >nul

REM Show startup logs automatically
echo ===============================================
echo Showing live startup logs (Press Ctrl+C to exit):
echo ===============================================

REM Show initial application output if it exists
if exist "%LOG_DIR%\application.out" (
    timeout /t 2 /nobreak >nul
    echo --- Initial Application Output ---
    type "%LOG_DIR%\application.out"
    echo.
)

REM Wait for log file to be created and show live logs
echo --- Live Application Logs ---
set timeout_count=0
:wait_for_log
if exist "%LOG_DIR%\attendance-sync.log" goto show_live_logs
if !timeout_count! geq 10 goto log_timeout
set /a timeout_count+=1
echo Waiting for log file to be created... (!timeout_count!/10)
timeout /t 1 /nobreak >nul
goto wait_for_log

:show_live_logs
echo Log file created. Showing live logs (Press Ctrl+C to exit):
echo ===============================================
REM Use PowerShell Get-Content -Wait for live log following (equivalent to tail -f)
powershell -Command "Get-Content '%LOG_DIR%\attendance-sync.log' -Wait"
goto end

:log_timeout
echo Log file not created within 10 seconds.
echo Check application.out for startup messages:
if exist "%LOG_DIR%\application.out" (
    type "%LOG_DIR%\application.out"
)
echo.
echo ===============================================
echo AttendanceSync started. To view logs later, use:
echo   %~nx0 logs
echo ===============================================
goto end

:stop
echo Stopping AttendanceSync...

if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo Terminating process !PID!...
        taskkill /pid !PID! /f >nul
        echo Waiting for process to stop...
        timeout /t 3 /nobreak >nul
        echo AttendanceSync stopped successfully
        del "%PID_FILE%" 2>nul
    ) else (
        echo AttendanceSync is not running
        del "%PID_FILE%" 2>nul
    )
) else (
    echo PID file not found. AttendanceSync may not be running.
)
goto end

:status
if exist "%PID_FILE%" (
    set /p PID=<"%PID_FILE%"
    tasklist /fi "PID eq !PID!" 2>nul | find /i "java.exe" >nul
    if !errorlevel! equ 0 (
        echo AttendanceSync is running (PID: !PID!)
    ) else (
        echo AttendanceSync is not running (stale PID file found)
    )
) else (
    echo AttendanceSync is not running
)
goto end

:restart
call :stop
timeout /t 2 /nobreak >nul
call :start
goto end

:logs
if exist "%LOG_DIR%\attendance-sync.log" (
    echo Showing last 50 lines of log file. Press Ctrl+C to exit.
    type "%LOG_DIR%\attendance-sync.log" | more
) else (
    echo Log file not found: %LOG_DIR%\attendance-sync.log
)
goto end

:test_connection
echo Testing database connection...
cd /d "%APP_DIR%"
java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync --test-connection
goto end

:test_api
echo Testing API connectivity...
cd /d "%APP_DIR%"
java %JAVA_OPTS% -cp "bin\AttendanceSync.jar;lib\*" com.attendance.sync.AttendanceSync --test-api
goto end

:usage
echo Usage: %0 {start^|stop^|restart^|status^|logs^|test-connection^|test-api}
echo.
echo Commands:
echo   start           Start AttendanceSync service
echo   stop            Stop AttendanceSync service
echo   restart         Restart AttendanceSync service
echo   status          Show service status
echo   logs            Show application logs
echo   test-connection Test database connection
echo   test-api        Test API connectivity
echo.
echo Example: attendancesync.bat start

:end
endlocal
