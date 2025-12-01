package com.nrstudio.portail.services.synchronisations.configurations.processors;

import com.nrstudio.portail.services.TicketService;
import com.nrstudio.portail.services.synchronisations.configurations.manager.SyncExecutionManager;
import com.nrstudio.portail.depots.solution.SolutionTicketSyncRepository;
import com.nrstudio.portail.services.synchronisations.configurations.fetchers.CrmDataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolutionTicketSyncProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SolutionTicketSyncProcessor.class);
    
    private final CrmDataFetcher dataFetcher;
    private final SolutionTicketSyncRepository syncRepository;
    private final SyncExecutionManager executionManager;
    private final TicketService ticketService;
    private final String entite = "solution-tickets";
    
    public SolutionTicketSyncProcessor(CrmDataFetcher dataFetcher,
                                     SolutionTicketSyncRepository syncRepository,
                                     SyncExecutionManager executionManager,
                                     TicketService ticketService) {
        this.dataFetcher = dataFetcher;
        this.syncRepository = syncRepository;
        this.executionManager = executionManager;
        this.ticketService = ticketService;
    }
    
    public void executerSynchronisationPlanifiee() {
        List<Map<String, Object>> liaisonsCrm = dataFetcher.recupererLiaisonsSolutionsTicketsCrm();
        int nouvellesLiaisons = 0;
        int erreurs = 0;
        
        for (Map<String, Object> donneesCrm : liaisonsCrm) {
            try {
                if (traiterLiaison(donneesCrm, false)) { // false = mode planifié
                    nouvellesLiaisons++;
                    executionManager.incrementerTraites(entite);
                }
            } catch (Exception e) {
                logger.warn("Erreur traitement liaison solution-ticket", e);
                erreurs++;
                executionManager.incrementerErrones(entite);
            }
        }
        
        if (nouvellesLiaisons > 0 || erreurs > 0) {
            logger.info("Sync planifiée {} terminée. {} nouvelles liaisons, {} erreurs", 
                       entite, nouvellesLiaisons, erreurs);
        }
    }
    
    public void executerSynchronisationManuelle() {
        List<Map<String, Object>> liaisonsCrm = dataFetcher.recupererLiaisonsSolutionsTicketsCrm();
        int nouvellesLiaisons = 0;
        int erreurs = 0;
        
        for (Map<String, Object> donneesCrm : liaisonsCrm) {
            if (executionManager.doitArreter(entite)) {
                logger.info("Sync manuelle {} arrêtée à la demande", entite);
                return;
            }
            
            try {
                if (traiterLiaison(donneesCrm, true)) { // true = mode manuel
                    nouvellesLiaisons++;
                    executionManager.incrementerTraites(entite);
                }
            } catch (Exception e) {
                logger.warn("Erreur traitement liaison solution-ticket", e);
                erreurs++;
                executionManager.incrementerErrones(entite);
            }
            
            executionManager.pauseCourte(entite);
        }
        
        if (nouvellesLiaisons > 0 || erreurs > 0) {
            logger.info("Sync manuelle {} terminée. {} nouvelles liaisons, {} erreurs", 
                       entite, nouvellesLiaisons, erreurs);
        }
    }
    
    private boolean traiterLiaison(Map<String, Object> donneesCrm, boolean modeManuel) {
        Integer solutionIdCrm = toInt(donneesCrm.get("SLnk_Soln_SolutionId"));
        Integer caseIdCrm = toInt(donneesCrm.get("SLnk_Case_CaseId"));
        
        if (solutionIdCrm == null || caseIdCrm == null) {
            return false;
        }
        
        // Créer la liaison via le repository
        boolean liaisonCreee = syncRepository.creerLiaisonSiAbsente(solutionIdCrm, caseIdCrm);
        
        if (liaisonCreee && !modeManuel) {
            // Envoyer notification uniquement en mode planifié
            envoyerNotification(solutionIdCrm, caseIdCrm);
        }
        
        return liaisonCreee;
    }
    
    private void envoyerNotification(Integer solutionIdCrm, Integer ticketIdCrm) {
        try {
            syncRepository.trouverTicketParIdExterne(ticketIdCrm.toString())
                .ifPresent(ticket -> {
                    ticketService.envoyerNotificationsAjoutSolution(ticket);
                    logger.debug("Notification envoyée pour le ticket {}", ticket.getReference());
                });
        } catch (Exception e) {
            logger.warn("Erreur envoi notification pour ticket {}", ticketIdCrm, e);
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