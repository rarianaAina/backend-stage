package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

public class InteractionDTO {
    private Integer id;
    private Integer ticketId;
    private Integer interventionId;
    private String message;
    
    // Type d'interaction
    private Integer typeInteractionId;
    private String typeInteractionLibelle;
    private String typeInteractionCode;
    
    // Canal d'interaction
    private Integer canalInteractionId;
    private String canalInteractionLibelle;
    private String canalInteractionCode;
    
    // Auteur
    private Integer auteurUtilisateurId;
    private String auteurNom;
    private String auteurPrenom;
    private String auteurEmail;
    
    private LocalDateTime dateCreation;
    private Boolean visibleClient;

    // Constructeurs
    public InteractionDTO() {}

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public Integer getInterventionId() { return interventionId; }
    public void setInterventionId(Integer interventionId) { this.interventionId = interventionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getTypeInteractionId() { return typeInteractionId; }
    public void setTypeInteractionId(Integer typeInteractionId) { this.typeInteractionId = typeInteractionId; }

    public String getTypeInteractionLibelle() { return typeInteractionLibelle; }
    public void setTypeInteractionLibelle(String typeInteractionLibelle) { this.typeInteractionLibelle = typeInteractionLibelle; }

    public String getTypeInteractionCode() { return typeInteractionCode; }
    public void setTypeInteractionCode(String typeInteractionCode) { this.typeInteractionCode = typeInteractionCode; }

    public Integer getCanalInteractionId() { return canalInteractionId; }
    public void setCanalInteractionId(Integer canalInteractionId) { this.canalInteractionId = canalInteractionId; }

    public String getCanalInteractionLibelle() { return canalInteractionLibelle; }
    public void setCanalInteractionLibelle(String canalInteractionLibelle) { this.canalInteractionLibelle = canalInteractionLibelle; }

    public String getCanalInteractionCode() { return canalInteractionCode; }
    public void setCanalInteractionCode(String canalInteractionCode) { this.canalInteractionCode = canalInteractionCode; }

    public Integer getAuteurUtilisateurId() { return auteurUtilisateurId; }
    public void setAuteurUtilisateurId(Integer auteurUtilisateurId) { this.auteurUtilisateurId = auteurUtilisateurId; }

    public String getAuteurNom() { return auteurNom; }
    public void setAuteurNom(String auteurNom) { this.auteurNom = auteurNom; }

    public String getAuteurPrenom() { return auteurPrenom; }
    public void setAuteurPrenom(String auteurPrenom) { this.auteurPrenom = auteurPrenom; }

    public String getAuteurEmail() { return auteurEmail; }
    public void setAuteurEmail(String auteurEmail) { this.auteurEmail = auteurEmail; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Boolean getVisibleClient() { return visibleClient; }
    public void setVisibleClient(Boolean visibleClient) { this.visibleClient = visibleClient; }
}