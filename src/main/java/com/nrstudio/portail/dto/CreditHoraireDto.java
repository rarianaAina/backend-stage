package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditHoraireDto {
  private Integer id;
  private Integer companyId;
  private Integer produitId;
  private String nomCompany;
  private String nomProduit;
  private LocalDate periodeDebut;
  private LocalDate periodeFin;
  private Integer heuresAllouees;
  private Integer heuresConsommees;
  private Integer heuresRestantes;
  private Double pourcentageUtilisation;
  private boolean actif;
  private boolean expire;

  public CreditHoraireDto(Integer companyId, Integer produitId, LocalDate periodeDebut, LocalDate periodeFin, Integer heuresAllouees) {
    this.companyId = companyId;
    this.produitId = produitId;
    this.periodeDebut = periodeDebut;
    this.periodeFin = periodeFin;
    this.heuresAllouees = heuresAllouees;
    this.heuresConsommees = 0;
    this.heuresRestantes = heuresAllouees;
    this.pourcentageUtilisation = 0.0;
    this.actif = true;
    this.expire = false;
  }

  //Getters et Setters
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
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
  public String getNomCompany() {
    return nomCompany;
  }
  public void setNomCompany(String nomCompany) {
    this.nomCompany = nomCompany;
  }
  public String getNomProduit() {
    return nomProduit;
  }
  public void setNomProduit(String nomProduit) {
    this.nomProduit = nomProduit;
  }
  public LocalDate getPeriodeDebut() {
    return periodeDebut;
  }
  public void setPeriodeDebut(LocalDate periodeDebut) {
    this.periodeDebut = periodeDebut;
  }
  public LocalDate getPeriodeFin() {
    return periodeFin;
  }
  public void setPeriodeFin(LocalDate periodeFin) {
    this.periodeFin = periodeFin;
  }
  public Integer getHeuresAllouees() {
    return heuresAllouees;
  }
  public void setHeuresAllouees(Integer heuresAllouees) {
    this.heuresAllouees = heuresAllouees;
  }
  public Integer getHeuresConsommees() {
    return heuresConsommees;
  }
  public void setHeuresConsommees(Integer heuresConsommees) {
    this.heuresConsommees = heuresConsommees;
  }
  public Integer getHeuresRestantes() {
    return heuresRestantes;
  }
  public void setHeuresRestantes(Integer heuresRestantes) {
    this.heuresRestantes = heuresRestantes;
  }
  public Double getPourcentageUtilisation() {
    return pourcentageUtilisation;
  }
  public void setPourcentageUtilisation(Double pourcentageUtilisation) {
    this.pourcentageUtilisation = pourcentageUtilisation;
  }
  public boolean isActif() {
    return actif;
  }
  public void setActif(boolean actif) {
    this.actif = actif;
  }
  public boolean isExpire() {
    return expire;
  }
  public void setExpire(boolean expire) {
    this.expire = expire;
  }
  
}
