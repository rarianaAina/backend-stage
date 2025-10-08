package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.dto.TicketCreationRequete;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

  private final TicketRepository tickets;
  private final JdbcTemplate crmJdbc;
  private final EmailNotificationService emailService;
  private final WhatsAppNotificationService whatsAppService;
  private final CompanyRepository companies;

  public TicketService(TicketRepository tickets,
                       @Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                       EmailNotificationService emailService,
                       WhatsAppNotificationService whatsAppService,
                       CompanyRepository companies) {
    this.tickets = tickets;
    this.crmJdbc = crmJdbc;
    this.emailService = emailService;
    this.whatsAppService = whatsAppService;
    this.companies = companies;
  }

  @Transactional
  public Ticket creerEtSynchroniser(TicketCreationRequete r) {

    if (!r.isPolitiqueAcceptee()) {
      throw new IllegalArgumentException("Les politiques doivent être acceptées.");
    }

    // 1) Créer dans PORTAIL_CLIENT
    Ticket t = new Ticket();
    t.setCompanyId(r.getCompanyId());
    t.setProduitId(r.getProduitId());
    t.setTypeTicketId(r.getTypeTicketId());
    t.setPrioriteTicketId(r.getPrioriteTicketId());
    t.setStatutTicketId(r.getStatutTicketId());
    t.setTitre(r.getTitre());
    t.setDescription(r.getDescription());
    t.setRaison(r.getRaison());
    t.setPolitiqueAcceptee(true);
    t.setCreeParUtilisateurId(r.getCreeParUtilisateurId());
    t.setAffecteAUtilisateurId(r.getAffecteAUtilisateurId());
    t.setDateCreation(LocalDateTime.now());
    t.setDateMiseAJour(LocalDateTime.now());

    // référence lisible côté portail
    t.setReference("TCK-" + System.currentTimeMillis());
    t = tickets.save(t);

    // 2) Créer le Case dans le CRM (dbo.Cases)
    String caseDescription = truncate(t.getTitre(), 40);
    String caseProblemNote = t.getDescription() != null ? t.getDescription() : "";
    String casePriority = mapPrioriteIdToCrmString(t.getPrioriteTicketId()); // TODO: adapter
    String caseStatus   = mapStatutIdToCrmString(t.getStatutTicketId());     // TODO: adapter
    String caseProduct  = mapProduitIdToCrmString(t.getProduitId());         // TODO: adapter

    // Company côté CRM
    Integer crmCompanyId = mapCompanyIdToCrmCompanyId(t.getCompanyId());

    Integer caseId = crmJdbc.queryForObject(
      "INSERT INTO dbo.Cases " +
      " (Case_PrimaryCompanyId, Case_Description, Case_ProblemNote, Case_Priority, Case_Status, " +
      "  Case_Product, Case_Opened, Case_Deleted, Case_Source, Case_CustomerRef) " +
      " VALUES (?,?,?,?,?,?, GETDATE(), 0, 'Portail', ?) ; " +
      " SELECT CAST(SCOPE_IDENTITY() AS INT);",
      Integer.class,
      crmCompanyId, caseDescription, caseProblemNote, casePriority, caseStatus,
      caseProduct, t.getReference()
    );

    if (caseId != null) {
      t.setIdExterneCrm(caseId);
      t.setDateMiseAJour(LocalDateTime.now());
      t = tickets.save(t);
    }

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
  public List<Ticket> listerTicketsClient(Integer clientId) {
    return tickets.findAll().stream()
      .filter(ticket -> ticket.getClientId().equals(clientId))
      .toList();
  }

  @Transactional
  public List<Ticket> listerTicketsConsultant(Integer consultantId) {
    return tickets.findAll().stream()
      .filter(ticket -> consultantId.equals(ticket.getAffecteAUtilisateurId()))
      .toList();
  }

  private void envoyerNotificationsCreation(Ticket t) {
    try {
      Utilisateur createur = utilisateurs.findById(t.getCreeParUtilisateurId()).orElse(null);
      if (createur != null && createur.getEmail() != null) {
        emailService.envoyerNotificationTicketCree(
          createur.getEmail(),
          t.getReference(),
          t.getTitre()
        );

        if (createur.getTelephone() != null) {
          whatsAppService.envoyerNotificationTicketCree(
            createur.getTelephone(),
            t.getReference(),
            t.getTitre()
          );
        }
      }

      if (t.getAffecteAUtilisateurId() != null) {
        Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
        if (consultant != null && consultant.getEmail() != null) {
          emailService.envoyerNotificationTicketCree(
            consultant.getEmail(),
            t.getReference(),
            t.getTitre()
          );
        }
      }
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
    }
  }

  private void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
    try {
      String ancienStatut = mapStatutIdToCrmString(ancienStatutId);
      String nouveauStatut = mapStatutIdToCrmString(nouveauStatutId);

      Utilisateur createur = utilisateurs.findById(t.getCreeParUtilisateurId()).orElse(null);
      if (createur != null && createur.getEmail() != null) {
        emailService.envoyerNotificationChangementStatut(
          createur.getEmail(),
          t.getReference(),
          ancienStatut,
          nouveauStatut
        );

        if (createur.getTelephone() != null) {
          whatsAppService.envoyerNotificationChangementStatut(
            createur.getTelephone(),
            t.getReference(),
            nouveauStatut
          );
        }
      }

      if (t.getAffecteAUtilisateurId() != null) {
        Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
        if (consultant != null && consultant.getEmail() != null) {
          emailService.envoyerNotificationChangementStatut(
            consultant.getEmail(),
            t.getReference(),
            ancienStatut,
            nouveauStatut
          );
        }
      }
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
    }
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
