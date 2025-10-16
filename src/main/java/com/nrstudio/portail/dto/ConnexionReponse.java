package com.nrstudio.portail.dto;
public class ConnexionReponse {
  private String jeton;
  private String email;
  public ConnexionReponse() {}
  public ConnexionReponse(String jeton, String email) { this.jeton = jeton; this.email = email; }
  public String getJeton() { return jeton; }
  public void setJeton(String jeton) { this.jeton = jeton; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
