package com.attendance.sync;

import java.util.Properties;

/**
 * Database configuration holder
 */
public class DatabaseConfig {
    private String host;
    private String port;
    private String databaseName;
    private String username;
    private String password;
    
    public DatabaseConfig(Properties config) {
        this.host = config.getProperty("db.host", "localhost");
        this.port = config.getProperty("db.port", "1433");
        this.databaseName = config.getProperty("db.name", "Realtime");
        this.username = config.getProperty("db.username", "sa");
        this.password = config.getProperty("db.password", "");
    }
    
    // Getters
    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getDatabaseName() { return databaseName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}