package com.attendance.sync;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * AttendanceSync - Biometric Attendance Synchronization System
 * 
 * This application synchronizes biometric punch data from local database
 * to remote web service APIs for school management systems.
 * 
 * @author AttendanceSync Team
 * @version 2.0
 */
public class AttendanceSync implements Runnable {
  
  private static final Logger logger = Logger.getLogger(AttendanceSync.class.getName());
  
  // Configuration properties
  private Properties config;
  private DatabaseConfig dbConfig;
  private SchoolConfig schoolConfig;
  private ApiConfig apiConfig;
  private AppConfig appConfig;
  
  /**
   * Constructor - loads configuration from properties file
   */
  public AttendanceSync() {
    setupLogging();
    loadConfiguration();
  }
  
  /**
   * Setup logging configuration
   */
  private void setupLogging() {
    try {
      FileHandler fileHandler = new FileHandler(Constants.DEFAULT_LOG_PATH, true);
      fileHandler.setFormatter(new SimpleFormatter());
      logger.addHandler(fileHandler);
      logger.setLevel(Level.INFO);
    } catch (IOException e) {
      System.err.println("Failed to setup logging: " + e.getMessage());
    }
  }
  
  /**
   * Load configuration from properties file
   */
  private void loadConfiguration() {
    config = new Properties();
    try {
      FileInputStream configFile = new FileInputStream(Constants.DEFAULT_CONFIG_PATH);
      config.load(configFile);
      configFile.close();
      
      // Load configuration objects
      dbConfig = new DatabaseConfig(config);
      schoolConfig = new SchoolConfig(config);
      apiConfig = new ApiConfig(config);
      appConfig = new AppConfig(config);
      
      logger.info(Constants.LOG_CONFIG_LOADED);
      logger.info(Constants.LOG_SCHOOL_PREFIX + schoolConfig.getSchoolName());
      logger.info(Constants.LOG_DATABASE_PREFIX + dbConfig.getHost() + ":" + dbConfig.getPort());
      
    } catch (IOException e) {
      logger.severe(Constants.ERROR_CONFIG_LOAD + e.getMessage());
      System.err.println("Error loading configuration file. Creating default...");
      createDefaultConfiguration();
      loadConfiguration(); // Retry with default config
    }
  }
  
  /**
   * Create default configuration file
   */
  private void createDefaultConfiguration() {
    Properties defaultConfig = new Properties();
    
    // Database defaults
    defaultConfig.setProperty("db.host", "localhost");
    defaultConfig.setProperty("db.port", "1433");
    defaultConfig.setProperty("db.name", "Realtime");
    defaultConfig.setProperty("db.username", "sa");
    defaultConfig.setProperty("db.password", "password");
    
    // School defaults
    defaultConfig.setProperty("school.code", "demo");
    defaultConfig.setProperty("school.name", "Demo School");
    
    // API defaults
    defaultConfig.setProperty("api.primary.url", "https://api.example.com/v1/attendance");
    defaultConfig.setProperty("api.fallback.url", "");
    defaultConfig.setProperty("api.timeout", "30000");
    
    // Application defaults
    defaultConfig.setProperty("app.sleep.interval", "60000");
    defaultConfig.setProperty("app.debug.enabled", "true");
    defaultConfig.setProperty("app.log.level", "INFO");
    
    // Machine defaults
    defaultConfig.setProperty("machine.ids", "101,102,103,104,105,106");
    
    try {
      defaultConfig.store(new java.io.FileOutputStream(Constants.DEFAULT_CONFIG_PATH),
                         "Default AttendanceSync Configuration");
      System.out.println("✅ Default configuration created at " + Constants.DEFAULT_CONFIG_PATH);
    } catch (IOException e) {
      System.err.println("Failed to create default configuration: " + e.getMessage());
    }
  }
  
  /**
   * Get database connection using configured parameters
   */
  public Connection getConnection() {
    Connection con = null;
    try {
      String connectionUrl = String.format(Constants.DB_URL_TEMPLATE, 
                                          dbConfig.getHost(), 
                                          dbConfig.getPort(), 
                                          dbConfig.getDatabaseName());
      
      // SSL configuration
      System.setProperty(Constants.SYSTEM_PROP_FIPS_PROVIDER, "false");
      System.setProperty(Constants.SYSTEM_PROP_HTTPS_PROTOCOLS, Constants.SYSTEM_PROP_PROTOCOLS_VALUE);
      
      con = DriverManager.getConnection(connectionUrl, dbConfig.getUsername(), dbConfig.getPassword());
      
      if (appConfig.isDebugEnabled()) {
        logger.info(Constants.LOG_DB_CONNECTION_SUCCESS);
      }
    } catch (Exception e) {
      logger.severe(Constants.LOG_DB_CONNECTION_FAILED + e.getMessage());
      if (appConfig.isDebugEnabled()) {
        e.printStackTrace();
      }
    } 
    return con;
  }
  
  /**
   * Main entry point
   */
  public static void main(String[] args) {
    try {
      System.out.println(Constants.CONSOLE_APP_STARTING);
      AttendanceSync app = new AttendanceSync();
      
      // Check for command line arguments
      if (args.length > 0) {
        switch (args[0]) {
          case Constants.CMD_TEST_CONNECTION:
            app.testConnection();
            return;
          case Constants.CMD_TEST_API:
            app.testApi();
            return;
          case Constants.CMD_HELP:
            app.showHelp();
            return;
        }
      }
      
      Thread t = new Thread(app);
      t.start();
    } catch (Exception e) {
      System.err.println(Constants.ERROR_APP_STARTUP + e.getMessage());
      e.printStackTrace();
    } 
  }
  
  /**
   * Test database connection
   */
  public void testConnection() {
    System.out.println("🔌 Testing database connection...");
    Connection con = getConnection();
    if (con != null) {
      System.out.println("✅ Database connection successful!");
      try {
        con.close();
      } catch (SQLException e) {
        // Ignore
      }
    } else {
      System.out.println("❌ Database connection failed!");
    }
  }
  
  /**
   * Test API connectivity
   */
  public void testApi() {
    System.out.println("🌐 Testing API connectivity...");
    try {
      JSONObject testData = new JSONObject();
      testData.put("test", "true");
      String url = apiConfig.getPrimaryUrl() + "?school_code=" + schoolConfig.getSchoolCode();
      String response = hitServer(url);
      System.out.println("✅ API test completed. Response: " + response);
    } catch (Exception e) {
      System.out.println("❌ API test failed: " + e.getMessage());
    }
  }
  
  /**
   * Show help information
   */
  public void showHelp() {
    System.out.println("AttendanceSync - Biometric Attendance Synchronization System");
    System.out.println("============================================================");
    System.out.println("");
    System.out.println("Usage: java -jar AttendanceSync.jar [OPTIONS]");
    System.out.println("");
    System.out.println("Options:");
    System.out.println("  --test-connection    Test database connectivity");
    System.out.println("  --test-api          Test API connectivity");
    System.out.println("  --help              Show this help message");
    System.out.println("");
    System.out.println("Configuration:");
    System.out.println("  Edit config/application.properties to modify settings");
    System.out.println("");
    System.out.println("Logs:");
    System.out.println("  Application logs are stored in logs/attendance-sync.log");
  }
  
  /**
   * Fetch and process attendance records
   */
  public synchronized void fetchRecord() throws SQLException {
    Connection con = null;
    try {
      con = getConnection();
      if (con == null) {
        logger.severe(Constants.ERROR_DB_NULL);
        return;
      }
      
      String sql = Constants.FETCH_UNPROCESSED_RECORDS_SQL;
      
      PreparedStatement ps = con.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      
      int recordCount = 0;
      while (rs.next()) {
        recordCount++;
        try {
          processRecord(rs, con);
        } catch (Exception e) {
          logger.severe("Error processing record: " + e.getMessage());
          if (appConfig.isDebugEnabled()) {
            e.printStackTrace();
          }
        } 
      } 
      
      logger.info("Total records processed: " + recordCount);
      if (recordCount == 0) {
        logger.info("No unprocessed records found");
      }
      
    } catch (Exception e) {
      logger.severe("Exception in fetchRecord(): " + e.getMessage());
      if (appConfig.isDebugEnabled()) {
        e.printStackTrace();
      }
    } finally {
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) {
          logger.warning("Error closing connection: " + e.getMessage());
        }
      }
    }
  }
  
  /**
   * Process individual attendance record
   */
  private void processRecord(ResultSet rs, Connection con) throws Exception {
    // Extract data from result set
    String machineId = rs.getString("MachineNo");
    String cardNo = rs.getString("CardNo");
    String punchDateTime = rs.getString("PunchDatetime");
    
    // Format data
    Date dateObject = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(punchDateTime);
    String formattedDateTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(dateObject);
    String formattedCardNo = String.format("%08d", Integer.parseInt(cardNo));
    
    logger.info("Processing: Employee " + formattedCardNo + " at " + formattedDateTime + " on Machine " + machineId);
    
    // Create JSON payload
    JSONObject record = new JSONObject();
    record.put("biomatric_code", formattedCardNo);
    record.put("school_code", schoolConfig.getSchoolCode());
    record.put("datetime", formattedDateTime);
    
    JSONArray list = new JSONArray();
    list.add(record);
    
    JSONObject json = new JSONObject();
    json.put("data", list);
    
    // Send to API
    try {
      String url = buildURL(machineId, json);
      logger.info("Sending attendance data to server for Machine: " + machineId);
      
      String response = hitServer(url);
      
      if (appConfig.isDebugEnabled()) {
        logger.info("API response: " + response);
      }
      
      boolean shouldUpdate = analyzeResponse(response);
      
      if (shouldUpdate) {
        updateDatabase(con, punchDateTime, cardNo, machineId);
        logger.info("✅ Record successfully synced for Employee " + formattedCardNo);
      } else {
        logger.warning("⚠️  Record not synced - will retry in next cycle for Employee " + formattedCardNo);
      }
    } catch (IllegalArgumentException e) {
      logger.warning("❌ Configuration issue: " + e.getMessage());
      logger.warning("⚠️  Skipping record for Employee " + formattedCardNo + " - check machine.ids configuration");
    } catch (Exception e) {
      logger.severe("❌ Error sending data to server: " + e.getMessage());
      if (appConfig.isDebugEnabled()) {
        e.printStackTrace();
      }
      logger.warning("⚠️  Record not synced - will retry in next cycle for Employee " + formattedCardNo);
    }
  }
  
  /**
   * Analyze API response to determine if database should be updated
   */
  private boolean analyzeResponse(String response) {
    if (response == null || response.length() == 0) {
      logger.warning("Empty response received - will retry in next cycle");
      return false;
    }
    
    // Check for success cases
    if (response.contains("\"status\":\"success\"") || 
        response.contains("successfully") || 
        response.contains("Successfully")) {
      logger.info("SUCCESS: Attendance recorded successfully");
      return true;
    }
    
    // Check for employee not found (also considered processed)
    if (response.contains("Employee not found")) {
      logger.info("INFO: Employee not found, marking as processed");
      return true;
    }
    
    // Check for HTTP errors
    if (response.startsWith("HTTP_ERROR_") || 
        response.equals("CONNECTION_FAILED") || 
        response.equals("TIMEOUT")) {
      logger.warning("Network issue - will retry in next cycle");
      return false;
    }
    
    logger.warning("Unexpected response format: " + response);
    return false;
  }
  
  /**
   * Update database record as processed
   */
  private void updateDatabase(Connection con, String punchDateTime, String cardNo, String machineId) 
      throws SQLException {
    String updateSql = Constants.UPDATE_PROCESSED_RECORD_SQL;
    
    PreparedStatement updatePs = con.prepareStatement(updateSql);
    updatePs.setString(1, punchDateTime);
    updatePs.setString(2, cardNo);
    updatePs.setString(3, machineId);
    
    int rowsUpdated = updatePs.executeUpdate();
    logger.info(Constants.LOG_DATABASE_UPDATED + rowsUpdated);
    updatePs.close();
  }
  
  /**
   * Build API URL for the given machine ID and JSON data
   */
  private String buildURL(String machineId, JSONObject json) throws Exception {
    if (machineId == null || machineId.isEmpty() || json == null) {
      throw new IllegalArgumentException("MachineId and JSON data cannot be null or empty");
    }
    
    // Check if machine ID is configured
    String[] configuredIds = appConfig.getMachineIds();
    boolean found = false;
    for (String id : configuredIds) {
      if (id.trim().equals(machineId)) {
        found = true;
        break;
      }
    }
    
    if (!found) {
      throw new IllegalArgumentException("Unknown MachineId: " + machineId + 
                                       ". Configured IDs: " + String.join(",", configuredIds));
    }
    
    String baseUrl = apiConfig.getPrimaryUrl() + "?school_code=" + schoolConfig.getSchoolCode() + "&attendancedata=";
    return baseUrl + URLEncoder.encode(json.toString(), "UTF-8");
  }
  
  /**
   * Send HTTP request to server
   */
  public String hitServer(String URL) {
    DefaultHttpClient httpClient = null;
    try {
      if (appConfig.isDebugEnabled()) {
        logger.info("Sending request to: " + URL);
      }
      
      httpClient = new DefaultHttpClient();
      
      // Set timeouts
      httpClient.getParams().setParameter("http.socket.timeout", apiConfig.getTimeout());
      httpClient.getParams().setParameter("http.connection.timeout", apiConfig.getTimeout());
      
      HttpGet httpGet = new HttpGet(URL);
      BasicResponseHandler responseHandler = new BasicResponseHandler();
      String response = httpClient.execute(httpGet, responseHandler);
      
      if (appConfig.isDebugEnabled()) {
        logger.info("Server response received: " + response);
      }
      
      return response;
    } catch (org.apache.http.client.HttpResponseException e) {
      logger.warning("HTTP Error " + e.getStatusCode() + ": " + e.getMessage());
      return "HTTP_ERROR_" + e.getStatusCode();
    } catch (java.net.ConnectException e) {
      logger.warning("Connection failed: " + e.getMessage());
      return "CONNECTION_FAILED";
    } catch (java.net.SocketTimeoutException e) {
      logger.warning("Request timeout: " + e.getMessage());
      return "TIMEOUT";
    } catch (Exception e) {
      logger.severe("Unexpected error: " + e.getMessage());
      if (appConfig.isDebugEnabled()) {
        e.printStackTrace();
      }
      return "ERROR";
    } finally {
      if (httpClient != null) {
        try {
          httpClient.getConnectionManager().shutdown();
        } catch (Exception e) {
          // Ignore cleanup errors
        }
      }
    }
  }
  
  /**
   * Main application run loop
   */
  @Override
  public void run() {
    try {
      logger.info("AttendanceSync started for " + schoolConfig.getSchoolName());
      
      while (true) {
        logger.info("Starting sync cycle...");
        fetchRecord();
        
        logger.info("Sleeping for " + (appConfig.getSleepInterval() / 1000) + " seconds...");
        Thread.sleep(appConfig.getSleepInterval());
      }
    } catch (InterruptedException e) {
      logger.info("Application interrupted, shutting down gracefully...");
    } catch (Exception e) {
      logger.severe("Application error: " + e.getMessage());
      if (appConfig.isDebugEnabled()) {
        e.printStackTrace();
      }
    }
  }
}