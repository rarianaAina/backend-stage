package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesTicketsDto {
  private Integer totalTickets;
  private Integer ticketsOuverts;
  private Integer ticketsEnCours;
  private Integer ticketsClotures;
  private Integer ticketsUrgents;
}
