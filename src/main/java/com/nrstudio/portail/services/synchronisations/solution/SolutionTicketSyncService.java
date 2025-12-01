// package com.nrstudio.portail.services.synchronisations.solution;

// import com.nrstudio.portail.dto.synchronisations.SyncConfig;
// import com.nrstudio.portail.dto.synchronisations.SyncStats;
// import com.nrstudio.portail.dto.synchronisations.Synchronisable;
// import com.nrstudio.portail.services.synchronisations.configurations.manager.SyncExecutionManager;
// import com.nrstudio.portail.services.synchronisations.configurations.processors.SolutionTicketSyncProcessor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// @Service
// public class SolutionTicketSyncService implements Synchronisable {
    
//     private static final Logger logger = LoggerFactory.getLogger(SolutionTicketSyncService.class);
    
//     private final SolutionTicketSyncProcessor syncProcessor;
//     private final SyncExecutionManager executionManager;
//     private final SyncConfig syncConfig;
//     private final String entite = "solution-tickets";
    
//     public SolutionTicketSyncService(SolutionTicketSyncProcessor syncProcessor,
//                                     SyncExecutionManager executionManager,
//                                     SyncConfig syncConfig) {
//         this.syncProcessor = syncProcessor;
//         this.executionManager = executionManager;
//         this.syncConfig = syncConfig;
//     }
    
//     @Override
//     public String getEntite() {
//         return entite;
//     }
    
//     @Scheduled(cron = "${scheduling.crm-solutick-sync-cron:0 * * * * *}")
//     @Transactional
//     @Override
//     public void synchroniserPlanifie() {
//         if (!syncConfig.isEnabled()) {
//             logger.debug("Synchronisation désactivée");
//             return;
//         }
        
//         if (!executionManager.peutDemarrerSyncPlanifiee(entite)) {
//             return;
//         }
        
//         try {
//             logger.info("Début synchronisation planifiée des {}", entite);
//             syncProcessor.executerSynchronisationPlanifiee();
//             logger.info("Synchronisation planifiée des {} terminée", entite);
//         } catch (Exception e) {
//             logger.error("Erreur sync planifiée des {}", entite, e);
//         } finally {
//             executionManager.terminerSyncPlanifiee(entite);
//         }
//     }
    
//     @Transactional
//     @Override
//     public void synchroniserManuellement() {
//         if (!syncConfig.isEnabled()) {
//             throw new IllegalStateException("Synchronisation désactivée");
//         }
        
//         executionManager.validerDemandeSyncManuelle(entite);
        
//         try {
//             logger.info("Début synchronisation manuelle des {}", entite);
//             executionManager.demarrerSynchronisationManuelle(entite);
//             syncProcessor.executerSynchronisationManuelle();
//             logger.info("Synchronisation manuelle des {} terminée", entite);
//         } catch (Exception e) {
//             logger.error("Erreur sync manuelle des {}", entite, e);
//             throw e;
//         } finally {
//             executionManager.terminerSynchronisationManuelle(entite);
//         }
//     }
    
//     @Override
//     public SyncStats getStats() {
//         return executionManager.getStats(entite);
//     }
// }