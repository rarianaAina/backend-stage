package com.nrstudio.portail.services.dashboard;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.*;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardStatisticsService {

    private final TicketRepository ticketRepository;
    private final CompanyRepository companyRepository;
    private final ProduitRepository produitRepository;
    private final InterventionRepository interventionRepository;
    private final UtilisateurInterneRepository utilisateurInterneRepository;

    public StatistiquesTicketsDto calculerStatistiquesTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            return new StatistiquesTicketsDto(0, 0, 0, 0, 0);
        }

        return new StatistiquesTicketsDto(
            tickets.size(),
            countTicketsByStatut(tickets, 1),
            countTicketsByStatut(tickets, 2, 3),
            countTicketsByStatut(tickets, 7),
            countTicketsByPriorite(tickets, 4)
        );
    }

    public StatistiquesGlobalesDto calculerStatistiquesGlobales() {
        List<Ticket> allTickets = ticketRepository.findAll();
        
        return new StatistiquesGlobalesDto(
            allTickets.size(),
            countTicketsByStatut(allTickets, 1),
            countTicketsByStatut(allTickets, 2, 3),
            countTicketsByStatut(allTickets, 7),
            (int) companyRepository.count(),
            (int) utilisateurInterneRepository.count(),
            countInterventionsPlanifiees()
        );
    }

    public Map<String, Integer> repartitionParStatut(List<Ticket> tickets) {
        Map<String, Integer> repartition = new LinkedHashMap<>();
        repartition.put("Ouvert", countTicketsByStatut(tickets, 1));
        repartition.put("En cours", countTicketsByStatut(tickets, 2));
        repartition.put("En attente", countTicketsByStatut(tickets, 3));
        repartition.put("Planifié", countTicketsByStatut(tickets, 5));
        repartition.put("Résolu", countTicketsByStatut(tickets, 6));
        repartition.put("Cloturé", countTicketsByStatut(tickets, 7));
        return repartition;
    }

    public Map<String, Integer> repartitionParPriorite(List<Ticket> tickets) {
        Map<String, Integer> repartition = new LinkedHashMap<>();
        repartition.put("Basse", countTicketsByPriorite(tickets, 1));
        repartition.put("Normale", countTicketsByPriorite(tickets, 2));
        repartition.put("Haute", countTicketsByPriorite(tickets, 3));
        repartition.put("Urgente", countTicketsByPriorite(tickets, 4));
        return repartition;
    }

    public Map<String, Integer> repartitionParProduit(List<Ticket> tickets) {
        // Précharger tous les produits pour éviter les requêtes N+1
        Map<Integer, Produit> produitsMap = produitRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Produit::getId, p -> p));

        return tickets.stream()
            .collect(Collectors.groupingBy(
                t -> getProduitLibelle(t.getProduitId(), produitsMap),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    public Map<String, Integer> repartitionParCompany(List<Ticket> tickets) {
        // Précharger toutes les companies pour éviter les requêtes N+1
        Map<Integer, Company> companiesMap = companyRepository.findAll()
            .stream()
            .collect(Collectors.toMap(Company::getId, c -> c));

        return tickets.stream()
            .collect(Collectors.groupingBy(
                t -> getCompanyNom(t.getCompanyId(), companiesMap),
                Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
            ));
    }

    public DureeTraitementDto calculerDureesTraitement(List<Ticket> tickets) {
        List<Ticket> ticketsClotures = tickets.stream()
            .filter(t -> t.getDateCloture() != null)
            .collect(Collectors.toList());

        if (ticketsClotures.isEmpty()) {
            return new DureeTraitementDto(0.0, 0.0, 0, 0, 0);
        }

        List<Long> durees = ticketsClotures.stream()
            .map(t -> Duration.between(t.getDateCreation(), t.getDateCloture()).toHours())
            .collect(Collectors.toList());

        double moyenneHeures = durees.stream().mapToLong(Long::longValue).average().orElse(0);
        double moyenneJours = moyenneHeures / 24.0;

        int rapides = (int) durees.stream().filter(d -> d < 24).count();
        int normaux = (int) durees.stream().filter(d -> d >= 24 && d <= 72).count();
        int lents = (int) durees.stream().filter(d -> d > 72).count();

        return new DureeTraitementDto(
            Math.round(moyenneHeures * 100.0) / 100.0,
            Math.round(moyenneJours * 100.0) / 100.0,
            rapides, normaux, lents
        );
    }

    public Map<String, ConsultantPerformanceDto> calculerPerformancesConsultants() {
        List<UtilisateurInterne> consultants = utilisateurInterneRepository.findAll();
        Map<String, ConsultantPerformanceDto> performances = new HashMap<>();

        // Précharger les données pour optimiser les performances
        List<Ticket> allTickets = ticketRepository.findAll();
        List<Intervention> allInterventions = interventionRepository.findAll();

        Map<Integer, List<Ticket>> ticketsParConsultant = allTickets.stream()
            .filter(t -> t.getAffecteAUtilisateurId() != null)
            .collect(Collectors.groupingBy(Ticket::getAffecteAUtilisateurId));

        for (UtilisateurInterne consultant : consultants) {
            List<Ticket> ticketsConsultant = ticketsParConsultant.getOrDefault(consultant.getId(), Collections.emptyList());
            
            ConsultantPerformanceDto perf = new ConsultantPerformanceDto();
            perf.setConsultantId(consultant.getId());
            perf.setConsultantNom(consultant.getNom() + " " + consultant.getPrenom());
            perf.setTicketsEnCours(countTicketsEnCours(ticketsConsultant));
            perf.setTicketsClotures(countTicketsByStatut(ticketsConsultant, 7));
            perf.setInterventionsRealisees(countInterventionsConsultant(consultant.getId(), allInterventions, allTickets));
            
            double tauxResolution = calculerTauxResolution(perf.getTicketsEnCours(), perf.getTicketsClotures());
            perf.setTauxResolution(tauxResolution);
            perf.setDureeMoyenneTraitement(calculerDureeMoyenneTraitement(ticketsConsultant));

            performances.put(consultant.getNom(), perf);
        }

        return performances;
    }

    public ChartDataDto buildChartData(List<Ticket> tickets) {
        ChartDataDto chartData = new ChartDataDto();
        
        List<String> labels = Arrays.asList("Ouvert", "En cours", "En attente", "Planifié", "Résolu", "Cloturé");
        chartData.setLabels(labels);
        
        List<Integer> data = Arrays.asList(
            countTicketsByStatut(tickets, 1),
            countTicketsByStatut(tickets, 2),
            countTicketsByStatut(tickets, 3),
            countTicketsByStatut(tickets, 5),
            countTicketsByStatut(tickets, 6),
            countTicketsByStatut(tickets, 7)
        );
        
        ChartDatasetDto dataset = new ChartDatasetDto();
        dataset.setLabel("Tickets");
        dataset.setData(data);
        dataset.setBackgroundColor("rgba(54, 162, 235, 0.5)");
        dataset.setBorderColor("rgba(54, 162, 235, 1)");
        
        chartData.setDatasets(Collections.singletonList(dataset));
        
        return chartData;
    }

    // Méthodes utilitaires privées
    private int countTicketsByStatut(List<Ticket> tickets, Integer... statutIds) {
        return (int) tickets.stream()
            .filter(t -> Arrays.asList(statutIds).contains(t.getStatutTicketId()))
            .count();
    }

    private int countTicketsByPriorite(List<Ticket> tickets, Integer prioriteId) {
        return (int) tickets.stream()
            .filter(t -> prioriteId.equals(t.getPrioriteTicketId()))
            .count();
    }

    private int countInterventionsPlanifiees() {
        return (int) interventionRepository.findAll().stream()
            .filter(i -> i.getStatutInterventionId() == 2)
            .count();
    }

    private String getProduitLibelle(Integer produitId, Map<Integer, Produit> produitsMap) {
        if (produitId == null) return "Non spécifié";
        Produit produit = produitsMap.get(produitId);
        return produit != null ? produit.getCodeProduit() : "Produit non trouvé";
    }

    private String getCompanyNom(Integer companyId, Map<Integer, Company> companiesMap) {
        Company company = companiesMap.get(companyId);
        return company != null ? company.getNom() : "Non spécifié";
    }

    private int countTicketsEnCours(List<Ticket> tickets) {
        return (int) tickets.stream()
            .filter(t -> t.getStatutTicketId() != 7)
            .count();
    }

    private int countInterventionsConsultant(Integer consultantId, List<Intervention> interventions, List<Ticket> tickets) {
        Map<Integer, Ticket> ticketsMap = tickets.stream()
            .collect(Collectors.toMap(Ticket::getId, t -> t));

        return (int) interventions.stream()
            .filter(i -> {
                Ticket t = ticketsMap.get(i.getTicketId());
                return t != null && consultantId.equals(t.getAffecteAUtilisateurId());
            })
            .count();
    }

    private double calculerTauxResolution(int ticketsEnCours, int ticketsClotures) {
        int total = ticketsEnCours + ticketsClotures;
        return total > 0 ? Math.round((ticketsClotures * 100.0) / total * 100.0) / 100.0 : 0;
    }

    private double calculerDureeMoyenneTraitement(List<Ticket> ticketsConsultant) {
        List<Ticket> ticketsClotures = ticketsConsultant.stream()
            .filter(t -> t.getDateCloture() != null)
            .collect(Collectors.toList());

        System.out.println("Tickets clôturés: " + ticketsClotures.size());
        if (ticketsClotures.isEmpty()) return 0;

        double dureeMoyenne = ticketsClotures.stream()
            .mapToLong(t -> Duration.between(t.getDateCreation(), t.getDateCloture()).toHours())
            .average()
            .orElse(0);

            System.out.println("Durée moyenne: " + dureeMoyenne);
        return Math.round(dureeMoyenne * 100.0) / 100.0;
    }
}