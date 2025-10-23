package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DureeTraitementDto {
  private Double dureeMoyenneHeures;
  private Double dureeMoyenneJours;
  private Integer ticketsTraitesRapidement;  // < 24h
  private Integer ticketsTraitesNormalement; // 24h - 72h
  private Integer ticketsTraitesLentement;   // > 72h
}
