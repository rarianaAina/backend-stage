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

  @Scheduled(cron = "0 30 2 * * *")
  @Transactional
  public void synchroniserProduits() {
    final String sql =
      "SELECT Prod_ProductId, Prod_Name, Prod_ProductFamilyId, " +
      "       Prod_PRDescription, Prod_Code, ISNULL(Prod_Deleted,0) AS Prod_Deleted " +
      "FROM dbo.NewProduct";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer produitId = toInt(r.get("Prod_ProductId"));
      if (produitId == null) continue;
      if (toInt(r.get("Prod_Deleted")) == 1) continue;

      Produit produitExistant = produits.findByIdExterneCrm(produitId).orElse(null);

      String nom = Objects.toString(r.get("Prod_Name"), "Produit " + produitId);
      String description = Objects.toString(r.get("Prod_PRDescription"), null);
      String reference = Objects.toString(r.get("Prod_Code"), null);
      Integer familyId = toInt(r.get("Prod_ProductFamilyId"));
      String categorie = recupererNomFamille(familyId);

      if (produitExistant != null) {
        produitExistant.setNom(nom);
        produitExistant.setDescription(description);
        produitExistant.setReference(reference);
        produitExistant.setCategorie(categorie);
        produitExistant.setDateMiseAJour(LocalDateTime.now());
        produits.save(produitExistant);
      } else {
        Produit nouveauProduit = new Produit();
        nouveauProduit.setNom(nom);
        nouveauProduit.setDescription(description);
        nouveauProduit.setReference(reference);
        nouveauProduit.setCategorie(categorie);
        nouveauProduit.setVersion(null);
        nouveauProduit.setActif(true);
        nouveauProduit.setDateCreation(LocalDateTime.now());
        nouveauProduit.setDateMiseAJour(LocalDateTime.now());
        nouveauProduit.setIdExterneCrm(produitId);
        produits.save(nouveauProduit);
      }
    }
  }

  private String recupererNomFamille(Integer familyId) {
    if (familyId == null) return null;
    try {
      String nom = crmJdbc.queryForObject(
        "SELECT ProdFam_Name FROM dbo.ProductFamily WHERE ProdFam_ProductFamilyId = ?",
        String.class,
        familyId
      );
      return nom;
    } catch (Exception e) {
      return null;
    }
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
