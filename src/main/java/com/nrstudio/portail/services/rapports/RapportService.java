package com.nrstudio.portail.services.rapports;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.rapports.*;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.services.dashboard.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RapportService {

    private final TicketRepository ticketRepository;
    private final InterventionRepository interventionRepository;
    private final UtilisateurInterneRepository utilisateurInterneRepository;
    private final CompanyRepository companyRepository;
    private final ProduitRepository produitRepository;
    private final InteractionRepository interactionRepository;
    private final DashboardService dashboardService;

    public RapportService(TicketRepository ticketRepository,
                         InterventionRepository interventionRepository,
                         UtilisateurInterneRepository utilisateurInterneRepository,
                         CompanyRepository companyRepository,
                         ProduitRepository produitRepository,
                         InteractionRepository interactionRepository,
                         DashboardService dashboardService) {
        this.ticketRepository = ticketRepository;
        this.interventionRepository = interventionRepository;
        this.utilisateurInterneRepository = utilisateurInterneRepository;
        this.companyRepository = companyRepository;
        this.produitRepository = produitRepository;
        this.interactionRepository = interactionRepository;
        this.dashboardService = dashboardService;
    }

    public RapportResponseDto genererRapport(RapportRequestDto request) {
        RapportResponseDto response = new RapportResponseDto();
        
        // Filtrer les tickets par periode
        List<Ticket> ticketsFiltres = filtrerTicketsParPeriode(request.getDateDebut(), request.getDateFin());
        
        switch (request.getTypeRapport()) {
            case "activite":
                response.setStatistiques(genererStatistiquesActivite(ticketsFiltres, request));
                response.setDonneesGraphique(genererGraphiqueActivite(ticketsFiltres, request));
                break;
            case "performance":
                response.setPerformancesConsultants(genererPerformancesConsultants(ticketsFiltres, request));
                response.setDonneesGraphique(genererGraphiquePerformance(ticketsFiltres, request));
                break;
            case "satisfaction":
                response.setSatisfaction(genererDonneesSatisfaction(ticketsFiltres, request));
                response.setDonneesGraphique(genererGraphiqueSatisfaction(ticketsFiltres, request));
                break;
            case "credits":
                response.setDonneesGraphique(genererGraphiqueCredits(ticketsFiltres, request));
                break;
        }
        
        return response;
    }

    private List<Ticket> filtrerTicketsParPeriode(LocalDate dateDebut, LocalDate dateFin) {
        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(23, 59, 59);
        
        return ticketRepository.findByDateCreationBetween(debut, fin);
    }

    private StatistiquesRapportDto genererStatistiquesActivite(List<Ticket> tickets, RapportRequestDto request) {
        StatistiquesRapportDto stats = new StatistiquesRapportDto();
        
        stats.setTotalDemandes(tickets.size());
        stats.setDemandesCreees(tickets.size());
        stats.setDemandesResolues((int) tickets.stream()
            .filter(t -> t.getStatutTicketId() == 7) // Cloture
            .count());
        
        double tauxResolution = tickets.isEmpty() ? 0 : 
            (stats.getDemandesResolues() * 100.0) / tickets.size();
        stats.setTauxResolution(Math.round(tauxResolution * 100.0) / 100.0);
        
        // Calcul du temps moyen de reponse (premiere interaction)
        double tempsMoyen = calculerTempsMoyenReponse(tickets);
        stats.setTempsMoyenReponse(Math.round(tempsMoyen * 100.0) / 100.0);
        
        stats.setEvolutionMensuelle(calculerEvolutionMensuelle(tickets, request));
        
        return stats;
    }

    private double calculerTempsMoyenReponse(List<Ticket> tickets) {
        return tickets.stream()
            .mapToDouble(ticket -> {
                // Trouver la premiere interaction pour ce ticket
                List<Interaction> interactions = interactionRepository.findByTicketId(ticket.getId());
                if (interactions.isEmpty()) return 0;
                
                Interaction premiereInteraction = interactions.stream()
                    .min(Comparator.comparing(Interaction::getDateCreation))
                    .orElse(null);
                    
                if (premiereInteraction == null) return 0;
                
                return java.time.Duration.between(
                    ticket.getDateCreation(), 
                    premiereInteraction.getDateCreation()
                ).toHours();
            })
            .average()
            .orElse(0);
    }

    private Map<String, Integer> calculerEvolutionMensuelle(List<Ticket> tickets, RapportRequestDto request) {
        Map<String, Integer> evolution = new LinkedHashMap<>();
        
        LocalDate current = request.getDateDebut();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
        
        while (!current.isAfter(request.getDateFin())) {
            YearMonth yearMonth = YearMonth.from(current);
            LocalDateTime debutMois = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime finMois = yearMonth.atEndOfMonth().atTime(23, 59, 59);
            
            long count = tickets.stream()
                .filter(t -> !t.getDateCreation().isBefore(debutMois) && !t.getDateCreation().isAfter(finMois))
                .count();
                
            evolution.put(yearMonth.format(formatter), (int) count);
            current = current.plusMonths(1);
        }
        
        return evolution;
    }

private DonneesGraphiqueDto genererGraphiqueActivite(List<Ticket> tickets, RapportRequestDto request) {
    DonneesGraphiqueDto graphique = new DonneesGraphiqueDto();
    
    Map<String, Integer> evolution = calculerEvolutionMensuelle(tickets, request);
    graphique.setLabels(new ArrayList<>(evolution.keySet()));
    
    // Dataset pour les demandes créées
    DatasetGraphiqueDto datasetCreees = new DatasetGraphiqueDto();
    datasetCreees.setLabel("Demandes créées");
    datasetCreees.setData(new ArrayList<>(evolution.values()));
    datasetCreees.setBackgroundColor("rgba(59, 130, 246, 0.7)");
    datasetCreees.setBorderColor("rgba(59, 130, 246, 1)");
    
    // Dataset pour les demandes résolues - calcul réel
    Map<String, Integer> evolutionResolues = calculerEvolutionMensuelleResolues(tickets, request);
    DatasetGraphiqueDto datasetResolues = new DatasetGraphiqueDto();
    datasetResolues.setLabel("Demandes résolues");
    datasetResolues.setData(new ArrayList<>(evolutionResolues.values()));
    datasetResolues.setBackgroundColor("rgba(16, 185, 129, 0.7)");
    datasetResolues.setBorderColor("rgba(16, 185, 129, 1)");
    
    graphique.setDatasets(Arrays.asList(datasetCreees, datasetResolues));
    return graphique;
}

    private List<ConsultantPerformanceDto> genererPerformancesConsultants(List<Ticket> tickets, RapportRequestDto request) {
        List<UtilisateurInterne> consultants = utilisateurInterneRepository.findAll();
        
        return consultants.stream()
            .map(consultant -> {
                List<Ticket> ticketsConsultant = tickets.stream()
                    .filter(t -> consultant.getId().equals(t.getAffecteAUtilisateurId()))
                    .collect(Collectors.toList());
                
                ConsultantPerformanceDto perf = new ConsultantPerformanceDto();
                perf.setConsultantId(consultant.getId());
                perf.setConsultantNom(consultant.getNom() + " " + consultant.getPrenom());
                perf.setTicketsEnCours((int) ticketsConsultant.stream()
                    .filter(t -> t.getStatutTicketId() != 7).count());
                perf.setTicketsClotures((int) ticketsConsultant.stream()
                    .filter(t -> t.getStatutTicketId() == 7).count());
                
                int total = perf.getTicketsEnCours() + perf.getTicketsClotures();
                double tauxResolution = total > 0 ? (perf.getTicketsClotures() * 100.0) / total : 0;
                perf.setTauxResolution(Math.round(tauxResolution * 100.0) / 100.0);
                
                return perf;
            })
            .filter(p -> p.getTicketsClotures() > 0) // Filtrer les consultants avec activite
            .sorted((p1, p2) -> Integer.compare(p2.getTicketsClotures(), p1.getTicketsClotures()))
            .collect(Collectors.toList());
    }

    private DonneesGraphiqueDto genererGraphiquePerformance(List<Ticket> tickets, RapportRequestDto request) {
        DonneesGraphiqueDto graphique = new DonneesGraphiqueDto();
        
        List<ConsultantPerformanceDto> performances = genererPerformancesConsultants(tickets, request);
        
        graphique.setLabels(performances.stream()
            .map(ConsultantPerformanceDto::getConsultantNom)
            .collect(Collectors.toList()));
        
        DatasetGraphiqueDto dataset = new DatasetGraphiqueDto();
        dataset.setLabel("Tickets resolues");
        dataset.setData(performances.stream()
            .map(ConsultantPerformanceDto::getTicketsClotures)
            .collect(Collectors.toList()));
        dataset.setBackgroundColor("rgba(59, 130, 246, 0.7)");
        dataset.setBorderColor("rgba(59, 130, 246, 1)");
        
        graphique.setDatasets(Collections.singletonList(dataset));
        return graphique;
    }

    private DonneesSatisfactionDto genererDonneesSatisfaction(List<Ticket> tickets, RapportRequestDto request) {
        DonneesSatisfactionDto satisfaction = new DonneesSatisfactionDto();
        
        // Donnees fictives pour l'exemple - A remplacer par vos vraies donnees
        Map<String, Integer> repartition = new LinkedHashMap<>();
        repartition.put("Tres satisfait", 45);
        repartition.put("Satisfait", 35);
        repartition.put("Neutre", 12);
        repartition.put("Insatisfait", 6);
        repartition.put("Tres insatisfait", 2);
        
        satisfaction.setRepartition(repartition);
        satisfaction.setMoyenne(4.3);
        satisfaction.setTotalAvis(100);
        
        return satisfaction;
    }

    private DonneesGraphiqueDto genererGraphiqueSatisfaction(List<Ticket> tickets, RapportRequestDto request) {
        DonneesGraphiqueDto graphique = new DonneesGraphiqueDto();
        
        DonneesSatisfactionDto satisfaction = genererDonneesSatisfaction(tickets, request);
        
        graphique.setLabels(new ArrayList<>(satisfaction.getRepartition().keySet()));
        
        DatasetGraphiqueDto dataset = new DatasetGraphiqueDto();
        dataset.setLabel("Satisfaction client");
        dataset.setData(new ArrayList<>(satisfaction.getRepartition().values()));
        
        // CORRECTION : Utilisation de backgroundColors au lieu de backgroundColor
        dataset.setBackgroundColors(Arrays.asList(
            "rgba(16, 185, 129, 0.8)",
            "rgba(59, 130, 246, 0.8)",
            "rgba(245, 158, 11, 0.8)",
            "rgba(239, 68, 68, 0.8)",
            "rgba(107, 114, 128, 0.8)"
        ));
        
        graphique.setDatasets(Collections.singletonList(dataset));
        return graphique;
    }

    private DonneesGraphiqueDto genererGraphiqueCredits(List<Ticket> tickets, RapportRequestDto request) {
        // Retourner un graphique vide pour l'instant
        DonneesGraphiqueDto graphique = new DonneesGraphiqueDto();
        graphique.setLabels(Arrays.asList("Credits utilises", "Credits restants"));
        
        DatasetGraphiqueDto dataset = new DatasetGraphiqueDto();
        dataset.setLabel("Utilisation des credits");
        dataset.setData(Arrays.asList(65, 35));
        dataset.setBackgroundColor("rgba(139, 92, 246, 0.7)");
        dataset.setBorderColor("rgba(139, 92, 246, 1)");
        
        graphique.setDatasets(Collections.singletonList(dataset));
        return graphique;
    }

    private Map<String, Integer> calculerEvolutionMensuelleResolues(List<Ticket> tickets, RapportRequestDto request) {
    Map<String, Integer> evolution = new LinkedHashMap<>();
    
    LocalDate current = request.getDateDebut();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy", Locale.FRENCH);
    
    while (!current.isAfter(request.getDateFin())) {
        YearMonth yearMonth = YearMonth.from(current);
        LocalDateTime debutMois = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime finMois = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        
        long count = tickets.stream()
            .filter(t -> t.getStatutTicketId() == 7) // Clôturé
            .filter(t -> !t.getDateCreation().isBefore(debutMois) && !t.getDateCreation().isAfter(finMois))
            .count();
            
        evolution.put(yearMonth.format(formatter), (int) count);
        current = current.plusMonths(1);
    }
    
    return evolution;
}
}