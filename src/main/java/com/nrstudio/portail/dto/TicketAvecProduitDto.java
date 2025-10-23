package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

public class TicketAvecProduitDto {
    private String id;
    private String reference;
    private Integer produitId;
    private String produitNom;
    private String description;
    private String prioriteTicketId;
    private LocalDateTime dateCreation;
    private LocalDateTime dateCloture;
    private String etat;
    
    // Constructeurs
    public TicketAvecProduitDto() {}
    
    public TicketAvecProduitDto(String id, String reference, Integer produitId, String produitNom, 
                               String description, String prioriteTicketId, LocalDateTime dateCreation, 
                               LocalDateTime dateCloture, String etat) {
        this.id = id;
        this.reference = reference;
        this.produitId = produitId;
        this.produitNom = produitNom;
        this.description = description;
        this.prioriteTicketId = prioriteTicketId;
        this.dateCreation = dateCreation;
        this.dateCloture = dateCloture;
        this.etat = etat;
    }
    
    // Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }
    
    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getPrioriteTicketId() { return prioriteTicketId; }
    public void setPrioriteTicketId(String prioriteTicketId) { this.prioriteTicketId = prioriteTicketId; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}