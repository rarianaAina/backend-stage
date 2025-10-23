package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRecentDto {
  private Integer id;
  private String reference;
  private String titre;
  private String statut;
  private String priorite;
  private String nomCompany;
  private String nomProduit;
  private String consultantNom;
  private LocalDateTime dateCreation;
}
