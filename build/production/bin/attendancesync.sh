#!/bin/bash

# =================================================================
# AttendanceSync Production Startup Script
# =================================================================

# Set the application directory
APP_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
LOG_DIR="$APP_DIR/logs"
PID_FILE="$APP_DIR/logs/attendancesync.pid"

# Ensure logs directory exists
mkdir -p "$LOG_DIR"

# Function to start the application
start() {
    echo "Starting AttendanceSync..."
    
    # Check if already running
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "AttendanceSync is already running (PID: $PID)"
            return 1
        else
            echo "Removing stale PID file..."
            rm -f "$PID_FILE"
        fi
    fi
    
    # Set environment variables if they exist
    if [ -f "$APP_DIR/config/environment.env" ]; then
        echo "Loading environment variables from config/environment.env"
        export $(grep -v '^#' "$APP_DIR/config/environment.env" | xargs)
    fi
    
    # Start the application in background
    cd "$APP_DIR"
    nohup java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync > logs/application.out 2>&1 &
    
    # Save PID
    echo $! > "$PID_FILE"
    echo "AttendanceSync started with PID: $!"
    echo "Logs: $LOG_DIR/attendance-sync.log"
    echo "Application output: $LOG_DIR/application.out"
    echo ""
    echo "Waiting for application to initialize..."
    sleep 3

    # Show startup logs automatically
    echo "==============================================="
    echo "Showing live startup logs (Press Ctrl+C to exit):"
    echo "==============================================="
    
    # Show initial application output if it exists
    if [ -f "$LOG_DIR/application.out" ]; then
        sleep 2
        echo "--- Initial Application Output ---"
        cat "$LOG_DIR/application.out"
        echo ""
    fi
    
    # Wait for log file to be created and show live logs
    echo "--- Live Application Logs ---"
    timeout_count=0
    while [ ! -f "$LOG_DIR/attendance-sync.log" ] && [ $timeout_count -lt 10 ]; do
        echo "Waiting for log file to be created... ($((timeout_count + 1))/10)"
        sleep 1
        timeout_count=$((timeout_count + 1))
    done
    
    if [ -f "$LOG_DIR/attendance-sync.log" ]; then
        echo "Log file created. Showing live logs (Press Ctrl+C to exit):"
        echo "==============================================="
        # Follow the log file in real-time
        tail -f "$LOG_DIR/attendance-sync.log"
    else
        echo "Log file not created within 10 seconds."
        echo "Check application.out for startup messages:"
        if [ -f "$LOG_DIR/application.out" ]; then
            cat "$LOG_DIR/application.out"
        fi
        echo ""
        echo "==============================================="
        echo "AttendanceSync started. To view logs later, use:"
        echo "  $(basename "$0") logs"
        echo "==============================================="
    fi
}

# Function to stop the application
stop() {
    echo "Stopping AttendanceSync..."
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            kill $PID
            echo "Waiting for process to stop..."
            
            # Wait up to 30 seconds for graceful shutdown
            for i in {1..30}; do
                if ! ps -p $PID > /dev/null 2>&1; then
                    echo "AttendanceSync stopped successfully"
                    rm -f "$PID_FILE"
                    return 0
                fi
                sleep 1
            done
            
            # Force kill if still running
            echo "Force killing process..."
            kill -9 $PID
            rm -f "$PID_FILE"
            echo "AttendanceSync force stopped"
        else
            echo "AttendanceSync is not running"
            rm -f "$PID_FILE"
        fi
    else
        echo "PID file not found. AttendanceSync may not be running."
    fi
}

# Function to check status
status() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "AttendanceSync is running (PID: $PID)"
            return 0
        else
            echo "AttendanceSync is not running (stale PID file found)"
            return 1
        fi
    else
        echo "AttendanceSync is not running"
        return 1
    fi
}

# Function to restart the application
restart() {
    stop
    sleep 2
    start
}

# Function to show logs
logs() {
    if [ -f "$LOG_DIR/attendance-sync.log" ]; then
        tail -f "$LOG_DIR/attendance-sync.log"
    else
        echo "Log file not found: $LOG_DIR/attendance-sync.log"
    fi
}

# Function to test database connection
test_connection() {
    echo "Testing database connection..."
    cd "$APP_DIR"
    java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync --test-connection
}

# Function to test API connectivity
test_api() {
    echo "Testing API connectivity..."
    cd "$APP_DIR"
    java $JAVA_OPTS -cp "bin/AttendanceSync.jar:lib/*" com.attendance.sync.AttendanceSync --test-api
}

# Main script logic
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    test-connection)
        test_connection
        ;;
    test-api)
        test_api
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|status|logs|test-connection|test-api}"
        echo ""
        echo "Commands:"
        echo "  start           Start AttendanceSync service"
        echo "  stop            Stop AttendanceSync service"
        echo "  restart         Restart AttendanceSync service"
        echo "  status          Show service status"
        echo "  logs            Show real-time logs"
        echo "  test-connection Test database connection"
        echo "  test-api        Test API connectivity"
        exit 1
        ;;
esac

exit $?
