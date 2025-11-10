package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "ticket", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ticket {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(length = 50, nullable = false)
  private String reference;

  @Column(name = "company_id", nullable = false)
  private Integer companyId;

  @Column(name = "client_id", nullable = false)
  private Integer clientId;

  @Column(name = "produit_id")
  private Integer produitId;

  @Column(name = "type_ticket_id", nullable = false)
  private Integer typeTicketId;

  @Column(name = "priorite_ticket_id", nullable = false)
  private Integer prioriteTicketId;

  @Column(name = "statut_ticket_id", nullable = false)
  private Integer statutTicketId;

  @Column(name = "titre", length = 250, nullable = false)
  private String titre;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "raison", length = 500)
  private String raison;

  @Column(name = "politique_acceptee", nullable = false)
  private boolean politiqueAcceptee;

  @Column(name = "cree_par_utilisateur_id", nullable = false)
  private Integer creeParUtilisateurId;

  @Column(name = "affecte_a_utilisateur_id")
  private Integer affecteAUtilisateurId;

  @Column(name = "date_creation", nullable = false)
  private LocalDateTime dateCreation;

  @Column(name = "date_mise_a_jour", nullable = false)
  private LocalDateTime dateMiseAJour;

  @Column(name = "date_cloture")
  private LocalDateTime dateCloture;

  @Column(name = "cloture_par_utilisateur_id")
  private Integer clotureParUtilisateurId;

  // Lien CRM
  @Column(name = "id_externe_crm", unique = true)
  private Integer idExterneCrm;

  @Column(name = "reference_id", unique = true)
  private String referenceId;
  // Getters et Setters

  public Integer getClientId() {
    return clientId;
  }

  public void setClientId(Integer clientId) {
    this.clientId = clientId;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
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

  public Integer getTypeTicketId() {
    return typeTicketId;
  }

  public void setTypeTicketId(Integer typeTicketId) {
    this.typeTicketId = typeTicketId;
  }

  public Integer getPrioriteTicketId() {
    return prioriteTicketId;
  }

  public void setPrioriteTicketId(Integer prioriteTicketId) {
    this.prioriteTicketId = prioriteTicketId;
  }

  public Integer getStatutTicketId() {
    return statutTicketId;
  }

  public void setStatutTicketId(Integer statutTicketId) {
    this.statutTicketId = statutTicketId;
  }

  public String getTitre() {
    return titre;
  }

  public void setTitre(String titre) {
    this.titre = titre;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRaison() {
    return raison;
  }

  public void setRaison(String raison) {
    this.raison = raison;
  }

  public boolean isPolitiqueAcceptee() {
    return politiqueAcceptee;
  }

  public void setPolitiqueAcceptee(boolean politiqueAcceptee) {
    this.politiqueAcceptee = politiqueAcceptee;
  }

  public Integer getCreeParUtilisateurId() {
    return creeParUtilisateurId;
  }

  public void setCreeParUtilisateurId(Integer creeParUtilisateurId) {
    this.creeParUtilisateurId = creeParUtilisateurId;
  }

  public Integer getAffecteAUtilisateurId() {
    return affecteAUtilisateurId;
  }

  public void setAffecteAUtilisateurId(Integer affecteAUtilisateurId) {
    this.affecteAUtilisateurId = affecteAUtilisateurId;
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

  public Integer getIdExterneCrm() {
    return idExterneCrm;
  }

  public void setIdExterneCrm(Integer idExterneCrm) {
    this.idExterneCrm = idExterneCrm;
  }

  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  @Override
  public String toString() {
            return "Ticket{" +
               "id=" + id +
               ", produitId='" + produitId + '\'' +
               ", clientId='" + clientId + '\'' +
               '}';
  }
}
