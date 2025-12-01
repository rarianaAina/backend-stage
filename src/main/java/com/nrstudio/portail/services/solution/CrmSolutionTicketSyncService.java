package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.depots.solution.SolutionTicketRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.domaine.solution.SolutionTicket;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.services.TicketService;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmSolutionTicketSyncService {

    private final JdbcTemplate crmJdbc;
    private final SolutionTicketRepository solutionTicketRepository;
    private final SolutionRepository solutionRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final SynchronisationManager synchronisationManager;

    // Variable pour suivre le mode de synchronisation
    private boolean synchronisationManuelleEnCours = false;

    public CrmSolutionTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                       SolutionTicketRepository solutionTicketRepository,
                                       SolutionRepository solutionRepository,
                                       TicketRepository ticketRepository,
                                       TicketService ticketService,
                                       SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.solutionTicketRepository = solutionTicketRepository;
        this.solutionRepository = solutionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.synchronisationManager = synchronisationManager;
    }

    // Synchronisation planifiée - non interruptible
    @Transactional
    public void synchroniserLiaisonsSolutionsTicketsDynamique() {
        System.out.println("🚀 Synchronisation via CRON dynamique (DB)");
        synchronisationManuelleEnCours = false;
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle - interruptible
    @Transactional
    public void synchroniserLiaisonsSolutionsTicketsManuellement() {
        System.out.println("🚀 Début de la synchronisation manuelle des liaisons solutions-tickets");
        synchronisationManuelleEnCours = true; // Mode manuel
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        final String sql =
            "SELECT SLnk_Soln_SolutionId, SLnk_Case_CaseId " +
            "FROM dbo.vSolutionCaseLinkReport " +
            "WHERE SLnk_Soln_SolutionId IS NOT NULL AND SLnk_Case_CaseId IS NOT NULL";

        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        int nouvellesLiaisons = 0;
        int erreurs = 0;

        for (Map<String, Object> r : rows) {
            try {
                if (traiterLiaison(r)) {
                    nouvellesLiaisons++;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la création de la liaison: " + e.getMessage());
                erreurs++;
            }
        }
        
        if (nouvellesLiaisons > 0 || erreurs > 0) {
            System.out.println("✅ Synchronisation planifiée terminée - " + nouvellesLiaisons + " nouvelle(s) liaison(s), " + erreurs + " erreur(s)");
        }
    }

    private void executerSynchronisationManuelle() {
        final String typeSync = "liaisons-solutions-tickets";
        
        // Vérifier si une synchronisation est déjà en cours
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des liaisons solutions-tickets est déjà en cours");
        }

        // Démarrer la synchronisation
        synchronisationManager.demarrerSynchronisation(typeSync);
        
        // Exécuter dans un thread séparé pour permettre l'interruption
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                
                final String sql =
                    "SELECT SLnk_Soln_SolutionId, SLnk_Case_CaseId " +
                    "FROM dbo.vSolutionCaseLinkReport " +
                    "WHERE SLnk_Soln_SolutionId IS NOT NULL AND SLnk_Case_CaseId IS NOT NULL";

                List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
                int nouvellesLiaisons = 0;
                int erreurs = 0;

                for (Map<String, Object> r : rows) {
                    // Vérifier si l'arrêt a été demandé
                    if (synchronisationManager.doitArreter(typeSync)) {
                        System.out.println("🛑 Synchronisation manuelle des liaisons arrêtée à la demande");
                        return;
                    }

                    try {
                        if (traiterLiaison(r)) {
                            nouvellesLiaisons++;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors de la création de la liaison: " + e.getMessage());
                        erreurs++;
                    }
                    
                    // Petit délai pour permettre une interruption plus réactive
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println("🛑 Synchronisation manuelle interrompue");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                if (nouvellesLiaisons > 0 || erreurs > 0) {
                    System.out.println("✅ Synchronisation manuelle terminée - " + nouvellesLiaisons + " nouvelle(s) liaison(s), " + erreurs + " erreur(s)");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la synchronisation manuelle des liaisons: " + e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
                synchronisationManuelleEnCours = false; // Réinitialiser le flag
            }
        });
        
        syncThread.start();
    }

    private boolean traiterLiaison(Map<String, Object> r) {
        Integer solutionIdCrm = toInt(r.get("SLnk_Soln_SolutionId"));
        Integer caseIdCrm = toInt(r.get("SLnk_Case_CaseId"));
        
        if (solutionIdCrm == null || caseIdCrm == null) return false;

        // 1. Trouver la solution dans notre base via son id_externe_crm
        String solutionIdExterne = solutionIdCrm.toString();
        Solution solution = solutionRepository.findByIdExterneCrm(solutionIdExterne)
            .orElse(null);
        
        if (solution == null) {
            System.out.println("Solution non trouvée avec id_externe_crm: " + solutionIdExterne);
            return false;
        }

        // 2. Trouver le ticket dans notre base via son id_externe_crm
        Integer ticketIdExterne = caseIdCrm;
        Ticket ticket = ticketRepository.findByIdExterneCrm(ticketIdExterne)
            .orElse(null);
        
        if (ticket == null) {
            System.out.println("Ticket non trouvé avec id_externe_crm: " + ticketIdExterne);
            return false;
        }

        // 3. Vérifier si la liaison existe déjà
        if (!solutionTicketRepository.existsBySolutionIdAndTicketId(solution.getId(), ticket.getId())) {
            // 4. Créer la liaison
            SolutionTicket solutionTicket = new SolutionTicket(solution, ticket);
            solutionTicketRepository.save(solutionTicket);
            
            System.out.println("✅ Liaison créée - Solution: " + solution.getId() + " (" + solution.getTitre() + "), Ticket: " + ticket.getId() + " (" + ticket.getReference() + ")");
            
            // 5. Envoyer la notification au client UNIQUEMENT en mode planifié
            if (!synchronisationManuelleEnCours) {
                envoyerNotificationAjoutSolution(ticket, solution);
            } else {
                System.out.println("🔕 Notification non envoyée (synchronisation manuelle)");
            }
            
            return true;
        }
        
        return false;
    }

    /**
     * Envoie une notification au client lorsqu'une solution est ajoutée à son ticket
     */
    private void envoyerNotificationAjoutSolution(Ticket ticket, Solution solution) {
        try {
            System.out.println("📢 Envoi de notification pour l'ajout de solution au ticket " + ticket.getReference());
            
            // Appel du service de notification existant
            ticketService.envoyerNotificationsAjoutSolution(ticket);
            
            System.out.println("✅ Notification envoyée avec succès pour le ticket " + ticket.getReference());
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification: " + e.getMessage());
            // Ne pas propager l'exception pour ne pas bloquer la synchronisation
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}