package com.attendance.sync;

/**
 * Application constants that can be externalized for production
 */
public class Constants {
    
    // Application Information
    public static final String APP_NAME = "AttendanceSync";
    public static final String APP_VERSION = "2.0";
    public static final String APP_DESCRIPTION = "Biometric Attendance Synchronization System";
    
    // Default Configuration Paths
    public static final String DEFAULT_CONFIG_PATH = "config/application.properties";
    public static final String DEFAULT_LOG_PATH = "logs/attendance-sync.log";
    
    // Database Constants
    public static final String DB_DRIVER_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String DB_URL_TEMPLATE = "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;trustServerCertificate=true;integratedSecurity=false;applicationIntent=ReadWrite;loginTimeout=30;";
    
    // SQL Queries
    public static final String FETCH_UNPROCESSED_RECORDS_SQL = 
        "SELECT MachineNo, CardNo, PunchDatetime FROM Tran_MachineRawPunch " +
        "WHERE (IsSync IS NULL OR IsSync = 0) ORDER BY PunchDatetime ASC";
    
    public static final String UPDATE_PROCESSED_RECORD_SQL = 
        "UPDATE dbo.Tran_MachineRawPunch SET IsSync=1 " +
        "WHERE PunchDatetime=? AND CardNo=? AND MachineNo=?";
    
    // Date Format Constants
    public static final String INPUT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String OUTPUT_DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String CARD_NUMBER_FORMAT = "%08d";
    
    // API Constants
    public static final String API_PARAM_SCHOOL_CODE = "school_code";
    public static final String API_PARAM_ATTENDANCE_DATA = "attendancedata";
    public static final String ENCODING_UTF8 = "UTF-8";
    
    // HTTP Client Constants
    public static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
    public static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
    
    // Response Analysis Constants
    public static final String RESPONSE_STATUS_SUCCESS = "\"status\":\"success\"";
    public static final String RESPONSE_SUCCESSFULLY = "successfully";
    public static final String RESPONSE_SUCCESSFULLY_CAPS = "Successfully";
    public static final String RESPONSE_EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String RESPONSE_HTTP_ERROR_PREFIX = "HTTP_ERROR_";
    public static final String RESPONSE_CONNECTION_FAILED = "CONNECTION_FAILED";
    public static final String RESPONSE_TIMEOUT = "TIMEOUT";
    public static final String RESPONSE_ERROR = "ERROR";
    
    // System Properties
    public static final String SYSTEM_PROP_FIPS_PROVIDER = "com.microsoft.sqlserver.jdbc.fipsProvider";
    public static final String SYSTEM_PROP_HTTPS_PROTOCOLS = "https.protocols";
    public static final String SYSTEM_PROP_PROTOCOLS_VALUE = "TLSv1,TLSv1.1,TLSv1.2";
    
    // Log Messages
    public static final String LOG_CONFIG_LOADED = "Configuration loaded successfully";
    public static final String LOG_SCHOOL_PREFIX = "School: ";
    public static final String LOG_DATABASE_PREFIX = "Database: ";
    public static final String LOG_APP_STARTED = "AttendanceSync started for ";
    public static final String LOG_SYNC_CYCLE_START = "Starting sync cycle...";
    public static final String LOG_SLEEPING_PREFIX = "Sleeping for ";
    public static final String LOG_SLEEPING_SUFFIX = " seconds...";
    public static final String LOG_DB_CONNECTION_SUCCESS = "Database connection successful!";
    public static final String LOG_DB_CONNECTION_FAILED = "Database connection failed: ";
    public static final String LOG_PROCESSING_PREFIX = "Processing: Employee ";
    public static final String LOG_API_RESPONSE_PREFIX = "API response: ";
    public static final String LOG_SUCCESS_RECORDED = "SUCCESS: Attendance recorded successfully";
    public static final String LOG_EMPLOYEE_NOT_FOUND = "INFO: Employee not found, marking as processed";
    public static final String LOG_DATABASE_UPDATED = "Database updated successfully! Rows: ";
    public static final String LOG_TOTAL_RECORDS = "Total records processed: ";
    public static final String LOG_NO_RECORDS = "No unprocessed records found";
    public static final String LOG_SENDING_REQUEST = "Sending request to: ";
    public static final String LOG_RESPONSE_RECEIVED = "Server response received: ";
    
    // Error Messages
    public static final String ERROR_CONFIG_LOAD = "Error loading configuration: ";
    public static final String ERROR_DB_NULL = "Database connection is null, cannot proceed!";
    public static final String ERROR_PROCESSING_RECORD = "Error processing record: ";
    public static final String ERROR_FETCH_RECORD = "Exception in fetchRecord(): ";
    public static final String ERROR_CLOSING_CONNECTION = "Error closing connection: ";
    public static final String ERROR_EMPTY_RESPONSE = "Empty response received - will retry in next cycle";
    public static final String ERROR_NETWORK_ISSUE = "Network issue - will retry in next cycle";
    public static final String ERROR_UNEXPECTED_RESPONSE = "Unexpected response format: ";
    public static final String ERROR_URL_GENERATION = "URL generation failed for MachineId: ";
    public static final String ERROR_INVALID_PARAMS = "MachineId and JSON data cannot be null or empty";
    public static final String ERROR_UNKNOWN_MACHINE = "Unknown MachineId: ";
    public static final String ERROR_CONFIGURED_IDS = ". Configured IDs: ";
    public static final String ERROR_HTTP_PREFIX = "HTTP Error ";
    public static final String ERROR_CONNECTION_FAILED_MSG = "Connection failed: ";
    public static final String ERROR_TIMEOUT_MSG = "Request timeout: ";
    public static final String ERROR_UNEXPECTED = "Unexpected error: ";
    public static final String ERROR_APP_STARTUP = "Application startup failed: ";
    public static final String ERROR_APP_ERROR = "Application error: ";
    public static final String ERROR_APP_INTERRUPTED = "Application interrupted, shutting down gracefully...";
    
    // Console Messages
    public static final String CONSOLE_APP_STARTING = "🚀 Starting AttendanceSync Application...";
    public static final String CONSOLE_TEST_CONNECTION = "🔌 Testing database connection...";
    public static final String CONSOLE_CONNECTION_SUCCESS = "✅ Database connection successful!";
    public static final String CONSOLE_CONNECTION_FAILED = "❌ Database connection failed!";
    public static final String CONSOLE_TEST_API = "🌐 Testing API connectivity...";
    public static final String CONSOLE_API_TEST_COMPLETED = "✅ API test completed. Response: ";
    public static final String CONSOLE_API_TEST_FAILED = "❌ API test failed: ";
    
    // Help Text
    public static final String HELP_TITLE = "AttendanceSync - Biometric Attendance Synchronization System";
    public static final String HELP_SEPARATOR = "============================================================";
    public static final String HELP_USAGE = "Usage: java -jar AttendanceSync.jar [OPTIONS]";
    public static final String HELP_OPTIONS = "Options:";
    public static final String HELP_TEST_CONNECTION = "  --test-connection    Test database connectivity";
    public static final String HELP_TEST_API = "  --test-api          Test API connectivity";
    public static final String HELP_HELP = "  --help              Show this help message";
    public static final String HELP_CONFIG = "Configuration:";
    public static final String HELP_CONFIG_EDIT = "  Edit config/application.properties to modify settings";
    public static final String HELP_LOGS = "Logs:";
    public static final String HELP_LOGS_LOCATION = "  Application logs are stored in logs/attendance-sync.log";
    
    // JSON Field Names
    public static final String JSON_FIELD_BIOMETRIC_CODE = "biomatric_code";
    public static final String JSON_FIELD_SCHOOL_CODE = "school_code";
    public static final String JSON_FIELD_DATETIME = "datetime";
    public static final String JSON_FIELD_DATA = "data";
    public static final String JSON_FIELD_TEST = "test";
    
    // Command Line Arguments
    public static final String CMD_TEST_CONNECTION = "--test-connection";
    public static final String CMD_TEST_API = "--test-api";
    public static final String CMD_HELP = "--help";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
