package com.nrstudio.portail.dto.synchronisations;

public class CreateSimplifiedSchedulingRequest extends SimplifiedSchedulingRequest {
    private String jobName;
    private String jobDescription;
    
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
}