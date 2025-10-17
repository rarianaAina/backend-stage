package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "piece_jointe", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PieceJointe {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "ticket_id")
  private Integer ticketId;

  @Column(name = "intervention_id")
  private Integer interventionId;

  @Column(name = "nom_fichier", length = 255, nullable = false)
  private String nomFichier;

  @Column(name = "chemin_fichier", length = 500, nullable = false)
  private String cheminFichier;

  @Column(name = "type_fichier", length = 100)
  private String typeFichier;

  @Column(name = "taille_fichier")
  private Long tailleFichier;

  @Column(name = "televerse_par_utilisateur_id", nullable = false)
  private Integer televerseParUtilisateurId;

  @Column(name = "date_telechargement", nullable = false)
  private LocalDateTime dateTelechargement;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getTicketId() {
    return ticketId;
  }

  public void setTicketId(Integer ticketId) {
    this.ticketId = ticketId;
  }

  public Integer getInterventionId() {
    return interventionId;
  }

  public void setInterventionId(Integer interventionId) {
    this.interventionId = interventionId;
  }

  public String getNomFichier() {
    return nomFichier;
  }

  public void setNomFichier(String nomFichier) {
    this.nomFichier = nomFichier;
  }

  public String getCheminFichier() {
    return cheminFichier;
  }

  public void setCheminFichier(String cheminFichier) {
    this.cheminFichier = cheminFichier;
  }

  public String getTypeFichier() {
    return typeFichier;
  }

  public void setTypeFichier(String typeFichier) {
    this.typeFichier = typeFichier;
  }

  public Long getTailleFichier() {
    return tailleFichier;
  }

  public void setTailleFichier(Long tailleFichier) {
    this.tailleFichier = tailleFichier;
  }

  public Integer getTeleverseParUtilisateurId() {
    return televerseParUtilisateurId;
  }

  public void setTeleverseParUtilisateurId(Integer televerseParUtilisateurId) {
    this.televerseParUtilisateurId = televerseParUtilisateurId;
  }

  public LocalDateTime getDateTelechargement() {
    return dateTelechargement;
  }

  public void setDateTelechargement(LocalDateTime dateTelechargement) {
    this.dateTelechargement = dateTelechargement;
  }
}
