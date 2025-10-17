package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "interaction", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Interaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "ticket_id", nullable = false)
  private Integer ticketId;

  @Column(name = "intervention_id")
  private Integer interventionId;

  @Lob
  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "type_interaction", length = 50)
  private String typeInteraction;

  @Column(name = "auteur_utilisateur_id", nullable = false)
  private Integer auteurUtilisateurId;

  @Column(name = "date_creation", nullable = false)
  private LocalDateTime dateCreation;

  @Column(name = "visible_client")
  private Boolean visibleClient;

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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTypeInteraction() {
    return typeInteraction;
  }

  public void setTypeInteraction(String typeInteraction) {
    this.typeInteraction = typeInteraction;
  }

  public Integer getAuteurUtilisateurId() {
    return auteurUtilisateurId;
  }

  public void setAuteurUtilisateurId(Integer auteurUtilisateurId) {
    this.auteurUtilisateurId = auteurUtilisateurId;
  }

  public LocalDateTime getDateCreation() {
    return dateCreation;
  }

  public void setDateCreation(LocalDateTime dateCreation) {
    this.dateCreation = dateCreation;
  }

  public Boolean getVisibleClient() {
    return visibleClient;
  }

  public void setVisibleClient(Boolean visibleClient) {
    this.visibleClient = visibleClient;
  }
}
