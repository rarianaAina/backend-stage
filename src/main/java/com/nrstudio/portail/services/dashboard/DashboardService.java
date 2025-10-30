package com.nrstudio.portail.services.dashboard;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.depots.utiisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.*;
import com.nrstudio.portail.services.CreditHoraireService;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;


import lombok.ToString;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

  private final TicketRepository ticketRepository;
  private final InterventionRepository interventionRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final CompanyRepository companyRepository;
  private final ProduitRepository produitRepository;
  private final CreditHoraireService creditHoraireService;
  private final UtilisateurInterneRepository utilisateurInterneRepository;

  public DashboardService(TicketRepository ticketRepository,
                         InterventionRepository interventionRepository,
                         UtilisateurRepository utilisateurRepository,
                         CompanyRepository companyRepository,
                         ProduitRepository produitRepository,
                         CreditHoraireService creditHoraireService,
                         UtilisateurInterneRepository utilisateurInterneRepository) {
    this.ticketRepository = ticketRepository;
    this.interventionRepository = interventionRepository;
    this.utilisateurRepository = utilisateurRepository;
    this.companyRepository = companyRepository;
    this.produitRepository = produitRepository;
    this.creditHoraireService = creditHoraireService;
    this.utilisateurInterneRepository = utilisateurInterneRepository;
  }

    public DashboardClientDto getDashboardClient(Integer userId) {
      Utilisateur utilisateur = utilisateurRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

      // Récupérer l'idExterneCrm depuis l'utilisateur
      String idExterneCrm = utilisateur.getIdExterneCrm();
      
      if (idExterneCrm == null) {
          throw new RuntimeException("Aucun idExterneCrm trouvé pour cet utilisateur");
      }

      // Utiliser idExterneCrm pour trouver la company
      Integer companyId = getCompanyIdForUser(idExterneCrm);
      if (companyId == null) {
        throw new RuntimeException("Company non trouvée pour cet utilisateur (idExterneCrm: " + idExterneCrm + ")");
      }

      DashboardClientDto dashboard = new DashboardClientDto();
      
      List<Ticket> tickets = ticketRepository.findByCompanyId(companyId);
      
      dashboard.setStatistiquesTickets(calculerStatistiquesTickets(tickets));
      dashboard.setCreditsHoraires(creditHoraireService.getCreditsActifs(companyId));
      dashboard.setTicketsRecents(getTicketsRecents(tickets, 10));
      dashboard.setInterventionsProchaines(getInterventionsProchaines(companyId, 10));
      dashboard.setTicketsParStatut(repartitionParStatut(tickets));
      dashboard.setTicketsParPriorite(repartitionParPriorite(tickets));
      dashboard.setTicketsParProduit(repartitionParProduit(tickets));
      dashboard.setDureesMoyennes(calculerDureesTraitement(tickets));

      return dashboard;
  }
  public DashboardAdminDto getDashboardAdmin() {
    DashboardAdminDto dashboard = new DashboardAdminDto();
    
    List<Ticket> allTickets = ticketRepository.findAll();
    
    dashboard.setStatistiquesGlobales(calculerStatistiquesGlobales());
    dashboard.setTicketsParStatut(repartitionParStatut(allTickets));
    dashboard.setTicketsParPriorite(repartitionParPriorite(allTickets));
    dashboard.setPerformancesConsultants(calculerPerformancesConsultants());
    dashboard.setTicketsRecents(getTicketsRecents(allTickets, 20));
    dashboard.setTicketsParCompany(repartitionParCompany(allTickets));
    dashboard.setTicketsParProduit(repartitionParProduit(allTickets));
    dashboard.setDureesMoyennes(calculerDureesTraitement(allTickets));

    return dashboard;
  }

  public ChartDataDto getChartDataClient(Integer userId) {
    Integer companyId = getCompanyIdForUser(userId.toString());
    List<Ticket> tickets = ticketRepository.findByCompanyId(companyId);
    
    return buildChartData(tickets);
  }

  public ChartDataDto getChartDataAdmin() {
    List<Ticket> tickets = ticketRepository.findAll();
    return buildChartData(tickets);
  }

  private StatistiquesTicketsDto calculerStatistiquesTickets(List<Ticket> tickets) {
    StatistiquesTicketsDto stats = new StatistiquesTicketsDto();
    stats.setTotalTickets(tickets.size());
    stats.setTicketsOuverts((int) tickets.stream()
      .filter(t -> t.getStatutTicketId() == 1).count());
    stats.setTicketsEnCours((int) tickets.stream()
      .filter(t -> t.getStatutTicketId() == 2 || t.getStatutTicketId() == 3).count());
    stats.setTicketsClotures((int) tickets.stream()
      .filter(t -> t.getStatutTicketId() == 7).count());
    stats.setTicketsUrgents((int) tickets.stream()
      .filter(t -> t.getPrioriteTicketId() == 4).count());
    return stats;
  }

  private StatistiquesGlobalesDto calculerStatistiquesGlobales() {
    StatistiquesGlobalesDto stats = new StatistiquesGlobalesDto();
    List<Ticket> allTickets = ticketRepository.findAll();
    
    stats.setTotalTickets(allTickets.size());
    stats.setTicketsOuverts((int) allTickets.stream()
      .filter(t -> t.getStatutTicketId() == 1).count());
    stats.setTicketsEnCours((int) allTickets.stream()
      .filter(t -> t.getStatutTicketId() == 2 || t.getStatutTicketId() == 3).count());
    stats.setTicketsClotures((int) allTickets.stream()
      .filter(t -> t.getStatutTicketId() == 7).count());
    stats.setTotalCompanies((int) companyRepository.count());
    stats.setTotalConsultants((int) utilisateurInterneRepository.count());
    stats.setInterventionsPlanifiees((int) interventionRepository.findAll().stream()
      .filter(i -> i.getStatutInterventionId() == 2).count());
    
    return stats;
  }

  private Map<String, Integer> repartitionParStatut(List<Ticket> tickets) {
    Map<String, Integer> repartition = new LinkedHashMap<>();
    repartition.put("Ouvert", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 1).count());
    repartition.put("En cours", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 2).count());
    repartition.put("En attente", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 3).count());
    repartition.put("Planifié", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 5).count());
    repartition.put("Résolu", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 6).count());
    repartition.put("Cloturé", (int) tickets.stream().filter(t -> t.getStatutTicketId() == 7).count());
    return repartition;
  }

  private Map<String, Integer> repartitionParPriorite(List<Ticket> tickets) {
    Map<String, Integer> repartition = new LinkedHashMap<>();
    repartition.put("Basse", (int) tickets.stream().filter(t -> t.getPrioriteTicketId() == 1).count());
    repartition.put("Normale", (int) tickets.stream().filter(t -> t.getPrioriteTicketId() == 2).count());
    repartition.put("Haute", (int) tickets.stream().filter(t -> t.getPrioriteTicketId() == 3).count());
    repartition.put("Urgente", (int) tickets.stream().filter(t -> t.getPrioriteTicketId() == 4).count());
    return repartition;
  }


  private Map<String, Integer> repartitionParProduit(List<Ticket> tickets) {
    System.out.println("=== DEBUG repartitionParProduit ===");
    System.out.println("Nombre de tickets: " + tickets.size());
    
    for (Ticket ticket : tickets) {
        System.out.println("Ticket ID: " + ticket.getId() + ", Produit ID: " + ticket.getProduitId());
        if (ticket.getProduitId() != null) {
            Produit p = produitRepository.findById(ticket.getProduitId()).orElse(null);
            System.out.println("Produit trouvé: " + (p != null ? p.getLibelle() : "NULL"));
        }
    }
    
    // Puis la version sécurisée
    return tickets.stream()
        .collect(Collectors.groupingBy(
            t -> {
                String libelle = "Non spécifié";
                if (t.getProduitId() != null) {
                    Produit p = produitRepository.findById(t.getProduitId()).orElse(null);
                    libelle = p != null ? p.getCodeProduit() : "Produit non trouvé";
                }
                // S'assurer que le libellé n'est jamais null
                return libelle != null ? libelle : "Libellé null";
            },
            Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
        ));
}

  private Map<String, Integer> repartitionParCompany(List<Ticket> tickets) {
    return tickets.stream()
      .collect(Collectors.groupingBy(
        t -> {
          Company c = companyRepository.findById(t.getCompanyId()).orElse(null);
          return c != null ? c.getNom() : "Non spécifié";
        },
        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
      ));
  }

  private List<TicketRecentDto> getTicketsRecents(List<Ticket> tickets, int limit) {
    return tickets.stream()
      .sorted((t1, t2) -> t2.getDateCreation().compareTo(t1.getDateCreation()))
      .limit(limit)
      .map(this::toTicketRecentDto)
      .collect(Collectors.toList());
  }

  private TicketRecentDto toTicketRecentDto(Ticket ticket) {
    TicketRecentDto dto = new TicketRecentDto();
    dto.setId(ticket.getId());
    dto.setReference(ticket.getReference());
    dto.setTitre(ticket.getTitre());
    dto.setStatut(getStatutLabel(ticket.getStatutTicketId()));
    dto.setPriorite(getPrioriteLabel(ticket.getPrioriteTicketId()));
    
    Company company = companyRepository.findById(ticket.getCompanyId()).orElse(null);
    dto.setNomCompany(company != null ? company.getNom() : "");
    
    if (ticket.getProduitId() != null) {
      Produit produit = produitRepository.findById(ticket.getProduitId()).orElse(null);
      dto.setNomProduit(produit != null ? produit.getLibelle() : "");
    }
    
    if (ticket.getAffecteAUtilisateurId() != null) {
      Utilisateur consultant = utilisateurRepository.findById(ticket.getAffecteAUtilisateurId()).orElse(null);
      dto.setConsultantNom(consultant != null ? consultant.getNom() + " " + consultant.getPrenom() : "");
    }
    
    dto.setDateCreation(ticket.getDateCreation());
    return dto;
  }

  private List<InterventionProchaine> getInterventionsProchaines(Integer companyId, int limit) {
    List<Intervention> interventions = interventionRepository.findAll().stream()
      .filter(i -> {
        Ticket t = ticketRepository.findById(i.getTicketId()).orElse(null);
        return t != null && t.getCompanyId().equals(companyId);
      })
      .filter(i -> i.getDateIntervention().isAfter(LocalDateTime.now()))
      .sorted((i1, i2) -> i1.getDateIntervention().compareTo(i2.getDateIntervention()))
      .limit(limit)
      .collect(Collectors.toList());

    return interventions.stream()
      .map(this::toInterventionProchaine)
      .collect(Collectors.toList());
  }

  private InterventionProchaine toInterventionProchaine(Intervention intervention) {
    InterventionProchaine dto = new InterventionProchaine();
    dto.setId(intervention.getId());
    dto.setReference(intervention.getReference());
    
    Ticket ticket = ticketRepository.findById(intervention.getTicketId()).orElse(null);
    if (ticket != null) {
      dto.setTicketReference(ticket.getReference());
      dto.setTicketTitre(ticket.getTitre());
    }
    
    dto.setDateIntervention(intervention.getDateIntervention());
    dto.setTypeIntervention(intervention.getTypeIntervention());
    dto.setStatut(getStatutInterventionLabel(intervention.getStatutInterventionId()));
    
    if (intervention.getCreeParUtilisateurId() != null) {
      Utilisateur consultant = utilisateurRepository.findById(intervention.getCreeParUtilisateurId()).orElse(null);
      dto.setConsultantNom(consultant != null ? consultant.getNom() + " " + consultant.getPrenom() : "");
    }
    
    return dto;
  }

  private DureeTraitementDto calculerDureesTraitement(List<Ticket> tickets) {
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

  private Map<String, ConsultantPerformanceDto> calculerPerformancesConsultants() {
    List<UtilisateurInterne> consultants = utilisateurInterneRepository.findAll();
    Map<String, ConsultantPerformanceDto> performances = new HashMap<>();

    for (UtilisateurInterne consultant : consultants) {
      List<Ticket> ticketsConsultant = ticketRepository
        .findByAffecteAUtilisateurId(consultant.getId());

      ConsultantPerformanceDto perf = new ConsultantPerformanceDto();
      perf.setConsultantId(consultant.getId());
      perf.setConsultantNom(consultant.getNom() + " " + consultant.getPrenom());
      perf.setTicketsEnCours((int) ticketsConsultant.stream()
        .filter(t -> t.getStatutTicketId() != 7).count());
      perf.setTicketsClotures((int) ticketsConsultant.stream()
        .filter(t -> t.getStatutTicketId() == 7).count());
      
      List<Intervention> interventions = interventionRepository.findAll().stream()
        .filter(i -> {
          Ticket t = ticketRepository.findById(i.getTicketId()).orElse(null);
          return t != null && consultant.getId().equals(t.getAffecteAUtilisateurId());
        })
        .collect(Collectors.toList());
      
      perf.setInterventionsRealisees(interventions.size());
      
      int total = perf.getTicketsEnCours() + perf.getTicketsClotures();
      double tauxResolution = total > 0 ? (perf.getTicketsClotures() * 100.0) / total : 0;
      perf.setTauxResolution(Math.round(tauxResolution * 100.0) / 100.0);
      
      List<Ticket> ticketsClotures = ticketsConsultant.stream()
        .filter(t -> t.getDateCloture() != null)
        .collect(Collectors.toList());
      
      double dureeMoyenne = ticketsClotures.stream()
        .mapToLong(t -> Duration.between(t.getDateCreation(), t.getDateCloture()).toHours())
        .average()
        .orElse(0);
      perf.setDureeMoyenneTraitement(Math.round(dureeMoyenne * 100.0) / 100.0);

      performances.put(consultant.getNom(), perf);
    }

    return performances;
  }

  private ChartDataDto buildChartData(List<Ticket> tickets) {
    ChartDataDto chartData = new ChartDataDto();
    
    List<String> labels = Arrays.asList("Ouvert", "En cours", "En attente", "Planifié", "Résolu", "Cloturé");
    chartData.setLabels(labels);
    
    List<Integer> data = Arrays.asList(
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 1).count(),
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 2).count(),
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 3).count(),
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 5).count(),
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 6).count(),
      (int) tickets.stream().filter(t -> t.getStatutTicketId() == 7).count()
    );
    
    ChartDatasetDto dataset = new ChartDatasetDto();
    dataset.setLabel("Tickets");
    dataset.setData(data);
    dataset.setBackgroundColor("rgba(54, 162, 235, 0.5)");
    dataset.setBorderColor("rgba(54, 162, 235, 1)");
    
    chartData.setDatasets(Collections.singletonList(dataset));
    
    return chartData;
  }

  // private Integer getCompanyIdForUser(Integer userId) {
  //   Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
  //   if (utilisateur == null) return null;
    
  //   // TODO: Implémenter la logique pour récupérer le companyId à partir de utilisateur_role
  //   // Pour l'instant, retourner null ou une valeur par défaut
  //   return null;
  // }
  private Integer getCompanyIdForUser(String idExterneCrm) {
    Utilisateur utilisateur = utilisateurRepository.findByIdExterneCrm(idExterneCrm).orElse(null);
    if (utilisateur == null) return null;
    return utilisateur.getCompanyId();
  }

  private String getStatutLabel(Integer statutId) {
    if (statutId == null) return "";
    switch (statutId) {
      case 1: return "Ouvert";
      case 2: return "En cours";
      case 3: return "En attente";
      case 4: return "En attente client";
      case 5: return "Planifié";
      case 6: return "Résolu";
      case 7: return "Cloturé";
      default: return "Inconnu";
    }
  }

  private String getPrioriteLabel(Integer prioriteId) {
    if (prioriteId == null) return "";
    switch (prioriteId) {
      case 1: return "Basse";
      case 2: return "Normale";
      case 3: return "Haute";
      case 4: return "Urgente";
      default: return "Inconnu";
    }
  }

  private String getStatutInterventionLabel(Integer statutId) {
    if (statutId == null) return "";
    switch (statutId) {
      case 1: return "Proposée";
      case 2: return "Planifiée";
      case 3: return "En cours";
      case 4: return "À valider";
      case 5: return "Refusée";
      case 6: return "Clôturée";
      default: return "Inconnu";
    }
  }
}
