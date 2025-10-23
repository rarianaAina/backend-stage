package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterventionProchaine {
  private Integer id;
  private String reference;
  private String ticketReference;
  private String ticketTitre;
  private LocalDateTime dateIntervention;
  private String typeIntervention;
  private String statut;
  private String consultantNom;
}
