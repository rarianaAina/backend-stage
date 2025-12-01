package com.nrstudio.portail.dto.synchronisations;

import java.time.LocalDateTime;

public class StatutSynchronisationResponse {
    private String statut;
    private String message;
    private LocalDateTime timestamp;

    public StatutSynchronisationResponse(String statut, String message) {
        this.statut = statut;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et setters
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}