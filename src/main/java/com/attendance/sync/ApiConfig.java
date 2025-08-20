package com.attendance.sync;

import java.util.Properties;

/**
 * API configuration holder
 */
public class ApiConfig {
    private String primaryUrl;
    private String fallbackUrl;
    private int timeout;
    
    public ApiConfig(Properties config) {
        this.primaryUrl = config.getProperty("api.primary.url", "");
        this.fallbackUrl = config.getProperty("api.fallback.url", "");
        this.timeout = Integer.parseInt(config.getProperty("api.timeout", "30000"));
    }
    
    // Getters
    public String getPrimaryUrl() { return primaryUrl; }
    public String getFallbackUrl() { return fallbackUrl; }
    public int getTimeout() { return timeout; }
}