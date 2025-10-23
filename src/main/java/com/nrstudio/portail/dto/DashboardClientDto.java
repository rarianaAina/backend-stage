package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardClientDto {
  private StatistiquesTicketsDto statistiquesTickets;
  private List<CreditHoraireDto> creditsHoraires;
  private List<TicketRecentDto> ticketsRecents;
  private List<InterventionProchaine> interventionsProchaines;
  private Map<String, Integer> ticketsParStatut;
  private Map<String, Integer> ticketsParPriorite;
  private Map<String, Integer> ticketsParProduit;
  private DureeTraitementDto dureesMoyennes;
}
