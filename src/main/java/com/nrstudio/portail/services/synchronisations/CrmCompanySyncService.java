package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.config.SchedulingConfig;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class CrmCompanySyncService {

    @Autowired
    @Qualifier("crmJdbc")
    private JdbcTemplate crmJdbc;

    @Autowired
    private CompanyRepository companies;

    @Autowired
    private  SynchronisationManager synchronisationManager;

    private static final Logger logger = LoggerFactory.getLogger(CrmCompanySyncService.class);
    
    private final AtomicBoolean syncPlanifieeEnCours = new AtomicBoolean(false);

    // Exécution planifiée - version sécurisée
    @Transactional
    public void synchroniserCompanies() {
        // Éviter les chevauchements avec la sync manuelle
        if (synchronisationManager.estEnCours("companies")) {
            logger.info("Sync planifiée ignorée - Sync manuelle en cours");
            return;
        }
        
        if (syncPlanifieeEnCours.get()) {
            logger.info("Sync planifiée ignorée - Déjà en cours");
            return;
        }
        
        syncPlanifieeEnCours.set(true);
        try {
            logger.info("Début de la synchronisation planifiée des companies");
            executerSynchronisationPlanifiee();
        } finally {
            syncPlanifieeEnCours.set(false);
        }
    }

    // Exécution manuelle - version sécurisée
    @Transactional
    public void synchroniserCompaniesManuellement() {
        // Éviter les chevauchements
        if (synchronisationManager.estEnCours("companies")) {
            throw new IllegalStateException("Une synchronisation manuelle est déjà en cours");
        }
        
        if (syncPlanifieeEnCours.get()) {
            throw new IllegalStateException("Une synchronisation planifiée est en cours");
        }
        
        logger.info("Début de la synchronisation manuelle des companies");
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        // Logique originale rapide et fiable
        final String sql = "SELECT Comp_CompanyId, Comp_Name, Comp_Type, " +
                          "       ISNULL(Comp_Deleted,0) AS Comp_Deleted " +
                          "FROM dbo.Company ";

        List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
        int compteur = 0;

        for (Map<String,Object> r : rows) {
            Integer companyId = toInt(r.get("Comp_CompanyId"));
            if (companyId == null) continue;
            if (toInt(r.get("Comp_Deleted")) == 1) continue;

            traiterCompany(r, companyId);
            compteur++;
        }
        
        logger.info("Synchronisation planifiée terminée. {} companies traitées", compteur);
    }

    private void executerSynchronisationManuelle() {
        final String typeSync = "companies";
        synchronisationManager.demarrerSynchronisation(typeSync);
        
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                
                final String sql = "SELECT Comp_CompanyId, Comp_Name, Comp_Type, " +
                                  "       ISNULL(Comp_Deleted,0) AS Comp_Deleted " +
                                  "FROM dbo.Company ";

                List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
                int compteur = 0;

                for (Map<String,Object> r : rows) {
                    // Vérifier l'arrêt demandé
                    if (synchronisationManager.doitArreter(typeSync)) {
                        logger.info("Synchronisation manuelle arrêtée à la demande");
                        return;
                    }

                    Integer companyId = toInt(r.get("Comp_CompanyId"));
                    if (companyId == null) continue;
                    if (toInt(r.get("Comp_Deleted")) == 1) continue;

                    traiterCompany(r, companyId);
                    compteur++;
                    
                    // Permettre une interruption plus réactive
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.info("Synchronisation manuelle interrompue");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                logger.info("Synchronisation manuelle terminée. {} companies traitées", compteur);
                
            } finally {
                synchronisationManager.supprimerThread(typeSync);
            }
        });
        
        syncThread.start();
    }

    private void traiterCompany(Map<String, Object> r, Integer companyId) {
        String idExterneCrm = String.valueOf(companyId);
        Company companyExistante = companies.findByIdExterneCrm(idExterneCrm).orElse(null);

        String nom = Objects.toString(r.get("Comp_Name"), "Société " + companyId);
        logger.debug("Company CRM {} : {}", companyId, nom);

        if (companyExistante != null) {
            companyExistante.setNom(nom);
            companyExistante.setDateMiseAJour(LocalDateTime.now());
            companies.save(companyExistante);
        } else {
            Company nouvelleCompany = new Company();
            nouvelleCompany.setIdExterneCrm(idExterneCrm);
            nouvelleCompany.setCodeCompany("COMP-" + companyId);
            nouvelleCompany.setNom(nom);
            nouvelleCompany.setActif(true);
            nouvelleCompany.setDateCreation(LocalDateTime.now());
            nouvelleCompany.setDateMiseAJour(LocalDateTime.now());
            companies.save(nouvelleCompany);
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
    }
}