package com.nrstudio.portail.services.synchronisations.configurations.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nrstudio.portail.depots.company.CompanySyncRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.services.synchronisations.configurations.constants.SyncConstants;
import com.nrstudio.portail.services.synchronisations.configurations.fetchers.CrmDataFetcher;
import com.nrstudio.portail.services.synchronisations.configurations.manager.SyncExecutionManager;
import com.nrstudio.portail.services.synchronisations.configurations.mappers.CompanyDataMapper;

import java.util.List;
import java.util.Map;

@Component
public class CompanySyncProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(CompanySyncProcessor.class);
    
    private final CrmDataFetcher dataFetcher;
    private final CompanySyncRepository syncRepository;
    private final SyncExecutionManager executionManager;
    private final CompanyDataMapper dataMapper;
    private final String entite = SyncConstants.ENTITE_COMPANY;
    
    public CompanySyncProcessor(CrmDataFetcher dataFetcher,
                              CompanySyncRepository syncRepository,
                              SyncExecutionManager executionManager,
                              CompanyDataMapper dataMapper) {
        this.dataFetcher = dataFetcher;
        this.syncRepository = syncRepository;
        this.executionManager = executionManager;
        this.dataMapper = dataMapper;
    }
    
    public void executerSynchronisationPlanifiee() {
        List<Map<String, Object>> companiesCrm = dataFetcher.recupererCompaniesCrm();
        int compteur = 0;
        
        for (Map<String, Object> donneesCrm : companiesCrm) {
            if (dataMapper.estCompanyValide(donneesCrm)) {
                try {
                    traiterCompany(donneesCrm);
                    compteur++;
                    executionManager.incrementerTraites(entite);
                } catch (Exception e) {
                    logger.warn("Erreur lors du traitement de la company {}", donneesCrm.get("Comp_CompanyId"), e);
                    executionManager.incrementerErrones(entite);
                }
            }
        }
        
        logger.info("Synchronisation planifiée {} terminée. {} companies traitées", entite, compteur);
    }
    
    public void executerSynchronisationManuelle() {
        List<Map<String, Object>> companiesCrm = dataFetcher.recupererCompaniesCrm();
        int compteur = 0;
        
        for (Map<String, Object> donneesCrm : companiesCrm) {
            if (executionManager.doitArreter(entite)) {
                logger.info("Synchronisation manuelle {} arrêtée à la demande", entite);
                return;
            }
            
            if (dataMapper.estCompanyValide(donneesCrm)) {
                try {
                    traiterCompany(donneesCrm);
                    compteur++;
                    executionManager.incrementerTraites(entite);
                    executionManager.pauseCourte(entite);
                } catch (Exception e) {
                    logger.warn("Erreur lors du traitement de la company {}", donneesCrm.get("Comp_CompanyId"), e);
                    executionManager.incrementerErrones(entite);
                }
            }
        }
        
        logger.info("Synchronisation manuelle {} terminée. {} companies traitées", entite, compteur);
    }
    
    private void traiterCompany(Map<String, Object> donneesCrm) {
        Integer companyId = dataMapper.extraireCompanyId(donneesCrm);
        String idExterneCrm = String.valueOf(companyId);
        
        Company company = syncRepository.trouverOuCreerCompany(idExterneCrm);
        dataMapper.mettreAJourCompany(company, donneesCrm, companyId);
        syncRepository.sauvegarder(company);
        
        logger.debug("Company CRM {} : {}", companyId, company.getNom());
    }
}