package com.nrstudio.portail.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class TicketCreationRequete {
    private Integer companyId;
    private Integer produitId;           // nullable
    private Integer typeTicketId;
    private Integer prioriteTicketId;
    private Integer statutTicketId;
    private String titre;                // 250 max
    private String description;          // long
    private String raison;               // 500 max
    private boolean politiqueAcceptee;
    private Integer creeParUtilisateurId;
    private Integer affecteAUtilisateurId; // nullable
    private Integer clientId;
    private List<MultipartFile> fichiers;
    // Getters et Setters


    public List<MultipartFile> getFichiers() { 
        return fichiers; 
    }
    public void setFichiers(List<MultipartFile> fichiers) { 
        this.fichiers = fichiers; 
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }
    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getProduitId() {
        return produitId;
    }

    public void setProduitId(Integer produitId) {
        this.produitId = produitId;
    }

    public Integer getTypeTicketId() {
        return typeTicketId;
    }

    public void setTypeTicketId(Integer typeTicketId) {
        this.typeTicketId = typeTicketId;
    }

    public Integer getPrioriteTicketId() {
        return prioriteTicketId;
    }

    public void setPrioriteTicketId(Integer prioriteTicketId) {
        this.prioriteTicketId = prioriteTicketId;
    }

    public Integer getStatutTicketId() {
        return statutTicketId;
    }

    public void setStatutTicketId(Integer statutTicketId) {
        this.statutTicketId = statutTicketId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRaison() {
        return raison;
    }

    public void setRaison(String raison) {
        this.raison = raison;
    }

    public boolean isPolitiqueAcceptee() {
        return politiqueAcceptee;
    }

    public void setPolitiqueAcceptee(boolean politiqueAcceptee) {
        this.politiqueAcceptee = politiqueAcceptee;
    }

    public Integer getCreeParUtilisateurId() {
        return creeParUtilisateurId;
    }

    public void setCreeParUtilisateurId(Integer creeParUtilisateurId) {
        this.creeParUtilisateurId = creeParUtilisateurId;
    }

    public Integer getAffecteAUtilisateurId() {
        return affecteAUtilisateurId;
    }

    public void setAffecteAUtilisateurId(Integer affecteAUtilisateurId) {
        this.affecteAUtilisateurId = affecteAUtilisateurId;
    }
}
