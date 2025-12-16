package com.nrstudio.portail.services.synchronisations.configurations.executor;

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
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import com.nrstudio.portail.services.synchronisations.configurations.processors.TicketDataProcessor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TicketSyncExecutor {

    private final JdbcTemplate crmJdbc;
    private final TicketRepository tickets;
    private final CompanyRepository companies;
    private final UtilisateurRepository users;
    private final UtilisateurInterneRepository utilisateurs;
    private final TicketService ticketService;
    private final ProduitRepository produitRepository;
    private final TicketDataProcessor dataProcessor;
    
    private static final Logger log = LoggerFactory.getLogger(TicketSyncExecutor.class);

    public TicketSyncExecutor(JdbcTemplate crmJdbc,
                            TicketRepository tickets,
                            CompanyRepository companies,
                            UtilisateurRepository users,
                            UtilisateurInterneRepository utilisateurs,
                            TicketService ticketService,
                            ProduitRepository produitRepository,
                            TicketDataProcessor dataProcessor) {
        this.crmJdbc = crmJdbc;
        this.tickets = tickets;
        this.companies = companies;
        this.users = users;
        this.utilisateurs = utilisateurs;
        this.ticketService = ticketService;
        this.produitRepository = produitRepository;
        this.dataProcessor = dataProcessor;
    }

    public void executerSynchronisationPlanifiee() {
        final String sql = getSqlRequete();
        List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
        log.info("{} enregistrements récupérés depuis le CRM", rows.size());
        
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurErreurs = 0;

        for (Map<String,Object> r : rows) {
            try {
                Integer caseId = dataProcessor.toInt(r.get("Case_CaseId"));
                if (caseId == null) continue;
                if (dataProcessor.toInt(r.get("Case_Deleted")) == 1) continue;

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

    public void executerSynchronisationManuelle(SynchronisationManager synchronisationManager) {
        final String typeSync = "tickets";
        
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des tickets est déjà en cours");
        }

        synchronisationManager.demarrerSynchronisation(typeSync);
        
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                executerSynchronisationManuelleAvecInterruption(synchronisationManager, typeSync);
            } catch (Exception e) {
                log.error("❌ Erreur lors de la synchronisation manuelle des tickets: {}", e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
            }
        });
        
        syncThread.start();
    }

    private void executerSynchronisationManuelleAvecInterruption(SynchronisationManager synchronisationManager, String typeSync) {
        final String sql = getSqlRequete();
        List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
        log.info("{} enregistrements récupérés depuis le CRM", rows.size());
        
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurErreurs = 0;

        for (Map<String,Object> r : rows) {
            if (synchronisationManager.doitArreter(typeSync)) {
                log.info("🛑 Synchronisation manuelle des tickets arrêtée à la demande");
                return;
            }

            try {
                Integer caseId = dataProcessor.toInt(r.get("Case_CaseId"));
                if (caseId == null) continue;
                if (dataProcessor.toInt(r.get("Case_Deleted")) == 1) continue;

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
    }

    private void creerNouveauTicket(Integer caseId, Map<String, Object> r) {
        String titre = dataProcessor.toString(r.get("Case_Description"), null);
        String description = dataProcessor.toString(r.get("Case_ProblemNote"), null);
        String prioriteStr = dataProcessor.toString(r.get("Case_Priority"), null);
        String statutStr = dataProcessor.toString(r.get("Case_Status"), null);
        String stageStr = dataProcessor.toString(r.get("Case_Stage"), null);
        Integer produitId = dataProcessor.toInt(r.get("Case_PARCId"));
        Integer compId = dataProcessor.toInt(r.get("Case_PrimaryCompanyId"));
        Integer personId = dataProcessor.toInt(r.get("Case_PrimaryPersonId"));
        String referenceId = dataProcessor.toString(r.get("Case_ReferenceId"), null);
        Integer assignedUserId = dataProcessor.toInt(r.get("Case_AssignedUserId"));

        String ref = dataProcessor.toString(r.get("Case_CustomerRef"), null);
        Integer creeParUtilisateurId = dataProcessor.toInt(r.get("Case_CreatedBy"));
        log.debug("Utilisateur: {}", creeParUtilisateurId);

        LocalDateTime opened = dataProcessor.toLdt(r.get("Case_Opened"));
        LocalDateTime closed = dataProcessor.toLdt(r.get("Case_Closed"));
        LocalDateTime created = dataProcessor.toLdt(r.get("Case_CreatedDate"));
        LocalDateTime updated = dataProcessor.toLdt(r.get("Case_UpdatedDate"));  
        
        Integer companyIdPortail = mapCompanyIdToCompanyId(compId);
        if (companyIdPortail == null) {
            return;
        }

        Integer utilisateurAssignéId = mapAssignedUserIdToUtilisateurInterneId(assignedUserId);

        Ticket t = new Ticket();
        t.setReference(ref != null && !ref.isEmpty() ? ref : "CRM-" + caseId);
        t.setCompanyId(companyIdPortail);
        t.setProduitId(mapProduitIdToId(produitId));
        t.setTypeTicketId(mapTypeByHeuristique(titre, description));

        t.setPrioriteTicketId(dataProcessor.mapPrioriteCrmStringToId(prioriteStr));
        t.setStatutTicketId(dataProcessor.mapStageCrmStringToId(stageStr, statutStr));

        t.setTitre(titre != null ? titre : "Ticket CRM " + caseId);
        t.setDescription(description);
        t.setRaison(null);
        t.setPolitiqueAcceptee(true);
        t.setClientId(personId != null ? personId : null);
        t.setAffecteAUtilisateurId(utilisateurAssignéId);

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
        String statutCrm = dataProcessor.toString(r.get("Case_Status"), null);
        String stageCrm = dataProcessor.toString(r.get("Case_Stage"), null);
        String prioriteCrm = dataProcessor.toString(r.get("Case_Priority"), null);
        Integer assignedUserId = dataProcessor.toInt(r.get("Case_AssignedUserId"));
        
        Integer nouveauStatutId = dataProcessor.mapStageCrmStringToId(stageCrm, statutCrm);
        Integer nouvellePrioriteId = dataProcessor.mapPrioriteCrmStringToId(prioriteCrm);
        Integer nouvelUtilisateurAssignéId = mapAssignedUserIdToUtilisateurInterneId(assignedUserId);
        
        boolean modification = false;
        Integer ancienStatutId = ticket.getStatutTicketId();
        
        if (nouveauStatutId != null && !nouveauStatutId.equals(ticket.getStatutTicketId())) {
            ticket.setStatutTicketId(nouveauStatutId);
            modification = true;
            log.debug("Statut mis à jour pour le ticket {}: {} -> {} (Stage: {}, Status: {})", 
                    ticket.getIdExterneCrm(), ancienStatutId, nouveauStatutId, stageCrm, statutCrm);
        }
        
        if (nouvellePrioriteId != null && !nouvellePrioriteId.equals(ticket.getPrioriteTicketId())) {
            ticket.setPrioriteTicketId(nouvellePrioriteId);
            modification = true;
            log.debug("Priorité mise à jour pour le ticket {}: {} -> {}", 
                    ticket.getIdExterneCrm(), ticket.getPrioriteTicketId(), nouvellePrioriteId);
        }
        
        if (nouvelUtilisateurAssignéId != null && !nouvelUtilisateurAssignéId.equals(ticket.getAffecteAUtilisateurId())) {
            ticket.setAffecteAUtilisateurId(nouvelUtilisateurAssignéId);
            modification = true;
            log.debug("Utilisateur assigné mis à jour pour le ticket {}: {} -> {}", 
                    ticket.getIdExterneCrm(), ticket.getAffecteAUtilisateurId(), nouvelUtilisateurAssignéId);
        } else if (ticket.getAffecteAUtilisateurId() != null && nouvelUtilisateurAssignéId == null) {
            ticket.setAffecteAUtilisateurId(null);
            modification = true;
            log.debug("Utilisateur assigné retiré pour le ticket {}", ticket.getIdExterneCrm());
        }
        
        LocalDateTime updated = dataProcessor.toLdt(r.get("Case_UpdatedDate"));
        if (updated != null && !updated.equals(ticket.getDateMiseAJour())) {
            ticket.setDateMiseAJour(updated);
            modification = true;
        }
        
        if ("Solved".equals(stageCrm) || "Closed".equals(statutCrm)) {
            LocalDateTime closed = dataProcessor.toLdt(r.get("Case_Closed"));
            if (closed != null && !closed.equals(ticket.getDateCloture())) {
                ticket.setDateCloture(closed);
                modification = true;
            }
        }
        
        if (modification) {
            tickets.save(ticket);
            log.debug("✅ Ticket mis à jour: CRM ID {}", ticket.getIdExterneCrm());
            
            if (ancienStatutId != null && !ancienStatutId.equals(nouveauStatutId)) {
                envoyerNotificationsChangementStatut(ticket, ancienStatutId, nouveauStatutId);
            }
        }
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
                
                Optional<Utilisateur> utilisateurNormalOpt = users.findByIdExterneCrm(idExterneCrm);
                if (utilisateurNormalOpt.isPresent()) {
                    Utilisateur utilisateur = utilisateurNormalOpt.get();
                    log.debug("Utilisateur normal trouvé (mais pas interne): ID={}, Nom={} {}", 
                             utilisateur.getId(), utilisateur.getPrenom(), utilisateur.getNom());
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
            ticketService.envoyerNotificationsChangementStatut(t, ancienStatutId, nouveauStatutId);
            log.info("📢 Notification envoyée pour le changement de statut du ticket {} : {} -> {}", 
                    t.getReference(), ancienStatutId, nouveauStatutId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi des notifications pour le ticket {} : {}", 
                     t.getReference(), e.getMessage());
        }
    }

    private String getSqlRequete() {
        return "SELECT Case_CaseId, Case_Description, Case_PrimaryPersonId, Case_ProblemNote, Case_Priority, Case_Status, Case_Stage, Case_CreatedBy, " +
               "       Case_PARCId, Case_PrimaryCompanyId, Case_CreatedDate, Case_UpdatedDate, Case_ReferenceId, Case_Opened, Case_Closed, Case_CustomerRef, " +
               "       Case_AssignedUserId, " + 
               "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
               "FROM dbo.Cases WHERE ISNULL(Case_Deleted,0) = 0 " +
               "AND Case_CreatedDate >= '2024-01-01'";
    }
}