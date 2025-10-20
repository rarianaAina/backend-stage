package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.domaine.Produit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmProductSyncService {

  private final JdbcTemplate crmJdbc;
  private final ProduitRepository produits;

  public CrmProductSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                               ProduitRepository produits) {
    this.crmJdbc = crmJdbc;
    this.produits = produits;
  }

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void synchroniserProduits() {
    final String sql =
      "SELECT Prod_ProductId, Prod_Name, Prod_Description, " +
      "       ISNULL(Prod_Deleted,0) AS Prod_Deleted " +
      "FROM dbo.Products WHERE ISNULL(Prod_Deleted,0) = 0";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer produitId = toInt(r.get("Prod_ProductId"));
      if (produitId == null) continue;
      if (toInt(r.get("Prod_Deleted")) == 1) continue;

      String idExterneCrm = String.valueOf(produitId);
      Produit produitExistant = produits.findByIdExterneCrm(idExterneCrm).orElse(null);

      String libelle = Objects.toString(r.get("Prod_Name"), "Produit " + produitId);
      String description = Objects.toString(r.get("Prod_Description"), null);
      String codeProduit = null;

      if (produitExistant != null) {
        produitExistant.setLibelle(libelle);
        produitExistant.setDescription(description);
        produitExistant.setCodeProduit(codeProduit);
        produitExistant.setDateMiseAJour(LocalDateTime.now());
        produits.save(produitExistant);
      } else {
        Produit nouveauProduit = new Produit();
        nouveauProduit.setLibelle(libelle);
        nouveauProduit.setDescription(description);
        nouveauProduit.setCodeProduit(codeProduit);
        nouveauProduit.setActif(true);
        nouveauProduit.setDateCreation(LocalDateTime.now());
        nouveauProduit.setDateMiseAJour(LocalDateTime.now());
        nouveauProduit.setIdExterneCrm(idExterneCrm);
        produits.save(nouveauProduit);
      }
    }
  }


  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
