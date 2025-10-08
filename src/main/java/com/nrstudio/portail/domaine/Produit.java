package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "produit", schema = "dbo")
public class Produit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "nom", length = 255, nullable = false)
  private String nom;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "reference", length = 100)
  private String reference;

  @Column(name = "categorie", length = 100)
  private String categorie;

  @Column(name = "version", length = 50)
  private String version;

  @Column(name = "actif")
  private Boolean actif;

  @Column(name = "date_creation")
  private LocalDateTime dateCreation;

  @Column(name = "date_mise_a_jour")
  private LocalDateTime dateMiseAJour;

  @Column(name = "id_externe_crm", unique = true)
  private Integer idExterneCrm;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getCategorie() {
    return categorie;
  }

  public void setCategorie(String categorie) {
    this.categorie = categorie;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Boolean getActif() {
    return actif;
  }

  public void setActif(Boolean actif) {
    this.actif = actif;
  }

  public LocalDateTime getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(LocalDateTime dateCreation) {
    this.dateCreation = dateCreation;
  }

  public LocalDateTime getDateMiseAJour() {
    return dateMiseAJour;
  }

  public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
    this.dateMiseAJour = dateMiseAJour;
  }

  public Integer getIdExterneCrm() {
    return idExterneCrm;
  }

  public void setIdExterneCrm(Integer idExterneCrm) {
    this.idExterneCrm = idExterneCrm;
  }
}
