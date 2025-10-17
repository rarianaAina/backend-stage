package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "intervention", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Intervention {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "ticket_id", nullable = false)
  private Integer ticketId;

  @Column(length = 50, nullable = false)
  private String reference;

  @Lob
  @Column(name = "raison")
  private String raison;

  @Column(name = "date_intervention", nullable = false)
  private LocalDateTime dateIntervention;

  @Column(name = "date_proposee_client")
  private LocalDateTime dateProposeeClient;

  @Column(name = "type_intervention", length = 50)
  private String typeIntervention;

  @Column(name = "statut_intervention_id", nullable = false)
  private Integer statutInterventionId;

  @Column(name = "cree_par_utilisateur_id", nullable = false)
  private Integer creeParUtilisateurId;

  @Column(name = "date_creation", nullable = false)
  private LocalDateTime dateCreation;

  @Column(name = "date_mise_a_jour", nullable = false)
  private LocalDateTime dateMiseAJour;

  @Column(name = "date_cloture")
  private LocalDateTime dateCloture;

  @Column(name = "cloture_par_utilisateur_id")
  private Integer clotureParUtilisateurId;

  @Lob
  @Column(name = "fiche_intervention")
  private String ficheIntervention;

  @Column(name = "validee_par_client")
  private Boolean valideeParClient;

  @Column(name = "id_externe_crm", unique = true)
  private Integer idExterneCrm;

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

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getRaison() {
    return raison;
  }

  public void setRaison(String raison) {
    this.raison = raison;
  }

  public LocalDateTime getDateIntervention() {
    return dateIntervention;
  }

  public void setDateIntervention(LocalDateTime dateIntervention) {
    this.dateIntervention = dateIntervention;
  }

  public LocalDateTime getDateProposeeClient() {
    return dateProposeeClient;
  }

  public void setDateProposeeClient(LocalDateTime dateProposeeClient) {
    this.dateProposeeClient = dateProposeeClient;
  }

  public String getTypeIntervention() {
    return typeIntervention;
  }

  public void setTypeIntervention(String typeIntervention) {
    this.typeIntervention = typeIntervention;
  }

  public Integer getStatutInterventionId() {
    return statutInterventionId;
  }

  public void setStatutInterventionId(Integer statutInterventionId) {
    this.statutInterventionId = statutInterventionId;
  }

  public Integer getCreeParUtilisateurId() {
    return creeParUtilisateurId;
  }

  public void setCreeParUtilisateurId(Integer creeParUtilisateurId) {
    this.creeParUtilisateurId = creeParUtilisateurId;
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

  public LocalDateTime getDateCloture() {
    return dateCloture;
  }

  public void setDateCloture(LocalDateTime dateCloture) {
    this.dateCloture = dateCloture;
  }

  public Integer getClotureParUtilisateurId() {
    return clotureParUtilisateurId;
  }

  public void setClotureParUtilisateurId(Integer clotureParUtilisateurId) {
    this.clotureParUtilisateurId = clotureParUtilisateurId;
  }

  public String getFicheIntervention() {
    return ficheIntervention;
  }

  public void setFicheIntervention(String ficheIntervention) {
    this.ficheIntervention = ficheIntervention;
  }

  public Boolean getValideeParClient() {
    return valideeParClient;
  }

  public void setValideeParClient(Boolean valideeParClient) {
    this.valideeParClient = valideeParClient;
  }

  public Integer getIdExterneCrm() {
    return idExterneCrm;
  }

  public void setIdExterneCrm(Integer idExterneCrm) {
    this.idExterneCrm = idExterneCrm;
  }
}
