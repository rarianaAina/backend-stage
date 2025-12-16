package com.nrstudio.portail.services.synchronisations.configurations.executor;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.depots.solution.SolutionTicketRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.domaine.solution.SolutionTicket;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.services.TicketService;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import com.nrstudio.portail.services.synchronisations.configurations.processors.SolutionTicketDataProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class SolutionTicketSyncExecutor {

    private final JdbcTemplate crmJdbc;
    private final SolutionTicketRepository solutionTicketRepository;
    private final SolutionRepository solutionRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final SolutionTicketDataProcessor dataProcessor;
    
    private boolean synchronisationManuelleEnCours = false;

    public SolutionTicketSyncExecutor(JdbcTemplate crmJdbc,
                                     SolutionTicketRepository solutionTicketRepository,
                                     SolutionRepository solutionRepository,
                                     TicketRepository ticketRepository,
                                     TicketService ticketService,
                                     SolutionTicketDataProcessor dataProcessor) {
        this.crmJdbc = crmJdbc;
        this.solutionTicketRepository = solutionTicketRepository;
        this.solutionRepository = solutionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.dataProcessor = dataProcessor;
    }

    public void setSynchronisationManuelleEnCours(boolean synchronisationManuelleEnCours) {
        this.synchronisationManuelleEnCours = synchronisationManuelleEnCours;
    }

    public void executerSynchronisationPlanifiee() {
        final String sql = getSqlRequete();
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

    public void executerSynchronisationManuelle(SynchronisationManager synchronisationManager) {
        final String typeSync = "liaisons-solutions-tickets";
        
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des liaisons solutions-tickets est déjà en cours");
        }

        synchronisationManager.demarrerSynchronisation(typeSync);
        
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                executerSynchronisationManuelleAvecInterruption(synchronisationManager, typeSync);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la synchronisation manuelle des liaisons: " + e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
                synchronisationManuelleEnCours = false;
            }
        });
        
        syncThread.start();
    }

    private void executerSynchronisationManuelleAvecInterruption(SynchronisationManager synchronisationManager, String typeSync) {
        final String sql = getSqlRequete();
        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        int nouvellesLiaisons = 0;
        int erreurs = 0;

        for (Map<String, Object> r : rows) {
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
    }

    private boolean traiterLiaison(Map<String, Object> r) {
        Integer solutionIdCrm = dataProcessor.toInt(r.get("SLnk_Soln_SolutionId"));
        Integer caseIdCrm = dataProcessor.toInt(r.get("SLnk_Case_CaseId"));
        
        if (solutionIdCrm == null || caseIdCrm == null) return false;

        String solutionIdExterne = solutionIdCrm.toString();
        Solution solution = solutionRepository.findByIdExterneCrm(solutionIdExterne)
            .orElse(null);
        
        if (solution == null) {
            System.out.println("Solution non trouvée avec id_externe_crm: " + solutionIdExterne);
            return false;
        }

        Integer ticketIdExterne = caseIdCrm;
        Ticket ticket = ticketRepository.findByIdExterneCrm(ticketIdExterne)
            .orElse(null);
        
        if (ticket == null) {
            System.out.println("Ticket non trouvé avec id_externe_crm: " + ticketIdExterne);
            return false;
        }

        if (!solutionTicketRepository.existsBySolutionIdAndTicketId(solution.getId(), ticket.getId())) {
            SolutionTicket solutionTicket = new SolutionTicket(solution, ticket);
            solutionTicketRepository.save(solutionTicket);
            
            System.out.println("✅ Liaison créée - Solution: " + solution.getId() + " (" + solution.getTitre() + "), Ticket: " + ticket.getId() + " (" + ticket.getReference() + ")");
            
            if (!synchronisationManuelleEnCours) {
                envoyerNotificationAjoutSolution(ticket, solution);
            } else {
                System.out.println("🔕 Notification non envoyée (synchronisation manuelle)");
            }
            
            return true;
        }
        
        return false;
    }

    private void envoyerNotificationAjoutSolution(Ticket ticket, Solution solution) {
        try {
            System.out.println("📢 Envoi de notification pour l'ajout de solution au ticket " + ticket.getReference());
            ticketService.envoyerNotificationsAjoutSolution(ticket);
            System.out.println("✅ Notification envoyée avec succès pour le ticket " + ticket.getReference());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification: " + e.getMessage());
        }
    }

    private String getSqlRequete() {
        return "SELECT SLnk_Soln_SolutionId, SLnk_Case_CaseId " +
               "FROM dbo.vSolutionCaseLinkReport " +
               "WHERE SLnk_Soln_SolutionId IS NOT NULL AND SLnk_Case_CaseId IS NOT NULL";
    }
}