package com.nrstudio.portail.dto.workflow;

import java.util.List;

public class WorkflowConfigDto {
    private String typeNotificationCode;
    private List<WorkflowStepDto> steps;
    
    // Constructors
    public WorkflowConfigDto() {}
    
    public WorkflowConfigDto(String typeNotificationCode, List<WorkflowStepDto> steps) {
        this.typeNotificationCode = typeNotificationCode;
        this.steps = steps;
    }
    
    // Getters and Setters
    public String getTypeNotificationCode() { return typeNotificationCode; }
    public void setTypeNotificationCode(String typeNotificationCode) { this.typeNotificationCode = typeNotificationCode; }
    public List<WorkflowStepDto> getSteps() { return steps; }
    public void setSteps(List<WorkflowStepDto> steps) { this.steps = steps; }
}