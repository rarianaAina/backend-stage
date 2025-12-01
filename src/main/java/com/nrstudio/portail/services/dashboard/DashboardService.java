package com.nrstudio.portail.services.dashboard;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.*;
import com.nrstudio.portail.services.credithoraire.CreditHoraireService;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final CompanyRepository companyRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DashboardStatisticsService statisticsService;
    private final DashboardDataService dataService;
    private final CreditHoraireService creditHoraireService;

    public DashboardClientDto getDashboardClient(Integer userId) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String idExterneCrm = utilisateur.getIdExterneCrm();
        if (idExterneCrm == null) {
            throw new RuntimeException("Aucun idExterneCrm trouvé pour cet utilisateur");
        }

        Integer companyId = dataService.getCompanyIdForUser(idExterneCrm);
        if (companyId == null) {
            throw new RuntimeException("Company non trouvée pour cet utilisateur (idExterneCrm: " + idExterneCrm + ")");
        }

        List<Ticket> tickets = ticketRepository.findByCompanyId(companyId);
        
        DashboardClientDto dashboard = new DashboardClientDto();
        dashboard.setStatistiquesTickets(statisticsService.calculerStatistiquesTickets(tickets));
        dashboard.setCreditsHoraires(creditHoraireService.getCreditsActifs(companyId));
        dashboard.setTicketsRecents(dataService.getTicketsRecents(tickets, 10));
        dashboard.setInterventionsProchaines(dataService.getInterventionsProchaines(companyId, 10));
        dashboard.setTicketsParStatut(statisticsService.repartitionParStatut(tickets));
        dashboard.setTicketsParPriorite(statisticsService.repartitionParPriorite(tickets));
        dashboard.setTicketsParProduit(statisticsService.repartitionParProduit(tickets));
        dashboard.setDureesMoyennes(statisticsService.calculerDureesTraitement(tickets));

        return dashboard;
    }

    public DashboardAdminDto getDashboardAdmin() {
        List<Ticket> allTickets = ticketRepository.findAll();
        
        DashboardAdminDto dashboard = new DashboardAdminDto();
        dashboard.setStatistiquesGlobales(statisticsService.calculerStatistiquesGlobales());
        dashboard.setTicketsParStatut(statisticsService.repartitionParStatut(allTickets));
        dashboard.setTicketsParPriorite(statisticsService.repartitionParPriorite(allTickets));
        dashboard.setPerformancesConsultants(statisticsService.calculerPerformancesConsultants());
        dashboard.setTicketsRecents(dataService.getTicketsRecents(allTickets, 20));
        dashboard.setTicketsParCompany(statisticsService.repartitionParCompany(allTickets));
        dashboard.setTicketsParProduit(statisticsService.repartitionParProduit(allTickets));
        dashboard.setDureesMoyennes(statisticsService.calculerDureesTraitement(allTickets));

        return dashboard;
    }

    public ChartDataDto getChartDataClient(Integer userId) {
        Integer companyId = dataService.getCompanyIdForUser(userId.toString());
        List<Ticket> tickets = ticketRepository.findByCompanyId(companyId);
        return statisticsService.buildChartData(tickets);
    }

    public ChartDataDto getChartDataAdmin() {
        List<Ticket> tickets = ticketRepository.findAll();
        return statisticsService.buildChartData(tickets);
    }
}