package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.services.TicketService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private final SynchronisationManager synchronisationManager;
  private final ProduitRepository produitRepository;
  
  private static final Logger log = LoggerFactory.getLogger(CrmTicketSyncService.class);

  public CrmTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              TicketRepository tickets,
                              CompanyRepository companies,
                              UtilisateurRepository users,
                              UtilisateurInterneRepository utilisateurs,
                              TicketService ticketService,
                              SynchronisationManager synchronisationManager,
                              ProduitRepository produitRepository) {
    this.crmJdbc = crmJdbc;
    this.tickets = tickets;
    this.companies = companies;
    this.users = users;
    this.utilisateurs = utilisateurs;
    this.ticketService = ticketService;
    this.synchronisationManager = synchronisationManager;
    this.produitRepository = produitRepository;
  }

  // Synchronisation planifiée - non interruptible
  @Scheduled(cron = "${scheduling.crm-ticket-sync-cron:0 * * * * *}")
  @Transactional
  public void importerDepuisCrm() {
    log.info("🚀 Début de la synchronisation planifiée des tickets - {}", LocalDateTime.now());
    executerSynchronisationPlanifiee();
  }

  // Synchronisation manuelle - interruptible
  @Transactional
  public void importerDepuisCrmManuellement() {
    log.info("🚀 Début de la synchronisation manuelle des tickets - {}", LocalDateTime.now());
    executerSynchronisationManuelle();
  }

  private void executerSynchronisationPlanifiee() {
    final String sql =
    "SELECT Case_CaseId, Case_Description, Case_PrimaryPersonId, Case_ProblemNote, Case_Priority, Case_Status, Case_Stage, Case_CreatedBy, " +
    "       Case_PARCId, Case_PrimaryCompanyId, Case_CreatedDate, Case_UpdatedDate, Case_ReferenceId, Case_Opened, Case_Closed, Case_CustomerRef, " +
    "       Case_AssignedUserId, " + 
    "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
    "FROM dbo.Cases WHERE ISNULL(Case_Deleted,0) = 0 " +
    "AND Case_CreatedDate >= '2024-01-01'";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
    log.info("{} enregistrements récupérés depuis le CRM", rows.size());
    
    int compteurNouveaux = 0;
    int compteurMaj = 0;
    int compteurErreurs = 0;

    for (Map<String,Object> r : rows) {
      try {
        Integer caseId = toInt(r.get("Case_CaseId"));
        if (caseId == null) continue;
        if (toInt(r.get("Case_Deleted")) == 1) continue;

        Optional<Ticket> ticketOpt = tickets.findByIdExterneCrm(caseId);
        
        if (ticketOpt.isPresent()) {
          mettreAJourTicketExistant(ticketOpt.get(), r);
          compteurMaj++;
        } else {
          creerNouveauTicket(caseId, r);
          compteurNouveaux++;
        }
      } catch (Exception e) {
        log.error("❌ Erreur lors du traitement du ticket: {}", e.getMessage());
        compteurErreurs++;
      }
    }
    
    log.info("✅ Synchronisation planifiée terminée - {} nouveaux, {} mis à jour, {} erreurs", 
             compteurNouveaux, compteurMaj, compteurErreurs);
  }

  private void executerSynchronisationManuelle() {
    final String typeSync = "tickets";
    
    // Vérifier si une synchronisation est déjà en cours
    if (synchronisationManager.estEnCours(typeSync)) {
      throw new IllegalStateException("Une synchronisation des tickets est déjà en cours");
    }

    // Démarrer la synchronisation
    synchronisationManager.demarrerSynchronisation(typeSync);
    
    // Exécuter dans un thread séparé pour permettre l'interruption
    Thread syncThread = new Thread(() -> {
      try {
        synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
        
        final String sql =
        "SELECT Case_CaseId, Case_Description, Case_PrimaryPersonId, Case_ProblemNote, Case_Priority, Case_Status, Case_Stage, Case_CreatedBy, " +
        "       Case_PARCId, Case_PrimaryCompanyId, Case_CreatedDate, Case_UpdatedDate, Case_ReferenceId, Case_Opened, Case_Closed, Case_CustomerRef, " +
        "       Case_AssignedUserId, " + // AJOUT DE CETTE COLONNE
        "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
        "FROM dbo.Cases WHERE ISNULL(Case_Deleted,0) = 0 " +
        "AND Case_CreatedDate >= '2024-01-01'";

        List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
        log.info("{} enregistrements récupérés depuis le CRM", rows.size());
        
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurErreurs = 0;

        for (Map<String,Object> r : rows) {
          // Vérifier si l'arrêt a été demandé
          if (synchronisationManager.doitArreter(typeSync)) {
            log.info("🛑 Synchronisation manuelle des tickets arrêtée à la demande");
            return;
          }

          try {
            Integer caseId = toInt(r.get("Case_CaseId"));
            if (caseId == null) continue;
            if (toInt(r.get("Case_Deleted")) == 1) continue;

            Optional<Ticket> ticketOpt = tickets.findByIdExterneCrm(caseId);
            
            if (ticketOpt.isPresent()) {
              mettreAJourTicketExistant(ticketOpt.get(), r);
              compteurMaj++;
            } else {
              creerNouveauTicket(caseId, r);
              compteurNouveaux++;
            }
          } catch (Exception e) {
            log.error("❌ Erreur lors du traitement du ticket: {}", e.getMessage());
            compteurErreurs++;
          }
          
          // Petit délai pour permettre une interruption plus réactive
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            log.info("🛑 Synchronisation manuelle interrompue");
            Thread.currentThread().interrupt();
            return;
          }
        }
        
        log.info("✅ Synchronisation manuelle terminée - {} nouveaux, {} mis à jour, {} erreurs", 
                 compteurNouveaux, compteurMaj, compteurErreurs);
        
      } catch (Exception e) {
        log.error("❌ Erreur lors de la synchronisation manuelle des tickets: {}", e.getMessage());
      } finally {
        synchronisationManager.supprimerThread(typeSync);
      }
    });
    
    syncThread.start();
  }

    private void creerNouveauTicket(Integer caseId, Map<String, Object> r) {
        String titre       = Objects.toString(r.get("Case_Description"), null);
        String description = Objects.toString(r.get("Case_ProblemNote"), null);
        String prioriteStr = Objects.toString(r.get("Case_Priority"), null);
        String statutStr   = Objects.toString(r.get("Case_Status"), null);
        String stageStr    = Objects.toString(r.get("Case_Stage"), null);
        Integer produitId  = toInt(r.get("Case_PARCId"));
        Integer compId     = toInt(r.get("Case_PrimaryCompanyId"));
        Integer personId    = toInt(r.get("Case_PrimaryPersonId"));
        String referenceId = Objects.toString(r.get("Case_ReferenceId"), null);
        Integer assignedUserId = toInt(r.get("Case_AssignedUserId")); // NOUVEAU

        String ref         = Objects.toString(r.get("Case_CustomerRef"), null);
        Integer creeParUtilisateurId = toInt(r.get("Case_CreatedBy"));
        log.debug("Utilisateur: {}", creeParUtilisateurId);

        LocalDateTime opened = toLdt(r.get("Case_Opened"));
        LocalDateTime closed = toLdt(r.get("Case_Closed"));
        LocalDateTime created = toLdt(r.get("Case_CreatedDate"));
        LocalDateTime updated = toLdt(r.get("Case_UpdatedDate"));  
        Integer companyIdPortail = mapCompanyIdToCompanyId(compId);
        if (companyIdPortail == null) {
            return;
        }

        // Mapper l'utilisateur assigné
        Integer utilisateurAssignéId = mapAssignedUserIdToUtilisateurInterneId(assignedUserId);

        Ticket t = new Ticket();
        t.setReference(ref != null && !ref.isEmpty() ? ref : "CRM-" + caseId);
        t.setCompanyId(companyIdPortail);
        t.setProduitId(mapProduitIdToId(produitId));
        t.setTypeTicketId(mapTypeByHeuristique(titre, description));

        t.setPrioriteTicketId(mapPrioriteCrmStringToId(prioriteStr));
        t.setStatutTicketId(mapStageCrmStringToId(stageStr, statutStr));

        t.setTitre(titre != null ? titre : "Ticket CRM " + caseId);
        t.setDescription(description);
        t.setRaison(null);
        t.setPolitiqueAcceptee(true);
        t.setClientId(personId != null ? personId : null);
        t.setAffecteAUtilisateurId(utilisateurAssignéId); // UTILISATION ICI

        t.setDateCreation(created != null ? created : LocalDateTime.now());
        t.setDateMiseAJour(updated != null ? updated : LocalDateTime.now());
        t.setDateCloture(closed);
        t.setClotureParUtilisateurId(null);
        t.setReferenceId(referenceId);
        t.setIdExterneCrm(caseId);

        tickets.save(t);
        log.info("✅ Nouveau ticket créé: CRM ID {}, Stage: {}, Status: {}, Assigné à: {}", 
                caseId, stageStr, statutStr, utilisateurAssignéId);
    }
    private void mettreAJourTicketExistant(Ticket ticket, Map<String, Object> r) {
        String statutCrm = Objects.toString(r.get("Case_Status"), null);
        String stageCrm = Objects.toString(r.get("Case_Stage"), null);
        String prioriteCrm = Objects.toString(r.get("Case_Priority"), null);
        Integer assignedUserId = toInt(r.get("Case_AssignedUserId")); // NOUVEAU
        
        Integer nouveauStatutId = mapStageCrmStringToId(stageCrm, statutCrm);
        Integer nouvellePrioriteId = mapPrioriteCrmStringToId(prioriteCrm);
        Integer nouvelUtilisateurAssignéId = mapAssignedUserIdToUtilisateurInterneId(assignedUserId); // NOUVEAU
        
        boolean modification = false;
        Integer ancienStatutId = ticket.getStatutTicketId();
        
        // Vérifier si le statut a changé
        if (nouveauStatutId != null && !nouveauStatutId.equals(ticket.getStatutTicketId())) {
            ticket.setStatutTicketId(nouveauStatutId);
            modification = true;
            log.debug("Statut mis à jour pour le ticket {}: {} -> {} (Stage: {}, Status: {})", 
                    ticket.getIdExterneCrm(), ancienStatutId, nouveauStatutId, stageCrm, statutCrm);
        }
        
        // Vérifier si la priorité a changé
        if (nouvellePrioriteId != null && !nouvellePrioriteId.equals(ticket.getPrioriteTicketId())) {
            ticket.setPrioriteTicketId(nouvellePrioriteId);
            modification = true;
            log.debug("Priorité mise à jour pour le ticket {}: {} -> {}", 
                    ticket.getIdExterneCrm(), ticket.getPrioriteTicketId(), nouvellePrioriteId);
        }
        
        // Vérifier si l'utilisateur assigné a changé
        if (nouvelUtilisateurAssignéId != null && !nouvelUtilisateurAssignéId.equals(ticket.getAffecteAUtilisateurId())) {
            ticket.setAffecteAUtilisateurId(nouvelUtilisateurAssignéId);
            modification = true;
            log.debug("Utilisateur assigné mis à jour pour le ticket {}: {} -> {}", 
                    ticket.getIdExterneCrm(), ticket.getAffecteAUtilisateurId(), nouvelUtilisateurAssignéId);
        } else if (ticket.getAffecteAUtilisateurId() != null && nouvelUtilisateurAssignéId == null) {
            // Si l'utilisateur était assigné mais n'est plus assigné dans le CRM
            ticket.setAffecteAUtilisateurId(null);
            modification = true;
            log.debug("Utilisateur assigné retiré pour le ticket {}", ticket.getIdExterneCrm());
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
            log.debug("✅ Ticket mis à jour: CRM ID {}", ticket.getIdExterneCrm());
            
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
    if (produitIdCrm == null) {
      log.debug("Produit CRM ID est null");
      return null;
    }
    
    try {
      String idExterneCrm = String.valueOf(produitIdCrm);
      log.debug("Recherche du produit avec id_externe_crm: {}", idExterneCrm);
      
      Optional<Produit> produitOpt = produitRepository.findByIdExterneCrm(idExterneCrm);
      
      if (produitOpt.isPresent()) {
        Produit produit = produitOpt.get();
        log.debug("Produit trouvé: ID={}, Libellé={}", produit.getId(), produit.getLibelle());
        return produit.getId();
      } else {
        log.warn("⚠️ Produit non trouvé avec id_externe_crm: {}", idExterneCrm);
        
        // Optionnel: Créer le produit automatiquement si nécessaire
        // return creerProduitAutomatiquement(produitIdCrm);
        
        return null;
      }
      
    } catch (Exception e) {
      log.error("❌ Erreur lors de la recherche du produit avec id_externe_crm {}: {}", 
               produitIdCrm, e.getMessage());
      return null;
    }
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

  private Integer mapAssignedUserIdToUtilisateurInterneId(Integer assignedUserIdCrm) {
    if (assignedUserIdCrm == null) {
        log.debug("Assigned User ID CRM est null");
        return null;
    }
    
    try {
        String idExterneCrm = String.valueOf(assignedUserIdCrm);
        log.debug("Recherche de l'utilisateur interne avec id_externe_crm: {}", idExterneCrm);
        
        Optional<UtilisateurInterne> utilisateurOpt = utilisateurs.findByIdExterneCrm(idExterneCrm);
        
        if (utilisateurOpt.isPresent()) {
            UtilisateurInterne utilisateur = utilisateurOpt.get();
            log.debug("Utilisateur interne trouvé: ID={}, Nom={} {}", 
                     utilisateur.getId(), utilisateur.getPrenom(), utilisateur.getNom());
            return utilisateur.getId();
        } else {
            log.warn("⚠️ Utilisateur interne non trouvé avec id_externe_crm: {}", idExterneCrm);
            
            // Optionnel: Rechercher dans la table Utilisateur normale
            Optional<Utilisateur> utilisateurNormalOpt = users.findByIdExterneCrm(idExterneCrm);
            if (utilisateurNormalOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurNormalOpt.get();
                log.debug("Utilisateur normal trouvé (mais pas interne): ID={}, Nom={} {}", 
                         utilisateur.getId(), utilisateur.getPrenom(), utilisateur.getNom());
                // Ne pas retourner car ce n'est pas un utilisateur interne
            }
            
            return null;
        }
        
    } catch (Exception e) {
        log.error("❌ Erreur lors de la recherche de l'utilisateur interne avec id_externe_crm {}: {}", 
                 assignedUserIdCrm, e.getMessage());
        return null;
    }
}

  private void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
    try {
        // Utiliser la méthode existante de TicketService
        ticketService.envoyerNotificationsChangementStatut(t, ancienStatutId, nouveauStatutId);
        log.info("📢 Notification envoyée pour le changement de statut du ticket {} : {} -> {}", 
                t.getReference(), ancienStatutId, nouveauStatutId);
    } catch (Exception e) {
        log.error("❌ Erreur lors de l'envoi des notifications pour le ticket {} : {}", 
                 t.getReference(), e.getMessage());
    }
  }
}