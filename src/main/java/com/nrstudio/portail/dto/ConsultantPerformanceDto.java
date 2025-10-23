package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultantPerformanceDto {
  private Integer consultantId;
  private String consultantNom;
  private Integer ticketsEnCours;
  private Integer ticketsClotures;
  private Integer interventionsRealisees;
  private Double tauxResolution;
  private Double dureeMoyenneTraitement;
}
