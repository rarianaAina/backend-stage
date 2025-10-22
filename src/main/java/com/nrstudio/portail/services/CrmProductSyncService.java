// package com.nrstudio.portail.services;

// import com.nrstudio.portail.depots.CompanyPARCRepository;
// import com.nrstudio.portail.depots.ProduitRepository;
// import com.nrstudio.portail.domaine.CompanyPARC;
// import com.nrstudio.portail.domaine.Produit;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;

// @Service
// public class CrmProductSyncService {

//   private final JdbcTemplate crmJdbc;
//   private final ProduitRepository produits;
//   private final CompanyPARCRepository companyPARCRepository;

//     public CrmProductSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
//                                ProduitRepository produits,
//                                CompanyPARCRepository companyPARCRepository) {
//     this.crmJdbc = crmJdbc;
//     this.produits = produits;
//     this.companyPARCRepository = companyPARCRepository;
//   }

//   @Scheduled(cron = "0 * * * * *") // Exécution toutes les minutes
//   @Transactional
//     public void synchroniserProduits() {
//     synchroniserProduitsParc();
//     synchroniserCompanyPARC();
//   }

//   public void synchroniserProduitsParc() {
//     final String sql = 
//       "SELECT parc_PARCid, parc_name, parc_detail, parc_Produit, parc_CreatedDate, " +
//       "       ISNULL(parc_Deleted, 0) AS parc_Deleted, parc_dateMaj " +
//       "FROM dbo.PARC WHERE ISNULL(parc_Deleted, 0) = 0";

//     List<Map<String, Object>> rows = crmJdbc.queryForList(sql);

//     for (Map<String, Object> r : rows) {
//       Integer parcId = toInt(r.get("parc_PARCid"));
//       if (parcId == null) continue;
//       if (toInt(r.get("parc_Deleted")) == 1) continue;

//       String idExterneCrm = String.valueOf(parcId);
//       Produit produitExistant = produits.findByIdExterneCrm(idExterneCrm).orElse(null);

//       String libelle = Objects.toString(r.get("parc_name"), "Produit " + parcId);
//       String description = Objects.toString(r.get("parc_detail"), null);
//       String codeProduit = Objects.toString(r.get("parc_Produit"), null);  // Utilise parc_Produit comme code
//       LocalDateTime createdDate = r.get("parc_CreatedDate") != null ?
//           ((java.sql.Timestamp) r.get("parc_CreatedDate")).toLocalDateTime() : LocalDateTime.now();

//       LocalDateTime dateMaj = r.get("parc_dateMaj") != null ?
//           ((java.sql.Timestamp) r.get("parc_dateMaj")).toLocalDateTime() : LocalDateTime.now();
//       if (produitExistant != null) {
//         produitExistant.setLibelle(codeProduit);
//         produitExistant.setDescription(description);
//         produitExistant.setCodeProduit(libelle);
//         //produitExistant.setDateMiseAJour(LocalDateTime.now());
//         produitExistant.setDateMiseAJour(dateMaj);
//         produits.save(produitExistant);
//       } else {
//         Produit nouveauProduit = new Produit();
//         nouveauProduit.setLibelle(codeProduit);
//         nouveauProduit.setDescription(description);
//         nouveauProduit.setCodeProduit(libelle);
//         nouveauProduit.setActif(true);
//         nouveauProduit.setDateCreation(createdDate);
//         nouveauProduit.setDateMiseAJour(dateMaj);
//         nouveauProduit.setIdExterneCrm(idExterneCrm);
//         //nouveauProduit.setDateMaj(Objects.toString(r.get("parc_dateMaj"), null));
//         produits.save(nouveauProduit);
//       }
//     }
//   }

//   private void synchroniserCompanyPARC() {
//     final String sql = 
//       "SELECT parc_PARCid, parc_name, parc_companyid, Comp_CompanyId, Comp_Name " +
//       "FROM dbo.vCompanyPARC";

//     List<Map<String, Object>> rows = crmJdbc.queryForList(sql);

//     for (Map<String, Object> r : rows) {
//       Integer parcId = toInt(r.get("parc_PARCid"));
//       if (parcId == null) continue;

//       CompanyPARC companyPARCExistant = companyPARCRepository.findById(parcId).orElse(null);

//       String parcName = Objects.toString(r.get("parc_name"), "Nom de parc " + parcId);
//       Integer parcCompanyId = toInt(r.get("parc_companyid"));
//       Integer compCompanyId = toInt(r.get("Comp_CompanyId"));
//       String compName = Objects.toString(r.get("Comp_Name"), null);

//       if (companyPARCExistant != null) {
//         companyPARCExistant.setParcName(parcName);
//         companyPARCExistant.setParcCompanyId(parcCompanyId);
//         companyPARCExistant.setCompCompanyId(compCompanyId);
//         companyPARCExistant.setCompName(compName);
//         companyPARCRepository.save(companyPARCExistant);
//       } else {
//         CompanyPARC nouveauCompanyPARC = new CompanyPARC();
//         nouveauCompanyPARC.setParcId(parcId);
//         nouveauCompanyPARC.setParcName(parcName);
//         nouveauCompanyPARC.setParcCompanyId(parcCompanyId);
//         nouveauCompanyPARC.setCompCompanyId(compCompanyId);
//         nouveauCompanyPARC.setCompName(compName);
//         companyPARCRepository.save(nouveauCompanyPARC);
//       }
//     }
//   }
//   // Méthode pour convertir un objet en entier
//   private Integer toInt(Object o) {
//     if (o == null) return null;
//     if (o instanceof Number) return ((Number) o).intValue();
//     try {
//       return Integer.valueOf(o.toString());
//     } catch (Exception e) {
//       return null;
//     }
//   }
// }
