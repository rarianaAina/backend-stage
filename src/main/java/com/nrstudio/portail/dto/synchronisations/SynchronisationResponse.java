package com.nrstudio.portail.dto.synchronisations;

import java.time.LocalDateTime;

public class SynchronisationResponse {
    private String status;
    private String message;
    private LocalDateTime timestamp;

    public SynchronisationResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}