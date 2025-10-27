package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

public class InteractionCreateDTO {
    private Integer ticketId;
    private Integer interventionId;
    private String message;
    private Integer typeInteractionId;
    private Integer canalInteractionId;
    private Integer auteurUtilisateurId;
    private Boolean visibleClient;

    // Constructeurs
    public InteractionCreateDTO() {}

    public InteractionCreateDTO(Integer ticketId, String message, Integer typeInteractionId, 
                               Integer canalInteractionId, Integer auteurUtilisateurId) {
        this.ticketId = ticketId;
        this.message = message;
        this.typeInteractionId = typeInteractionId;
        this.canalInteractionId = canalInteractionId;
        this.auteurUtilisateurId = auteurUtilisateurId;
        this.visibleClient = true;
    }

    // Getters et Setters
    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public Integer getInterventionId() { return interventionId; }
    public void setInterventionId(Integer interventionId) { this.interventionId = interventionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getTypeInteractionId() { return typeInteractionId; }
    public void setTypeInteractionId(Integer typeInteractionId) { this.typeInteractionId = typeInteractionId; }

    public Integer getCanalInteractionId() { return canalInteractionId; }
    public void setCanalInteractionId(Integer canalInteractionId) { this.canalInteractionId = canalInteractionId; }

    public Integer getAuteurUtilisateurId() { return auteurUtilisateurId; }
    public void setAuteurUtilisateurId(Integer auteurUtilisateurId) { this.auteurUtilisateurId = auteurUtilisateurId; }

    public Boolean getVisibleClient() { return visibleClient; }
    public void setVisibleClient(Boolean visibleClient) { this.visibleClient = visibleClient; }
}