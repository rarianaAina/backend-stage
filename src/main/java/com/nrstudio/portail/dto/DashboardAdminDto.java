package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAdminDto {
  private StatistiquesGlobalesDto statistiquesGlobales;
  private Map<String, Integer> ticketsParStatut;
  private Map<String, Integer> ticketsParPriorite;
  private Map<String, ConsultantPerformanceDto> performancesConsultants;
  private List<TicketRecentDto> ticketsRecents;
  private Map<String, Integer> ticketsParCompany;
  private Map<String, Integer> ticketsParProduit;
  private DureeTraitementDto dureesMoyennes;
}
