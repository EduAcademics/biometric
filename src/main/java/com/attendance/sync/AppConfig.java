package com.attendance.sync;

import java.util.Properties;

/**
 * Application configuration holder
 */
public class AppConfig {
    private long sleepInterval;
    private boolean debugEnabled;
    private String logLevel;
    private String[] machineIds;
    
    public AppConfig(Properties config) {
        this.sleepInterval = Long.parseLong(config.getProperty("app.sleep.interval", "60000"));
        this.debugEnabled = Boolean.parseBoolean(config.getProperty("app.debug.enabled", "true"));
        this.logLevel = config.getProperty("app.log.level", "INFO");
        
        String machineIdList = config.getProperty("machine.ids", "101,102,103,104,105,106");
        this.machineIds = machineIdList.split(",");
        for (int i = 0; i < machineIds.length; i++) {
            machineIds[i] = machineIds[i].trim();
        }
    }
    
    // Getters
    public long getSleepInterval() { return sleepInterval; }
    public boolean isDebugEnabled() { return debugEnabled; }
    public String getLogLevel() { return logLevel; }
    public String[] getMachineIds() { return machineIds; }
}