package com.attendance.sync;

import java.util.Properties;

/**
 * School configuration holder
 */
public class SchoolConfig {
    private String schoolCode;
    private String schoolName;
    
    public SchoolConfig(Properties config) {
        this.schoolCode = config.getProperty("school.code", "demo");
        this.schoolName = config.getProperty("school.name", "Demo School");
    }
    
    // Getters
    public String getSchoolCode() { return schoolCode; }
    public String getSchoolName() { return schoolName; }
}