package com.nrstudio.portail.dto.utilisateur;

import java.time.LocalDateTime;

public class UtilisateurInterneDto {
    private Integer id;
    private Integer companyId;
    private String companyName;
    private String idExterneCrm;
    private String identifiant;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String whatsappNumero;
    private boolean actif;
    private LocalDateTime dateDerniereConnexion;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    
    // Constructeurs
    public UtilisateurInterneDto() {}
    
    public UtilisateurInterneDto(Integer id, String nom, String prenom, String email) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getIdExterneCrm() { return idExterneCrm; }
    public void setIdExterneCrm(String idExterneCrm) { this.idExterneCrm = idExterneCrm; }
    
    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getWhatsappNumero() { return whatsappNumero; }
    public void setWhatsappNumero(String whatsappNumero) { this.whatsappNumero = whatsappNumero; }
    
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
    
    public LocalDateTime getDateDerniereConnexion() { return dateDerniereConnexion; }
    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) { this.dateDerniereConnexion = dateDerniereConnexion; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
}