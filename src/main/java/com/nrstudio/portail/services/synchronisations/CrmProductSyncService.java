package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.depots.CompanyPARCRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.domaine.CompanyPARC;
import com.nrstudio.portail.domaine.Produit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CrmProductSyncService {

  private final JdbcTemplate crmJdbc;
  private final ProduitRepository produits;
  private final CompanyPARCRepository companyPARCRepository;
  private final SynchronisationManager synchronisationManager;
    
  private static final Logger log = LoggerFactory.getLogger(CrmProductSyncService.class);

  public CrmProductSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                               ProduitRepository produits,
                               CompanyPARCRepository companyPARCRepository,
                               SynchronisationManager synchronisationManager) {
    this.crmJdbc = crmJdbc;
    this.produits = produits;
    this.companyPARCRepository = companyPARCRepository;
    this.synchronisationManager = synchronisationManager;
  }

  // Synchronisation planifiée - non interruptible
  //@Scheduled(cron = "${scheduling.crm-product-sync-cron:0 * * * * *}")
  @Transactional
  public void synchroniserProduits() {
    log.info("🚀 Début de la synchronisation planifiée des produits");
    synchroniserProduitsParcPlanifiee();
    synchroniserCompanyPARCPlanifiee();
    log.info("✅ Synchronisation planifiée des produits terminée");
  }

  // Synchronisation manuelle - interruptible
  @Transactional
  public void synchroniserProduitsManuellement() {
    log.info("🚀 Début de la synchronisation manuelle des produits");
    executerSynchronisationManuelle();
  }

  private void synchroniserProduitsParcPlanifiee() {
    final String sql =
      "SELECT parc_PARCid, parc_name, parc_detail, parc_Produit, parc_CreatedDate, " +
      "       ISNULL(parc_Deleted, 0) AS parc_Deleted, parc_dateMaj " +
      "FROM dbo.PARC " +
      "WHERE ISNULL(parc_Deleted, 0) = 0 ";

    List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
    log.info("{} enregistrements récupérés depuis le CRM", rows.size());
    
    for (Map<String, Object> r : rows) {
      try {
        traiterProduitParc(r);
      } catch (Exception e) {
        log.error("❌ Erreur lors du traitement du produit: {}", e.getMessage());
      }
    }
  }

  private void synchroniserCompanyPARCPlanifiee() {
    final String sql = 
        "SELECT parc_PARCid, parc_name, parc_companyid, parc_CreatedDate, Comp_CompanyId, Comp_Name " +
        "FROM dbo.vCompanyPARC";

    List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
    log.info("{} enregistrements récupérés depuis le CRM pour company parc", rows.size());
    
    for (Map<String, Object> r : rows) {
      try {
        traiterCompanyParc(r);
      } catch (Exception e) {
        log.error("❌ Erreur lors du traitement du company parc: {}", e.getMessage());
      }
    }
  }

  private void executerSynchronisationManuelle() {
    final String typeSync = "produits";
    
    // Vérifier si une synchronisation est déjà en cours
    if (synchronisationManager.estEnCours(typeSync)) {
      throw new IllegalStateException("Une synchronisation des produits est déjà en cours");
    }

    // Démarrer la synchronisation
    synchronisationManager.demarrerSynchronisation(typeSync);
    
    // Exécuter dans un thread séparé pour permettre l'interruption
    Thread syncThread = new Thread(() -> {
      try {
        synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
        
        // Synchroniser les produits PARC
        synchroniserProduitsParcManuellement();
        
        // Vérifier l'arrêt avant de continuer
        if (synchronisationManager.doitArreter(typeSync)) {
          log.info("🛑 Synchronisation manuelle des produits arrêtée à la demande après produits PARC");
          return;
        }
        
        // Synchroniser les company PARC
        synchroniserCompanyPARCManuellement();
        
        log.info("✅ Synchronisation manuelle des produits terminée");
        
      } catch (Exception e) {
        log.error("❌ Erreur lors de la synchronisation manuelle des produits: {}", e.getMessage());
      } finally {
        synchronisationManager.supprimerThread(typeSync);
      }
    });
    
    syncThread.start();
  }

  private void synchroniserProduitsParcManuellement() {
    final String sql =
      "SELECT parc_PARCid, parc_name, parc_detail, parc_Produit, parc_CreatedDate, " +
      "       ISNULL(parc_Deleted, 0) AS parc_Deleted, parc_dateMaj " +
      "FROM dbo.PARC " +
      "WHERE ISNULL(parc_Deleted, 0) = 0 ";

    List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
    log.info("{} enregistrements récupérés depuis le CRM pour produits PARC", rows.size());
    
    for (Map<String, Object> r : rows) {
      // Vérifier si l'arrêt a été demandé
      if (synchronisationManager.doitArreter("produits")) {
        log.info("🛑 Synchronisation produits PARC arrêtée à la demande");
        return;
      }

      try {
        traiterProduitParc(r);
      } catch (Exception e) {
        log.error("❌ Erreur lors du traitement du produit: {}", e.getMessage());
      }
      
      // Petit délai pour permettre une interruption plus réactive
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        log.info("🛑 Synchronisation produits PARC interrompue");
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private void synchroniserCompanyPARCManuellement() {
    final String sql = 
        "SELECT parc_PARCid, parc_name,parc_UserId, parc_companyid, parc_CreatedDate, Comp_CompanyId, Comp_Name " +
        "FROM dbo.vCompanyPARC";

    List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
    log.info("{} enregistrements récupérés depuis le CRM pour company PARC", rows.size());
    
    for (Map<String, Object> r : rows) {
      // Vérifier si l'arrêt a été demandé
      if (synchronisationManager.doitArreter("produits")) {
        log.info("🛑 Synchronisation company PARC arrêtée à la demande");
        return;
      }

      try {
        traiterCompanyParc(r);
      } catch (Exception e) {
        log.error("❌ Erreur lors du traitement du company parc: {}", e.getMessage());
      }
      
      // Petit délai pour permettre une interruption plus réactive
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        log.info("🛑 Synchronisation company PARC interrompue");
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private void traiterProduitParc(Map<String, Object> r) {
    Integer parcId = toInt(r.get("parc_PARCid"));
    
    if (parcId == null) return;
    if (toInt(r.get("parc_Deleted")) == 1) return;

    String idExterneCrm = String.valueOf(parcId);
    Produit produitExistant = produits.findByIdExterneCrm(idExterneCrm).orElse(null);

    String libelle = Objects.toString(r.get("parc_name"), "Produit " + parcId);
    String description = Objects.toString(r.get("parc_detail"), null);
    String codeProduit = Objects.toString(r.get("parc_Produit"), null);
    LocalDateTime createdDate = r.get("parc_CreatedDate") != null ?
        ((java.sql.Timestamp) r.get("parc_CreatedDate")).toLocalDateTime() : LocalDateTime.now();

    LocalDateTime dateMaj = r.get("parc_dateMaj") != null ?
        ((java.sql.Timestamp) r.get("parc_dateMaj")).toLocalDateTime() : LocalDateTime.now();

    if (produitExistant != null) {
      produitExistant.setLibelle(codeProduit);
      produitExistant.setDescription(description);
      produitExistant.setCodeProduit(libelle);
      produitExistant.setDateMiseAJour(dateMaj);
      produits.save(produitExistant);
    } else {
      Produit nouveauProduit = new Produit();
      nouveauProduit.setLibelle(codeProduit);
      nouveauProduit.setDescription(description);
      nouveauProduit.setCodeProduit(libelle);
      nouveauProduit.setActif(true);
      nouveauProduit.setDateCreation(createdDate);
      nouveauProduit.setDateMiseAJour(dateMaj);
      nouveauProduit.setIdExterneCrm(idExterneCrm);
      produits.save(nouveauProduit);
    }
  }

  private void traiterCompanyParc(Map<String, Object> r) {
    Integer parcId = toInt(r.get("parc_PARCid"));
    Integer userId = toInt(r.get("parc_UserId"));
    if (parcId == null) return;

    CompanyPARC companyPARCExistant = companyPARCRepository.findById(parcId).orElse(null);

    String parcName = Objects.toString(r.get("parc_name"), "Nom de parc " + parcId);
    Integer parcCompanyId = toInt(r.get("parc_companyid"));
    Integer compCompanyId = toInt(r.get("Comp_CompanyId"));
    String compName = Objects.toString(r.get("Comp_Name"), null);
    LocalDateTime createdDate = r.get("parc_CreatedDate") != null ?
        ((java.sql.Timestamp) r.get("parc_CreatedDate")).toLocalDateTime() : LocalDateTime.now();

    if (companyPARCExistant != null) {
      companyPARCExistant.setParcName(parcName);
      companyPARCExistant.setParcCompanyId(parcCompanyId);
      companyPARCExistant.setCompCompanyId(compCompanyId);
      companyPARCExistant.setCompName(compName);
      companyPARCExistant.setDateObtention(createdDate);
      companyPARCExistant.setUserId(userId);
      companyPARCRepository.save(companyPARCExistant);
    } else {
      CompanyPARC nouveauCompanyPARC = new CompanyPARC();
      nouveauCompanyPARC.setParcId(parcId);
      nouveauCompanyPARC.setParcName(parcName);
      nouveauCompanyPARC.setParcCompanyId(parcCompanyId);
      nouveauCompanyPARC.setCompCompanyId(compCompanyId);
      nouveauCompanyPARC.setCompName(compName);
      nouveauCompanyPARC.setDateObtention(createdDate);
      companyPARCExistant.setUserId(userId);
      companyPARCRepository.save(nouveauCompanyPARC);
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