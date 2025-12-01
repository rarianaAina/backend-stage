package com.nrstudio.portail.domaine.synchronisation;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduling_configuration")
public class SchedulingConfiguration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "job_name", unique = true, nullable = false, length = 100)
    private String jobName;
    
    @Column(name = "job_description", length = 255)
    private String jobDescription;
    
    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;
    
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    
    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;
    
    @Column(name = "last_modified_by", length = 100)
    private String lastModifiedBy;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    // Constructeurs
    public SchedulingConfiguration() {
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    public SchedulingConfiguration(String jobName, String jobDescription, String cronExpression, 
                                 String displayName, String scheduleType) {
        this();
        this.jobName = jobName;
        this.jobDescription = jobDescription;
        this.cronExpression = cronExpression;
        this.displayName = displayName;
        this.scheduleType = scheduleType;
    }
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    
    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
    
    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
}