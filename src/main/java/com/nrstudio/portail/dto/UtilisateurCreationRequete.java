package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

import com.twilio.rest.api.v2010.account.availablephonenumbercountry.Local;

public class UtilisateurCreationRequete {
  private String identifiant;
  private String nom;
  private String prenom;
  private String email;
  private Boolean actif;
  private String motDePasse;
  private Integer companyId;
  private String telephone;
  private Integer actifInt;
  private String idExterneCrm;
  private LocalDateTime dateCreation;
  private LocalDateTime dateMiseAJour;
  public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
  public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
  public LocalDateTime getDateCreation() { return dateCreation; }
  public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
  public Integer getCompanyId() { return companyId; }
  public void setCompanyId(Integer companyId) { this.companyId = companyId; }
  public String getTelephone() { return telephone; }
  public void setTelephone(String telephone) { this.telephone = telephone; }
  public Integer getActifInt() { return actifInt; }
  public void setActif(Integer actifInt) { this.actifInt = actifInt; }
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
  public Boolean getActif() { return actif; }
  public void setActif(Boolean actif) { this.actif = actif; }
  public String getMotDePasse() { return motDePasse; }
  public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
