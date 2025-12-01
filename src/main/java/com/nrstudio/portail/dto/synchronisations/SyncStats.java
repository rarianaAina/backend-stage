package com.nrstudio.portail.dto.synchronisations;

import java.time.LocalDateTime;

import com.nrstudio.portail.services.synchronisations.configurations.constants.SyncType;

public class SyncStats {
    private final String entite;
    private final SyncType type;
    private final LocalDateTime debut;
    private LocalDateTime fin;
    private int elementsTraites;
    private int elementsErrones;
    
    public SyncStats(String entite, SyncType type) {
        this.entite = entite;
        this.type = type;
        this.debut = LocalDateTime.now();
    }
    
    // Getters
    public String getEntite() { return entite; }
    public SyncType getType() { return type; }
    public LocalDateTime getDebut() { return debut; }
    public LocalDateTime getFin() { return fin; }
    public int getElementsTraites() { return elementsTraites; }
    public int getElementsErrones() { return elementsErrones; }
    
    // Setters
    public void setFin(LocalDateTime fin) { this.fin = fin; }
    public void incrementerTraites() { this.elementsTraites++; }
    public void incrementerErrones() { this.elementsErrones++; }
    
    public long getDuree() {
        if (debut == null || fin == null) return 0;
        return java.time.Duration.between(debut, fin).toMillis();
    }
}