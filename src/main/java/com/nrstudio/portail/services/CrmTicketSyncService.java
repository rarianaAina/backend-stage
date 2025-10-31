package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class CrmTicketSyncService {

  private final JdbcTemplate crmJdbc;
  private final TicketRepository tickets;
  private final CompanyRepository companies;
  private final UtilisateurRepository users;
  private final UtilisateurInterneRepository utilisateurs;
  private final TicketService ticketService;

  public CrmTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              TicketRepository tickets,
                              CompanyRepository companies,
                              UtilisateurRepository users,
                              UtilisateurInterneRepository utilisateurs,
                              TicketService ticketService) {
    this.crmJdbc = crmJdbc;
    this.tickets = tickets;
    this.companies = companies;
    this.users = users;
    this.utilisateurs = utilisateurs;
    this.ticketService = ticketService;
  }

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void importerDepuisCrm() {
    final String sql =
      "SELECT Case_CaseId, Case_Description, Case_PrimaryPersonId, Case_ProblemNote, Case_Priority, Case_Status, Case_Stage, Case_CreatedBy, " +
      "       Case_PARCId, Case_PrimaryCompanyId, Case_CreatedDate, Case_UpdatedDate, Case_Opened, Case_Closed, Case_CustomerRef, " +
      "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
      "FROM dbo.Cases WHERE ISNULL(Case_Deleted,0) = 0";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
    
    for (Map<String,Object> r : rows) {
      Integer caseId = toInt(r.get("Case_CaseId"));
      if (caseId == null) continue;
      if (toInt(r.get("Case_Deleted")) == 1) continue; // on ignore supprimés

      Optional<Ticket> ticketOpt = tickets.findByIdExterneCrm(caseId);
      
      if (ticketOpt.isPresent()) {
        // Ticket déjà existant - vérifier si le statut a changé
        mettreAJourTicketExistant(ticketOpt.get(), r);
      } else {
        // Nouveau ticket - création
        creerNouveauTicket(caseId, r);
      }
    }
  }

  private void creerNouveauTicket(Integer caseId, Map<String, Object> r) {
    String titre       = Objects.toString(r.get("Case_Description"), null);
    String description = Objects.toString(r.get("Case_ProblemNote"), null);
    String prioriteStr = Objects.toString(r.get("Case_Priority"), null);
    String statutStr   = Objects.toString(r.get("Case_Status"), null);
    String stageStr    = Objects.toString(r.get("Case_Stage"), null); // Nouveau champ
    Integer produitId  = toInt(r.get("Case_PARCId"));
    Integer compId     = toInt(r.get("Case_PrimaryCompanyId"));
    Integer personId    = toInt(r.get("Case_PrimaryPersonId"));
    String ref         = Objects.toString(r.get("Case_CustomerRef"), null);
    Integer creeParUtilisateurId = toInt(r.get("Case_CreatedBy"));
    System.out.println("Utilisateur: " + creeParUtilisateurId);

    // Recherche de l'utilisateur portail par id_externe_crm
    Integer utilisateurIdPortail = null;
    // if (creeParUtilisateurId != null) {
    //   UtilisateurInterne user = utilisateurs.findByIdExterneCrm(String.valueOf(creeParUtilisateurId)).orElse(null);
    //   if (user != null) {
    //     utilisateurIdPortail = user.getId();
    //   }
    // }

    LocalDateTime opened = toLdt(r.get("Case_Opened"));
    LocalDateTime closed = toLdt(r.get("Case_Closed"));
    LocalDateTime created = toLdt(r.get("Case_CreatedDate"));
    LocalDateTime updated = toLdt(r.get("Case_UpdatedDate"));  
    Integer companyIdPortail = mapCompanyIdToCompanyId(compId);
    if (companyIdPortail == null) {
      return;
    }

    Ticket t = new Ticket();
    t.setReference(ref != null && !ref.isEmpty() ? ref : "CRM-" + caseId);
    t.setCompanyId(companyIdPortail);
    t.setProduitId(mapProduitIdToId(produitId));
    t.setTypeTicketId(mapTypeByHeuristique(titre, description));

    t.setPrioriteTicketId(mapPrioriteCrmStringToId(prioriteStr));
    // Utiliser Case_Stage au lieu de Case_Status pour le mapping des statuts
    t.setStatutTicketId(mapStageCrmStringToId(stageStr, statutStr));

    t.setTitre(titre != null ? titre : "Ticket CRM " + caseId);
    t.setDescription(description);
    t.setRaison(null);
    t.setPolitiqueAcceptee(true);
    t.setClientId(personId != null ? personId : null);
    //t.setCreeParUtilisateurId(creeParUtilisateurId);
    t.setAffecteAUtilisateurId(null);

    t.setDateCreation(created != null ? created : LocalDateTime.now());
    t.setDateMiseAJour(updated != null ? updated : LocalDateTime.now());
    t.setDateCloture(closed);
    t.setClotureParUtilisateurId(null);

    t.setIdExterneCrm(caseId);

    tickets.save(t);
    System.out.println("Nouveau ticket créé: CRM ID " + caseId + ", Stage: " + stageStr + ", Status: " + statutStr);
  }

private void mettreAJourTicketExistant(Ticket ticket, Map<String, Object> r) {
    String statutCrm = Objects.toString(r.get("Case_Status"), null);
    String stageCrm = Objects.toString(r.get("Case_Stage"), null);
    String prioriteCrm = Objects.toString(r.get("Case_Priority"), null);
    
    Integer nouveauStatutId = mapStageCrmStringToId(stageCrm, statutCrm);
    Integer nouvellePrioriteId = mapPrioriteCrmStringToId(prioriteCrm);
    
    boolean modification = false;
    Integer ancienStatutId = ticket.getStatutTicketId(); // Sauvegarder l'ancien statut
    
    // Vérifier si le statut a changé
    if (nouveauStatutId != null && !nouveauStatutId.equals(ticket.getStatutTicketId())) {
        ticket.setStatutTicketId(nouveauStatutId);
        modification = true;
        System.out.println("Statut mis à jour pour le ticket " + ticket.getIdExterneCrm() + 
                         ": " + ancienStatutId + " -> " + nouveauStatutId +
                         " (Stage: " + stageCrm + ", Status: " + statutCrm + ")");
    }
    
    // Vérifier si la priorité a changé
    if (nouvellePrioriteId != null && !nouvellePrioriteId.equals(ticket.getPrioriteTicketId())) {
        ticket.setPrioriteTicketId(nouvellePrioriteId);
        modification = true;
        System.out.println("Priorité mise à jour pour le ticket " + ticket.getIdExterneCrm() + 
                         ": " + ticket.getPrioriteTicketId() + " -> " + nouvellePrioriteId);
    }
    
    // Mettre à jour la date de mise à jour
    LocalDateTime updated = toLdt(r.get("Case_UpdatedDate"));
    if (updated != null && !updated.equals(ticket.getDateMiseAJour())) {
        ticket.setDateMiseAJour(updated);
        modification = true;
    }
    
    // Mettre à jour la date de clôture si le stage est "Solved" ou statut est "Closed"
    if ("Solved".equals(stageCrm) || "Closed".equals(statutCrm)) {
        LocalDateTime closed = toLdt(r.get("Case_Closed"));
        if (closed != null && !closed.equals(ticket.getDateCloture())) {
            ticket.setDateCloture(closed);
            modification = true;
        }
    }
    
    // Sauvegarder seulement si des modifications ont été détectées
    if (modification) {
        tickets.save(ticket);
        System.out.println("Ticket mis à jour: CRM ID " + ticket.getIdExterneCrm());
        
        // ENVOYER LES NOTIFICATIONS SI LE STATUT A CHANGÉ
        if (ancienStatutId != null && !ancienStatutId.equals(nouveauStatutId)) {
            envoyerNotificationsChangementStatut(ticket, ancienStatutId, nouveauStatutId);
        }
    }
}
  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }

  private LocalDateTime toLdt(Object o) {
    if (o == null) return null;
    if (o instanceof Timestamp) return ((Timestamp)o).toLocalDateTime();
    if (o instanceof java.util.Date) return new Timestamp(((java.util.Date)o).getTime()).toLocalDateTime();
    return null;
  }

  private Integer mapPrioriteCrmStringToId(String s) {
    if (s == null) return 3; // ex. Normal
    if (s.equals("Urgent")) return 1;
    if (s.equals("High"))   return 2;
    if (s.equals("Normal")) return 3;
    return 3; // Low
  }

  private Integer mapStageCrmStringToId(String stage, String statut) {
    if (stage == null) {
      // Fallback sur Case_Status si Case_Stage est null
      return mapStatutCrmStringToId(statut);
    }
    
    // Mapping basé sur Case_Stage (les vraies étapes du workflow)
    switch (stage.toLowerCase()) {
      case "logged":
        return 1; // OUVERT
      case "confirmed":
        return 2; // EN_COURS
      case "waiting":
        return 3; // EN_ATTENTE
      case "solved":
        // Si solved, on vérifie Case_Status pour déterminer si c'est résolu ou clos
        if ("Closed".equals(statut)) {
          return 7; // CLOTURE
        } else {
          return 6; // RESOLU
        }
      default:
        // Fallback sur l'ancienne méthode si stage inconnu
        return mapStatutCrmStringToId(statut);
    }
  }

  // Garder l'ancienne méthode pour le fallback
  private Integer mapStatutCrmStringToId(String s) {
    if (s == null) return 1; // Open
    if (s.equals("Closed")) return 4;
    if (s.equals("Pending")) return 3;
    if (s.equals("In Progress")) return 2;
    return 1;
  }

  private Integer mapProduitIdToId(Integer produitIdCrm) {
    if (produitIdCrm == null) return null;
    // TODO : lookup dans table produit par id_externe_crm
    return null;
  }

  private Integer mapTypeByHeuristique(String titre, String desc) {
    return 1;
  }

  private Integer mapCompanyIdToCompanyId(Integer companyId) {
    if (companyId == null) return null;
    try {
      String idExterneCrm = String.valueOf(companyId);
      Company company = companies.findByIdExterneCrm(idExterneCrm).orElse(null);
      if (company != null) {
        return company.getId();
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  private void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
    try {
        // Utiliser la méthode existante de TicketService
        ticketService.envoyerNotificationsChangementStatut(t, ancienStatutId, nouveauStatutId);
        System.out.println("Notification envoyée pour le changement de statut du ticket " + t.getReference() + 
                         " : " + ancienStatutId + " -> " + nouveauStatutId);
    } catch (Exception e) {
        System.err.println("Erreur lors de l'envoi des notifications pour le ticket " + t.getReference() + 
                         " : " + e.getMessage());
        e.printStackTrace();
    }
}
}