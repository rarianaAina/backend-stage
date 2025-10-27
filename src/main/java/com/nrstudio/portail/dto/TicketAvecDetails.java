package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

public class TicketAvecDetails {
    private Integer id;
    private String reference;
    private String titre;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateCloture;
    private Integer produitId;
    private String produitNom;
    private Integer prioriteTicketId;
    private Integer statutTicketId;
    private Integer typeTicketId;
    private Integer companyId;
    private Integer clientId;
    private Boolean politiqueAcceptee;
    private String raison;
    private Integer creeParUtilisateurId;
    private Integer affecteAUtilisateurId;
    private Integer clotureParUtilisateurId;
    private LocalDateTime dateMiseAJour;
    private String idExterneCrm;

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }
    
    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }
    
    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }
    
    public Integer getPrioriteTicketId() { return prioriteTicketId; }
    public void setPrioriteTicketId(Integer prioriteTicketId) { this.prioriteTicketId = prioriteTicketId; }
    
    public Integer getStatutTicketId() { return statutTicketId; }
    public void setStatutTicketId(Integer statutTicketId) { this.statutTicketId = statutTicketId; }
    
    public Integer getTypeTicketId() { return typeTicketId; }
    public void setTypeTicketId(Integer typeTicketId) { this.typeTicketId = typeTicketId; }
    
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }
    
    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }
    
    public Boolean getPolitiqueAcceptee() { return politiqueAcceptee; }
    public void setPolitiqueAcceptee(Boolean politiqueAcceptee) { this.politiqueAcceptee = politiqueAcceptee; }
    
    public String getRaison() { return raison; }
    public void setRaison(String raison) { this.raison = raison; }
    
    public Integer getCreeParUtilisateurId() { return creeParUtilisateurId; }
    public void setCreeParUtilisateurId(Integer creeParUtilisateurId) { this.creeParUtilisateurId = creeParUtilisateurId; }
    
    public Integer getAffecteAUtilisateurId() { return affecteAUtilisateurId; }
    public void setAffecteAUtilisateurId(Integer affecteAUtilisateurId) { this.affecteAUtilisateurId = affecteAUtilisateurId; }
    
    public Integer getClotureParUtilisateurId() { return clotureParUtilisateurId; }
    public void setClotureParUtilisateurId(Integer clotureParUtilisateurId) { this.clotureParUtilisateurId = clotureParUtilisateurId; }
    
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
    
    public String getIdExterneCrm() { return idExterneCrm; }
    public void setIdExterneCrm(String idExterneCrm) { this.idExterneCrm = idExterneCrm; }
}
