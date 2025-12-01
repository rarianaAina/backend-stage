package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.config.SchedulingConfig;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.CreditHoraireRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.CreditHoraire;
import com.nrstudio.portail.domaine.Produit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CrmCreditHoraireSyncService {

    private static final Logger logger = LoggerFactory.getLogger(CrmCreditHoraireSyncService.class);
  
    private final JdbcTemplate crmJdbc;
    private final CreditHoraireRepository creditHoraireRepository;
    private final CompanyRepository companyRepository;
    private final ProduitRepository produitRepository;
    private final SchedulingConfig schedulingConfig;
    private final SynchronisationManager synchronisationManager;

    public CrmCreditHoraireSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                 CreditHoraireRepository creditHoraireRepository,
                                 CompanyRepository companyRepository,
                                 ProduitRepository produitRepository,
                                 SchedulingConfig schedulingConfig,
                                 SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.creditHoraireRepository = creditHoraireRepository;
        this.companyRepository = companyRepository;
        this.produitRepository = produitRepository;
        this.schedulingConfig = schedulingConfig;
        this.synchronisationManager = synchronisationManager;
    }

    // Synchronisation planifiée - non interruptible
    //@Scheduled(cron = "${scheduling.crm-ch-sync-cron:0 * * * * *}")
    @Transactional
    public void synchroniserCreditHoraire() {
        logger.info("🚀 Début de la synchronisation planifiée des crédits horaires - {}", LocalDateTime.now());
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle - interruptible
    @Transactional
    public void synchroniserCreditHoraireManuellement() {
        logger.info("🚀 Début de la synchronisation manuelle des crédits horaires - {}", LocalDateTime.now());
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        final String sql =
            "SELECT hc_CreatedDate, hc_Deleted, hc_companyid, hc_start_date, hc_end_date, " +
            "       hc_creditbeneficie, hc_creditconsomme, hc_creditrestant, hc_parc, hc_estcloture " +
            "FROM dbo.vCH " +
            "WHERE (hc_estcloture IS NULL)";

        try {
            List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
            logger.info("📊 {} enregistrements trouvés dans le CRM", rows.size());

            int compteurSuccess = 0;
            int compteurErreurs = 0;

            for (Map<String,Object> r : rows) {
                try {
                    traiterEnregistrementCredit(r);
                    compteurSuccess++;
                } catch (Exception e) {
                    logger.error("❌ Erreur lors du traitement pour companyId: {}", r.get("hc_companyid"), e);
                    compteurErreurs++;
                }
            }

            logger.info("✅ Synchronisation planifiée TERMINÉE - {} succès, {} erreurs - {}", 
                       compteurSuccess, compteurErreurs, LocalDateTime.now());

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la synchronisation planifiée des crédits horaires", e);
        }
    }

    private void executerSynchronisationManuelle() {
        final String typeSync = "credits-horaires";
        
        // Vérifier si une synchronisation est déjà en cours
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des crédits horaires est déjà en cours");
        }

        // Démarrer la synchronisation
        synchronisationManager.demarrerSynchronisation(typeSync);
        
        // Exécuter dans un thread séparé pour permettre l'interruption
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                
                final String sql =
                    "SELECT hc_CreatedDate, hc_Deleted, hc_companyid, hc_start_date, hc_end_date, " +
                    "       hc_creditbeneficie, hc_creditconsomme, hc_creditrestant, hc_parc, hc_estcloture " +
                    "FROM dbo.vCH " +
                    "WHERE (hc_estcloture IS NULL)";

                List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
                logger.info("📊 {} enregistrements trouvés dans le CRM", rows.size());

                int compteurSuccess = 0;
                int compteurErreurs = 0;

                for (Map<String,Object> r : rows) {
                    // Vérifier si l'arrêt a été demandé
                    if (synchronisationManager.doitArreter(typeSync)) {
                        logger.info("🛑 Synchronisation manuelle des crédits horaires arrêtée à la demande");
                        return;
                    }

                    try {
                        traiterEnregistrementCredit(r);
                        compteurSuccess++;
                    } catch (Exception e) {
                        logger.error("❌ Erreur lors du traitement pour companyId: {}", r.get("hc_companyid"), e);
                        compteurErreurs++;
                    }
                    
                    // Petit délai pour permettre une interruption plus réactive
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.info("🛑 Synchronisation manuelle interrompue");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                logger.info("✅ Synchronisation manuelle TERMINÉE - {} succès, {} erreurs - {}", 
                           compteurSuccess, compteurErreurs, LocalDateTime.now());
                
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la synchronisation manuelle des crédits horaires", e);
            } finally {
                synchronisationManager.supprimerThread(typeSync);
            }
        });
        
        syncThread.start();
    }

    private void traiterEnregistrementCredit(Map<String, Object> r) {
        Integer companyIdCrm = toInt(r.get("hc_companyid"));
        if (companyIdCrm == null) {
            logger.warn("Company ID null ignoré");
            throw new IllegalArgumentException("Company ID null");
        }

        // Trouver la company
        String idExterneCrm = String.valueOf(companyIdCrm);
        Company company = companyRepository.findByIdExterneCrm(idExterneCrm).orElse(null);
        if (company == null) {
            logger.warn("❌ Company non trouvée pour hc_companyid: {} (idExterneCrm: {})", companyIdCrm, idExterneCrm);
            throw new IllegalArgumentException("Company non trouvée");
        }

        // Trouver le produit
        Integer produitIdCrm = toInt(r.get("hc_parc"));
        Produit produit = null;
        if (produitIdCrm != null) {
            produit = produitRepository.findByIdExterneCrm(String.valueOf(produitIdCrm)).orElse(null);
            logger.debug("Produit CRM {} trouvé: {}", produitIdCrm, (produit != null));
        }

        // Convertir les dates
        LocalDate startDate = convertToLocalDate(r.get("hc_start_date"));
        LocalDate endDate = convertToLocalDate(r.get("hc_end_date"));
        LocalDateTime createdDate = convertToLocalDateTime(r.get("hc_CreatedDate"));

        if (startDate == null) {
            logger.warn("Date de début manquante pour company: {}", companyIdCrm);
            throw new IllegalArgumentException("Date de début manquante");
        }

        if (endDate == null) {
            endDate = startDate.plusYears(1);
        }

        // Vérifier si le crédit existe déjà
        CreditHoraire creditExistant = trouverCreditExistant(company, produit, startDate, endDate);

        Integer heuresAllouees = toInt(r.get("hc_creditbeneficie"));
        Integer heuresConsommees = toInt(r.get("hc_creditconsomme"));
        Integer heuresRestantesCrm = toInt(r.get("hc_creditrestant"));

        if (heuresAllouees == null) {
            logger.warn("Heures allouées manquantes pour company: {}", companyIdCrm);
            throw new IllegalArgumentException("Heures allouées manquantes");
        }

        // Validation et calcul des heures
        if (heuresConsommees == null) heuresConsommees = 0;
        
        // Calcul cohérent des heures restantes
        int heuresRestantesCalculees = Math.max(0, heuresAllouees - heuresConsommees);
        
        // Utiliser la valeur du CRM si elle est cohérente, sinon utiliser le calcul
        Integer heuresRestantes = heuresRestantesCrm;
        if (heuresRestantes == null || heuresRestantes < 0) {
            heuresRestantes = heuresRestantesCalculees;
        }

        if (creditExistant != null) {
            // Mettre à jour le crédit existant
            mettreAJourCreditExistant(creditExistant, heuresAllouees, heuresConsommees, heuresRestantes, produit);
            logger.debug("✅ Crédit horaire MIS À JOUR pour company: {} (ID crédit: {})", companyIdCrm, creditExistant.getId());
        } else {
            // Créer un nouveau crédit
            CreditHoraire nouveauCredit = creerNouveauCredit(company, produit, startDate, endDate, heuresAllouees, heuresConsommees, heuresRestantes, createdDate);
            logger.debug("✅ NOUVEAU crédit horaire CRÉÉ pour company: {} (ID crédit: {})", companyIdCrm, nouveauCredit.getId());
        }
    }

    private CreditHoraire trouverCreditExistant(Company company, Produit produit, LocalDate startDate, LocalDate endDate) {
        try {
            if (produit != null) {
                return creditHoraireRepository
                    .findByCompanyAndProduitAndPeriodeDebutAndPeriodeFin(company, produit, startDate, endDate)
                    .orElse(null);
            } else {
                return creditHoraireRepository
                    .findByCompanyAndPeriodeDebutAndPeriodeFin(company, startDate, endDate)
                    .orElse(null);
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la recherche du crédit existant", e);
            return null;
        }
    }

    private void mettreAJourCreditExistant(CreditHoraire credit, Integer heuresAllouees, Integer heuresConsommees, 
                                         Integer heuresRestantes, Produit produit) {
        try {
            credit.setHeuresAllouees(heuresAllouees);
            credit.setHeuresConsommees(heuresConsommees);
            credit.setHeuresRestantes(heuresRestantes);
            credit.setProduit(produit);
            credit.setDateMiseAJour(LocalDateTime.now());
            creditHoraireRepository.save(credit);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du crédit ID: {}", credit.getId(), e);
            throw e;
        }
    }

    private CreditHoraire creerNouveauCredit(Company company, Produit produit, LocalDate startDate, LocalDate endDate,
                                  Integer heuresAllouees, Integer heuresConsommees, Integer heuresRestantes, 
                                  LocalDateTime createdDate) {
        try {
            CreditHoraire nouveauCredit = new CreditHoraire();
            nouveauCredit.setCompany(company);
            nouveauCredit.setProduit(produit);
            nouveauCredit.setPeriodeDebut(startDate);
            nouveauCredit.setPeriodeFin(endDate);
            nouveauCredit.setHeuresAllouees(heuresAllouees);
            nouveauCredit.setHeuresConsommees(heuresConsommees);
            nouveauCredit.setHeuresRestantes(heuresRestantes);
            nouveauCredit.setActif(true);
            
            if (createdDate != null) {
                nouveauCredit.setDateCreation(createdDate);
                nouveauCredit.setDateMiseAJour(createdDate);
            }
            
            return creditHoraireRepository.save(nouveauCredit);
        } catch (Exception e) {
            logger.error("Erreur lors de la création d'un nouveau crédit", e);
            throw e;
        }
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
    }

    private LocalDate convertToLocalDate(Object dateObject) {
        if (dateObject == null) return null;
        try {
            if (dateObject instanceof java.sql.Date) {
                return ((java.sql.Date) dateObject).toLocalDate();
            } else if (dateObject instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateObject).toLocalDateTime().toLocalDate();
            }
        } catch (Exception e) {
            logger.warn("Erreur conversion date: {}", dateObject);
        }
        return null;
    }

    private LocalDateTime convertToLocalDateTime(Object dateObject) {
        if (dateObject == null) return null;
        try {
            if (dateObject instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateObject).toLocalDateTime();
            } else if (dateObject instanceof java.sql.Date) {
                return ((java.sql.Date) dateObject).toLocalDate().atStartOfDay();
            }
        } catch (Exception e) {
            logger.warn("Erreur conversion datetime: {}", dateObject);
        }
        return null;
    }
}