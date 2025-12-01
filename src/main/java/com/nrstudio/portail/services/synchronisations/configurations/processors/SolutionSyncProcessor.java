package com.nrstudio.portail.services.synchronisations.configurations.processors;

import com.nrstudio.portail.services.synchronisations.configurations.fetchers.CrmDataFetcher;
import com.nrstudio.portail.services.synchronisations.configurations.manager.SyncExecutionManager;
import com.nrstudio.portail.services.synchronisations.configurations.mappers.SolutionDataMapper;
import com.nrstudio.portail.depots.solution.SolutionSyncRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolutionSyncProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(SolutionSyncProcessor.class);
    
    private final CrmDataFetcher dataFetcher;
    private final SolutionSyncRepository syncRepository;
    private final SyncExecutionManager executionManager;
    private final SolutionDataMapper dataMapper;
    private final String entite = "solutions";
    
    public SolutionSyncProcessor(CrmDataFetcher dataFetcher,
                               SolutionSyncRepository syncRepository,
                               SyncExecutionManager executionManager,
                               SolutionDataMapper dataMapper) {
        this.dataFetcher = dataFetcher;
        this.syncRepository = syncRepository;
        this.executionManager = executionManager;
        this.dataMapper = dataMapper;
    }
    
    public void executerSynchronisationPlanifiee() {
        List<Map<String, Object>> solutionsCrm = dataFetcher.recupererSolutionsCrm();
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurSupprimes = 0;
        int compteurErreurs = 0;
        
        for (Map<String, Object> donneesCrm : solutionsCrm) {
            try {
                if (dataMapper.estSolutionValide(donneesCrm)) {
                    boolean traitee = traiterSolution(donneesCrm);
                    if (traitee) {
                        executionManager.incrementerTraites(entite);
                        if (dataMapper.estNouvelleSolution(donneesCrm)) {
                            compteurNouveaux++;
                        } else {
                            compteurMaj++;
                        }
                    }
                } else if (dataMapper.estSolutionSupprimee(donneesCrm)) {
                    syncRepository.marquerCommeSupprimee(donneesCrm);
                    compteurSupprimes++;
                    executionManager.incrementerTraites(entite);
                }
            } catch (Exception e) {
                logger.warn("Erreur traitement solution {}", donneesCrm.get("Soln_SolutionId"), e);
                compteurErreurs++;
                executionManager.incrementerErrones(entite);
            }
        }
        
        logger.info("Sync planifiée {} terminée. {} nouveaux, {} MAJ, {} supprimés, {} erreurs", 
                   entite, compteurNouveaux, compteurMaj, compteurSupprimes, compteurErreurs);
    }
    
    public void executerSynchronisationManuelle() {
        List<Map<String, Object>> solutionsCrm = dataFetcher.recupererSolutionsCrm();
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurSupprimes = 0;
        int compteurErreurs = 0;
        
        for (Map<String, Object> donneesCrm : solutionsCrm) {
            if (executionManager.doitArreter(entite)) {
                logger.info("Sync manuelle {} arrêtée à la demande", entite);
                return;
            }
            
            try {
                if (dataMapper.estSolutionValide(donneesCrm)) {
                    boolean traitee = traiterSolution(donneesCrm);
                    if (traitee) {
                        executionManager.incrementerTraites(entite);
                        if (dataMapper.estNouvelleSolution(donneesCrm)) {
                            compteurNouveaux++;
                        } else {
                            compteurMaj++;
                        }
                    }
                } else if (dataMapper.estSolutionSupprimee(donneesCrm)) {
                    syncRepository.marquerCommeSupprimee(donneesCrm);
                    compteurSupprimes++;
                    executionManager.incrementerTraites(entite);
                }
            } catch (Exception e) {
                logger.warn("Erreur traitement solution {}", donneesCrm.get("Soln_SolutionId"), e);
                compteurErreurs++;
                executionManager.incrementerErrones(entite);
            }
            
            executionManager.pauseCourte(entite);
        }
        
        logger.info("Sync manuelle {} terminée. {} nouveaux, {} MAJ, {} supprimés, {} erreurs", 
                   entite, compteurNouveaux, compteurMaj, compteurSupprimes, compteurErreurs);
    }
    
    private boolean traiterSolution(Map<String, Object> donneesCrm) {
        Integer solutionId = dataMapper.extraireSolutionId(donneesCrm);
        String idExterneCrm = String.valueOf(solutionId);
        
        var solution = syncRepository.trouverOuCreerSolution(idExterneCrm);
        boolean aChange = dataMapper.mettreAJourSolution(solution, donneesCrm);
        
        if (aChange) {
            syncRepository.sauvegarder(solution);
            logger.debug("Solution CRM {} : {}", solutionId, solution.getTitre());
            return true;
        }
        
        return false;
    }
}