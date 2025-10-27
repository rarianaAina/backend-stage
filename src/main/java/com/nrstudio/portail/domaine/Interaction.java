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
  @Column(name = "contenu", nullable = false)
  private String message;

  @Column(name = "type_interaction_id")
  private Integer typeInteractionId;

  @Column(name = "canal_interaction_id")
  private Integer canalInteractionId;

  @Column(name = "auteur_utilisateur_id", nullable = false)
  private Integer auteurUtilisateurId;

  @Column(name = "date_creation", nullable = false)
  private LocalDateTime dateCreation;

  // @Column(name = "visible_client")
  // private Boolean visibleClient;

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

  public Integer getTypeInteractionId() {
    return typeInteractionId;
  }

  public void setTypeInteractionId(Integer typeInteractionId) {
    this.typeInteractionId = typeInteractionId;
  }

  public Integer getCanalInteractionId() {
    return canalInteractionId;
  }

  public void setCanalInteractionId(Integer canalInteractionId) {
    this.canalInteractionId = canalInteractionId;
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

  // public Boolean getVisibleClient() {
  //   return visibleClient;
  // }

  // public void setVisibleClient(Boolean visibleClient) {
  //   this.visibleClient = visibleClient;
  // }

  // TO string
  @Override
  public String toString() {
    return "Interaction{" +
        "id=" + id +
        ", ticketId=" + ticketId +
        ", interventionId=" + interventionId +
        ", message='" + message + '\'' +
        ", typeInteractionId=" + typeInteractionId +
        ", auteurUtilisateurId=" + auteurUtilisateurId +
        ", dateCreation=" + dateCreation +
        '}';
  } 
}
