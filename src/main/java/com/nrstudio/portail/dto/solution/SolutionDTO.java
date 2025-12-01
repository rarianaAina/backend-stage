package com.nrstudio.portail.dto.solution;

import java.time.LocalDateTime;

public class SolutionDTO {
    private Integer id;
    private String titre;
    private String description;
    private String zone;
    private String statut;
    private String etape;
    private String reference;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private LocalDateTime dateCloture;
    private boolean cloture;
    
    // Constructeurs
    public SolutionDTO() {}
    
    public SolutionDTO(Integer id, String titre, String description, String zone, String statut, 
                      String etape, String reference, LocalDateTime dateCreation, 
                      LocalDateTime dateMiseAJour, LocalDateTime dateCloture, boolean cloture) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.zone = zone;
        this.statut = statut;
        this.etape = etape;
        this.reference = reference;
        this.dateCreation = dateCreation;
        this.dateMiseAJour = dateMiseAJour;
        this.dateCloture = dateCloture;
        this.cloture = cloture;
    }
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public String getEtape() { return etape; }
    public void setEtape(String etape) { this.etape = etape; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
    
    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    
    public boolean isCloture() { return cloture; }
    public void setCloture(boolean cloture) { this.cloture = cloture; }
}