package com.nrstudio.portail.dto;
public class ConnexionReponse {
  private String jeton;
  private String email;
  private Integer utilisateurId;
  private String nom;
  public ConnexionReponse() {}
  public ConnexionReponse(String jeton, String email, Integer utilisateurId, String nom) { this.jeton = jeton; this.email = email; this.utilisateurId = utilisateurId; this.nom = nom; }
  public String getJeton() { return jeton; }
  public void setJeton(String jeton) { this.jeton = jeton; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public Integer getUtilisateurId() { return utilisateurId; }
  public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
  public String getNom() { return nom; }
  public void setNom(String nom) { this.nom = nom; }
}
