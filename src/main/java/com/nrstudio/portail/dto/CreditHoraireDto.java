package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditHoraireDto {
  private Integer id;
  private Integer companyId;
  private Integer produitId;
  private String nomCompany;
  private String nomProduit;
  private LocalDate periodeDebut;
  private LocalDate periodeFin;
  private Integer heuresAllouees;
  private Integer heuresConsommees;
  private Integer heuresRestantes;
  private Double pourcentageUtilisation;
  private boolean actif;
  private boolean expire;
}
