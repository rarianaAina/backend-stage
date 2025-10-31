package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.dto.TicketAvecProduitDto;
import com.nrstudio.portail.dto.TicketAvecProduitPageReponse;
import com.nrstudio.portail.dto.TicketCreationRequete;
import com.nrstudio.portail.dto.TicketPageReponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class TicketService {

  private final TicketRepository tickets;
  private final JdbcTemplate crmJdbc;
  private final EmailNotificationService emailService;
  private final WhatsAppNotificationService whatsAppService;
  private final CompanyRepository companies;
  private final UtilisateurRepository utilisateurs;
  private final ProduitRepository produitRepository;
  private final NotificationWorkflowService notificationWorkflowService;


  public TicketService(TicketRepository tickets,
                       @Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                       EmailNotificationService emailService,
                       WhatsAppNotificationService whatsAppService,
                       CompanyRepository companies,
                       UtilisateurRepository utilisateurs,
                       ProduitRepository produitRepository,
                       NotificationWorkflowService notificationWorkflowService) {
    this.tickets = tickets;
    this.crmJdbc = crmJdbc;
    this.emailService = emailService;
    this.whatsAppService = whatsAppService;
    this.companies = companies;
    this.utilisateurs = utilisateurs;
    this.produitRepository = produitRepository;
    this.notificationWorkflowService = notificationWorkflowService;
  }

@Transactional
public Ticket creerEtSynchroniser(TicketCreationRequete r) {

    // Pour le produit
    String produitIdExterneCrm = String.valueOf(r.getProduitId());
    Optional<Produit> produitOpt = produitRepository.findByIdExterneCrm(produitIdExterneCrm);
    
    Integer produitIdLocal;
    if (produitOpt.isPresent()) {
        produitIdLocal = produitOpt.get().getId();
    } else {
        throw new IllegalArgumentException("Produit introuvable pour idExterneCrm: " + produitIdExterneCrm);
    }

    // Pour le client - récupérer l'idExterneCrm à partir de l'ID local
    Integer clientIdLocal = r.getClientId();
    Optional<Utilisateur> utilisateurOpt = utilisateurs.findById(clientIdLocal); // Chercher par ID local
    
    String clientIdExterne;
    if (utilisateurOpt.isPresent()) {
        clientIdExterne = utilisateurOpt.get().getIdExterneCrm();
        // Si vous voulez un Integer, convertissez-le
        // clientIdExterne = Integer.valueOf(utilisateurOpt.get().getIdExterneCrm());
    } else {
        throw new IllegalArgumentException("Utilisateur introuvable pour id: " + clientIdLocal);
    }

    Ticket t = new Ticket();
    t.setCompanyId(r.getCompanyId());
    t.setClientId(Integer.valueOf(clientIdExterne)); // ou Integer.valueOf(clientIdExterne) si besoin
    t.setProduitId(produitIdLocal);
    t.setTypeTicketId(r.getTypeTicketId());
    t.setPrioriteTicketId(r.getPrioriteTicketId());
    t.setStatutTicketId(1);
    t.setTitre(r.getRaison());
    t.setDescription(r.getDescription());
    t.setRaison(r.getRaison());
    t.setPolitiqueAcceptee(true);
    t.setCreeParUtilisateurId(r.getCreeParUtilisateurId());
    t.setAffecteAUtilisateurId(r.getAffecteAUtilisateurId());
    t.setDateCreation(LocalDateTime.now());
    t.setDateMiseAJour(LocalDateTime.now());

    t.setReference("TCK-" + System.currentTimeMillis());
    t = tickets.save(t);

    // // 2) Créer le Case dans le CRM (dbo.Cases)
    // String caseDescription = truncate(t.getTitre(), 40);
    // String caseProblemNote = t.getDescription() != null ? t.getDescription() : "";
    // String casePriority = mapPrioriteIdToCrmString(t.getPrioriteTicketId()); // TODO: adapter
    // String caseStatus   = mapStatutIdToCrmString(t.getStatutTicketId());     // TODO: adapter
    // String caseProduct  = mapProduitIdToCrmString(t.getProduitId());         // TODO: adapter

    // // Company côté CRM
    // Integer crmCompanyId = mapCompanyIdToCrmCompanyId(t.getCompanyId());

    // Integer caseId = crmJdbc.queryForObject(
    //   "INSERT INTO dbo.Cases " +
    //   " (Case_PrimaryCompanyId, Case_Description, Case_ProblemNote, Case_Priority, Case_Status, " +
    //   "  Case_Product, Case_Opened, Case_Deleted, Case_Source, Case_CustomerRef) " +
    //   " VALUES (?,?,?,?,?,?, GETDATE(), 0, 'Portail', ?) ; " +
    //   " SELECT CAST(SCOPE_IDENTITY() AS INT);",
    //   Integer.class,
    //   crmCompanyId, caseDescription, caseProblemNote, casePriority, caseStatus,
    //   caseProduct, t.getReference()
    // );

    // if (caseId != null) {
    //   t.setIdExterneCrm(caseId);
    //   t.setDateMiseAJour(LocalDateTime.now());
    //   t = tickets.save(t);
    // }

    envoyerNotificationsCreation(t);

    return t;
}
  @Transactional
  public Ticket changerStatut(Integer ticketId, Integer nouveauStatutId, Integer utilisateurId) {
    Ticket t = tickets.findById(ticketId)
      .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable"));

    Integer ancienStatutId = t.getStatutTicketId();
    t.setStatutTicketId(nouveauStatutId);
    t.setDateMiseAJour(LocalDateTime.now());

    if (nouveauStatutId == 4) {
      t.setDateCloture(LocalDateTime.now());
      t.setClotureParUtilisateurId(utilisateurId);
    }

    t = tickets.save(t);

    if (t.getIdExterneCrm() != null) {
      String nouveauStatutCrm = mapStatutIdToCrmString(nouveauStatutId);
      crmJdbc.update(
        "UPDATE dbo.Cases SET Case_Status = ?, Case_Closed = ? WHERE Case_CaseId = ?",
        nouveauStatutCrm,
        nouveauStatutId == 4 ? LocalDateTime.now() : null,
        t.getIdExterneCrm()
      );
    }

    envoyerNotificationsChangementStatut(t, ancienStatutId, nouveauStatutId);

    return t;
  }

  @Transactional
  public List<Ticket> listerTicketsCompany(Integer companyId) {
    return tickets.findAll().stream()
      .filter(ticket -> ticket.getCompanyId().equals(companyId))
      .toList();
  }

  //Tickets par utilisateur
  public List<Ticket> listerTicketsUtilisateur(Integer utilisateurId) {
    return tickets.findAll().stream()
      .filter(ticket -> utilisateurId.equals(ticket.getCreeParUtilisateurId()))
      .toList();
  }

  @Transactional
  public List<Ticket> listerTicketsConsultant(Integer consultantId) {
    return tickets.findAll().stream()
      .filter(ticket -> consultantId.equals(ticket.getAffecteAUtilisateurId()))
      .toList();
  }

  //Ticket par utilisateur avec pagination
  // public List<Ticket> listerTicketsUtilisateurAvecPagination(Integer utilisateurId, int page, int size) {
  //   return tickets.findAll().stream()
  //     .filter(ticket -> utilisateurId.equals(ticket.getCreeParUtilisateurId()))
  //     .skip(page * size)
  //     .limit(size)
  //     .toList();
  // }

  public List<Ticket> listerTicketsUtilisateurAvecPagination(Integer utilisateurId, int page, int size) {
      // trouver l'id_externe_crm et non l'utilisateur par rapport à utilisateurId
      Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
          .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
      Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? Integer.valueOf(utilisateur.getIdExterneCrm()) : null;  
    return tickets.findAll().stream()
      .filter(ticket -> utilisateurIdClient.equals(ticket.getClientId()))
      .skip(page * size)
      .limit(size)
      .toList();
  }

  public TicketAvecProduitPageReponse listerTicketsUtilisateurAvecPaginationEtFiltres(
    Integer utilisateurId,
    int page,
    int size,
    String statutTicketIdStr,
    String reference,
    String produitIdStr,
    String dateDebut,
    String dateFin) {

    Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? Integer.valueOf(utilisateur.getIdExterneCrm()) : null;

    Stream<Ticket> ticketStream = tickets.findAll().stream()
        .filter(ticket -> utilisateurIdClient.equals(ticket.getClientId()));

    // Filtres existants
    if (statutTicketIdStr != null && !statutTicketIdStr.isEmpty()) {
      try {
        Integer statutTicketId = Integer.valueOf(statutTicketIdStr);
        ticketStream = ticketStream.filter(ticket -> statutTicketId.equals(ticket.getStatutTicketId()));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("statutTicketId invalide : " + statutTicketIdStr);
      }
    }

    if (reference != null && !reference.isEmpty()) {
      ticketStream = ticketStream.filter(ticket -> 
          ticket.getReference() != null &&
          ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
    }

    if (produitIdStr != null && !produitIdStr.isEmpty()) {
      try {
        Integer produitId = Integer.valueOf(produitIdStr);
        ticketStream = ticketStream.filter(ticket -> 
            ticket.getProduitId() != null &&
            ticket.getProduitId().equals(produitId));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("produitId invalide : " + produitIdStr);
      }
    }

    if (dateDebut != null && !dateDebut.isEmpty()) {
      LocalDate debut = LocalDate.parse(dateDebut);
      ticketStream = ticketStream.filter(ticket -> {
        if (ticket.getDateCreation() == null) return false;
        return !ticket.getDateCreation().toLocalDate().isBefore(debut);
      });
    }

    if (dateFin != null && !dateFin.isEmpty()) {
      LocalDate fin = LocalDate.parse(dateFin);
      ticketStream = ticketStream.filter(ticket -> {
        if (ticket.getDateCreation() == null) return false;
        return !ticket.getDateCreation().toLocalDate().isAfter(fin);
      });
    }

    // Convertir les tickets en DTOs avec les noms de produits
    List<TicketAvecProduitDto> ticketDtos = ticketStream
        .skip(page * size)
        .limit(size)
        .map(ticket -> convertirEnAvecProduitDto(ticket))
        .toList();

    long totalElements = countTicketsUtilisateurAvecFiltres(utilisateurId, statutTicketIdStr, reference, produitIdStr, dateDebut, dateFin);
    int totalPages = (int) Math.ceil((double) totalElements / size);

    return new TicketAvecProduitPageReponse(ticketDtos, page, totalPages, totalElements, size);
}

// Méthode utilitaire pour convertir Ticket en TicketAvecProduitDto
private TicketAvecProduitDto convertirEnAvecProduitDto(Ticket ticket) {
    TicketAvecProduitDto dto = new TicketAvecProduitDto();
    dto.setId(ticket.getId().toString());
    dto.setReference(ticket.getReference());
    dto.setProduitId(ticket.getProduitId());
    dto.setDescription(ticket.getDescription());
    dto.setTitre(ticket.getTitre());
    dto.setPrioriteTicketId(ticket.getPrioriteTicketId().toString());
    dto.setDateCreation(ticket.getDateCreation());
    dto.setDateCloture(ticket.getDateCloture());
    dto.setEtat(ticket.getStatutTicketId().toString()); // Adaptez selon votre logique d'état
    dto.setCompanyName(ticket.getCompanyId() != null ? 
        companies.findById(ticket.getCompanyId())
                 .map(Company::getNom)
                 .orElse("Entreprise inconnue") 
        : "Entreprise inconnue");

    
    
    // Récupérer le nom du produit
    String produitNom = "Produit inconnu";
    if (ticket.getProduitId() != null) {
        Optional<Produit> produit = produitRepository.findById(ticket.getProduitId());
        if (produit.isPresent()) {
            // Adaptez selon le champ qui contient le nom dans votre entité Produit
            produitNom = produit.get().getCodeProduit(); // ou getLibelle(), getDescription(), etc.
        }
    }
    dto.setProduitNom(produitNom);
    
    return dto;
}
  // Méthode pour obtenir le nombre total de tickets (pour la pagination)
  public long countTicketsUtilisateurAvecFiltres(
    Integer utilisateurId,
    String statutTicketIdStr,
    String reference,
    String produitIdStr,
    String dateDebut,
    String dateFin) {

  Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
      .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
  Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? Integer.valueOf(utilisateur.getIdExterneCrm()) : null;

  Stream<Ticket> ticketStream = tickets.findAll().stream()
      .filter(ticket -> utilisateurIdClient.equals(ticket.getClientId()));

  // Filtres
  if (statutTicketIdStr != null && !statutTicketIdStr.isEmpty()) {
    try {
      Integer statutTicketId = Integer.valueOf(statutTicketIdStr);
      ticketStream = ticketStream.filter(ticket -> statutTicketId.equals(ticket.getStatutTicketId()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("statutTicketId invalide : " + statutTicketIdStr);
    }
  }

  if (reference != null && !reference.isEmpty()) {
    ticketStream = ticketStream.filter(ticket ->
        ticket.getReference() != null &&
        ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
  }

  if (produitIdStr != null && !produitIdStr.isEmpty()) {
    try {
      Integer produitId = Integer.valueOf(produitIdStr);
      ticketStream = ticketStream.filter(ticket ->
          ticket.getProduitId() != null &&
          ticket.getProduitId().equals(produitId));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("produitId invalide : " + produitIdStr);
    }
  }

  if (dateDebut != null && !dateDebut.isEmpty()) {
    LocalDate debut = LocalDate.parse(dateDebut);
    ticketStream = ticketStream.filter(ticket -> {
      if (ticket.getDateCreation() == null) return false;
      return !ticket.getDateCreation().toLocalDate().isBefore(debut);
    });
  }

  if (dateFin != null && !dateFin.isEmpty()) {
    LocalDate fin = LocalDate.parse(dateFin);
    ticketStream = ticketStream.filter(ticket -> {
      if (ticket.getDateCreation() == null) return false;
      return !ticket.getDateCreation().toLocalDate().isAfter(fin);
    });
  }

  return ticketStream.count();
}


  // private void envoyerNotificationsCreation(Ticket t) {
  //     Integer clientId = t.getClientId();
  //     System.out.println("Client ID: " + clientId);
  //   try {
  //     Utilisateur createur = utilisateurs.findByIdExterneCrm(t.getClientId().toString()).orElse(null);

  //     System.out.println(createur.getEmail());
  //     if (createur != null && createur.getEmail() != null) {
  //       emailService.envoyerNotificationTicketCree(
  //         createur.getEmail(),
  //         t.getReference(),
  //         t.getTitre()
  //       );

  //       // if (createur.getTelephone() != null) {
  //       //   whatsAppService.envoyerNotificationTicketCree(
  //       //     createur.getTelephone(),
  //       //     t.getReference(),
  //       //     t.getTitre()
  //       //   );
  //       // }
  //     }

  //     // if (t.getAffecteAUtilisateurId() != null) {
  //     //   Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
  //     //   if (consultant != null && consultant.getEmail() != null) {
  //     //     emailService.envoyerNotificationTicketCree(
  //     //       consultant.getEmail(),
  //     //       t.getReference(),
  //     //       t.getTitre()
  //     //     );
  //     //   }
  //     // }
  //   } catch (Exception e) {
  //     System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
  //   }
  // }

  private void envoyerNotificationsCreation(Ticket t) {
      try {
          notificationWorkflowService.executerWorkflowNotification("CREATION_TICKET", t);
      } catch (Exception e) {
          System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
          e.printStackTrace();
      }
  }

  public void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
    try {
        notificationWorkflowService.executerWorkflowNotification("MODIFICATION_STATUT_TICKET", t, ancienStatutId, nouveauStatutId);
    } catch (Exception e) {
        System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
        e.printStackTrace();
    }
}
  // private void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
  //   try {
  //     String ancienStatut = mapStatutIdToCrmString(ancienStatutId);
  //     String nouveauStatut = mapStatutIdToCrmString(nouveauStatutId);

  //     Utilisateur createur = utilisateurs.findById(t.getCreeParUtilisateurId()).orElse(null);
  //     System.out.println(createur.getTelephone());
  //     if (createur != null && createur.getEmail() != null) {
  //       emailService.envoyerNotificationChangementStatut(
  //         createur.getEmail(),
  //         t.getReference(),
  //         ancienStatut,
  //         nouveauStatut
  //       );


  //       if (createur.getTelephone() != null) {
  //         whatsAppService.envoyerNotificationChangementStatut(
  //           createur.getTelephone(),
  //           t.getReference(),
  //           nouveauStatut
  //         );
  //       }
  //     }

  //     if (t.getAffecteAUtilisateurId() != null) {
  //       Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
  //       if (consultant != null && consultant.getEmail() != null) {
  //         emailService.envoyerNotificationChangementStatut(
  //           consultant.getEmail(),
  //           t.getReference(),
  //           ancienStatut,
  //           nouveauStatut
  //         );
  //       }
  //     }
  //   } catch (Exception e) {
  //     System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
  //   }
  // }

public TicketAvecProduitPageReponse listerTicketsAdminAvecPaginationEtFiltres(
    int page,
    int size,
    String etat,
    String reference,
    String produit,
    String dateDebut,
    String dateFin,
    String societe,
    String priorite) {

    Stream<Ticket> ticketStream = tickets.findAll().stream();

    System.out.println("Etat: " + etat);
    // Filtre par statut
    if (etat != null && !etat.isEmpty()) {
        try {
            Integer statutId = Integer.valueOf(etat);
            ticketStream = ticketStream.filter(ticket -> statutId.equals(ticket.getStatutTicketId()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("statut invalide : " + etat);
        }
    }

    // Filtre par référence
    if (reference != null && !reference.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> 
            ticket.getReference() != null &&
            ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
    }

    // Filtre par produit
    if (produit != null && !produit.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getProduitId() == null) return false;
            try {
                Produit produitEntity = produitRepository.findById(ticket.getProduitId()).orElse(null);
                return produitEntity != null && 
                       produitEntity.getCodeProduit().toLowerCase().contains(produit.toLowerCase());
            } catch (Exception e) {
                return false;
            }
        });
    }

    // Filtre par date début
    if (dateDebut != null && !dateDebut.isEmpty()) {
        LocalDate debut = LocalDate.parse(dateDebut);
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getDateCreation() == null) return false;
            return !ticket.getDateCreation().toLocalDate().isBefore(debut);
        });
    }

    // Filtre par date fin
    if (dateFin != null && !dateFin.isEmpty()) {
        LocalDate fin = LocalDate.parse(dateFin);
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getDateCreation() == null) return false;
            return !ticket.getDateCreation().toLocalDate().isAfter(fin);
        });
    }

    // Filtre par société
    if (societe != null && !societe.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getCompanyId() == null) return false;
            try {
                Company company = companies.findById(ticket.getCompanyId()).orElse(null);
                return company != null && 
                       company.getNom().toLowerCase().contains(societe.toLowerCase());
            } catch (Exception e) {
                return false;
            }
        });
    }

    // Filtre par priorité
    if (priorite != null && !priorite.isEmpty()) {
        try {
            Integer prioriteId = Integer.valueOf(priorite);
            ticketStream = ticketStream.filter(ticket -> prioriteId.equals(ticket.getPrioriteTicketId()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("priorite invalide : " + priorite);
        }
    }

    // Trier par date de création décroissante
    List<Ticket> ticketsFiltres = ticketStream
        .sorted((t1, t2) -> t2.getDateCreation().compareTo(t1.getDateCreation()))
        .toList();

    // Pagination
    long totalElements = ticketsFiltres.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);

    // Convertir les tickets en TicketAvecProduitDto
    List<TicketAvecProduitDto> ticketDtos = ticketsFiltres.stream()
        .skip(page * size)
        .limit(size)
        .map(this::convertirEnAvecProduitDto)
        .toList();

    return new TicketAvecProduitPageReponse(ticketDtos, page, totalPages, totalElements, size);
}
  private String truncate(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  // ====== MAPPINGS À ADAPTER à tes tables référentielles ======
  private String mapPrioriteIdToCrmString(Integer prioriteId) {
    if (prioriteId == null) return null;
    // exemple : 1=Low, 2=Normal, 3=High, 4=Urgent
    switch (prioriteId) {
      case 4: return "Urgent";
      case 3: return "High";
      case 2: return "Normal";
      default: return "Low";
    }
  }

  private String mapStatutIdToCrmString(Integer statutId) {
    if (statutId == null) return "Open";
    // exemple : 1=Open, 2=In Progress, 3=Pending, 4=Closed
    switch (statutId) {
      case 4: return "Closed";
      case 3: return "Pending";
      case 2: return "In Progress";
      default: return "Open";
    }
  }

  private String mapProduitIdToCrmString(Integer produitId) {
    // si tu as un catalogue CRM en texte, retourne le libellé attendu
    return null; // facultatif
  }

  private Integer mapCompanyIdToCrmCompanyId(Integer companyId) {
    try {
      Company company = companies.findById(companyId).orElse(null);
      if (company != null && company.getIdExterneCrm() != null) {
        return Integer.valueOf(company.getIdExterneCrm());
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
