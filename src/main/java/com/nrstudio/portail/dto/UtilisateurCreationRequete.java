package com.nrstudio.portail.dto;
public class UtilisateurCreationRequete {
  private String identifiant;
  private String nom;
  private String prenom;
  private String email;
  private Boolean actif;
  private String motDePasse;
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
