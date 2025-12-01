package com.nrstudio.portail.dto.synchronisations;

public class SimplifiedSchedulingRequest {
    private String frequency; // HOURLY, DAILY, WEEKLY, MONTHLY, etc.
    private String hour;
    private String minute;
    private String dayOfWeek;
    private String dayOfMonth;
    private String customCron;
    private boolean enabled = true;
    private String modifiedBy;
    
    // Getters et Setters
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    
    public String getHour() { return hour; }
    public void setHour(String hour) { this.hour = hour; }
    
    public String getMinute() { return minute; }
    public void setMinute(String minute) { this.minute = minute; }
    
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    
    public String getDayOfMonth() { return dayOfMonth; }
    public void setDayOfMonth(String dayOfMonth) { this.dayOfMonth = dayOfMonth; }
    
    public String getCustomCron() { return customCron; }
    public void setCustomCron(String customCron) { this.customCron = customCron; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }
}
