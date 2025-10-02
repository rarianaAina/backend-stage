package com.nrstudio.portail.dto;
public class ConnexionReponse {
  private String jeton;
  private String identifiant;
  public ConnexionReponse() {}
  public ConnexionReponse(String jeton, String identifiant) { this.jeton = jeton; this.identifiant = identifiant; }
  public String getJeton() { return jeton; }
  public void setJeton(String jeton) { this.jeton = jeton; }
  public String getIdentifiant() { return identifiant; }
  public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }
}
