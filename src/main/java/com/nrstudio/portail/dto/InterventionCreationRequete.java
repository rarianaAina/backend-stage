package com.nrstudio.portail.dto;

import java.time.LocalDateTime;

public class InterventionCreationRequete {
  private Integer ticketId;
  private String raison;
  private LocalDateTime dateIntervention;
  private String typeIntervention;
  private Integer consultantId;

  public Integer getTicketId() {
    return ticketId;
  }

  public void setTicketId(Integer ticketId) {
    this.ticketId = ticketId;
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

  public String getTypeIntervention() {
    return typeIntervention;
  }

  public void setTypeIntervention(String typeIntervention) {
    this.typeIntervention = typeIntervention;
  }

  public Integer getConsultantId() {
    return consultantId;
  }

  public void setConsultantId(Integer consultantId) {
    this.consultantId = consultantId;
  }
}
