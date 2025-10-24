package com.nrstudio.portail.dto;
public class ConnexionReponse {
  private String jeton;
  private String email;
  private Integer utilisateurId;
  private Integer companyId;
  private String companyName;
  private String nom;
  public ConnexionReponse() {}
  public ConnexionReponse(String jeton, String email, Integer utilisateurId, String nom, Integer companyId, String companyName) { this.jeton = jeton; this.email = email; this.utilisateurId = utilisateurId; this.nom = nom; this.companyId = companyId; this.companyName = companyName;}
  public String getJeton() { return jeton; }
  public void setJeton(String jeton) { this.jeton = jeton; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public Integer getUtilisateurId() { return utilisateurId; }
  public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
  public String getNom() { return nom; }
  public void setNom(String nom) { this.nom = nom; }
  public Integer getCompanyId() { return companyId; }
  public void setCompanyId(Integer companyId) { this.companyId = companyId; }
  public String getCompanyName() { return companyName; }
  public void setCompanyName(String companyName) { this.companyName = companyName; }
}
