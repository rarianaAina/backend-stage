package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.CreditHoraire;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditHoraireRepository extends JpaRepository<CreditHoraire, Integer> {
    
    // Pour la synchronisation - avec produit
    Optional<CreditHoraire> findByCompanyAndProduitAndPeriodeDebutAndPeriodeFin(
        Company company, Produit produit, LocalDate periodeDebut, LocalDate periodeFin);
    
    // Pour la synchronisation - sans produit
    Optional<CreditHoraire> findByCompanyAndPeriodeDebutAndPeriodeFin(
        Company company, LocalDate periodeDebut, LocalDate periodeFin);
    
    // Crédits par company (tous produits)
    List<CreditHoraire> findByCompanyAndActifTrueOrderByPeriodeFinDesc(Company company);
    
    // Crédits par company et produit
    List<CreditHoraire> findByCompanyAndProduitAndActifTrueOrderByPeriodeFinDesc(Company company, Produit produit);
    
    // Tous les crédits actifs d'une company à une date
    @Query("SELECT c FROM CreditHoraire c WHERE c.company = :company AND c.actif = true AND c.periodeDebut <= :date AND (c.periodeFin IS NULL OR c.periodeFin >= :date) ORDER BY c.periodeFin DESC")
    List<CreditHoraire> findCreditsActifsADate(@Param("company") Company company, @Param("date") LocalDate date);
    
    // Crédits actifs pour un produit spécifique à une date
    @Query("SELECT c FROM CreditHoraire c WHERE c.company = :company AND c.produit = :produit AND c.actif = true AND c.periodeDebut <= :date AND (c.periodeFin IS NULL OR c.periodeFin >= :date) ORDER BY c.periodeFin DESC")
    List<CreditHoraire> findCreditsActifsPourProduitADate(@Param("company") Company company, @Param("produit") Produit produit, @Param("date") LocalDate date);
    
    // Premier crédit actif pour un produit (pour consommation)
    @Query("SELECT c FROM CreditHoraire c WHERE c.company = :company AND c.produit = :produit AND c.actif = true AND c.periodeDebut <= :date AND (c.periodeFin IS NULL OR c.periodeFin >= :date) AND c.heuresRestantes > 0 ORDER BY c.periodeFin ASC")
    Optional<CreditHoraire> findPremierCreditActifPourProduit(@Param("company") Company company, @Param("produit") Produit produit, @Param("date") LocalDate date);
    
    // Somme des heures restantes pour un produit
    @Query("SELECT SUM(c.heuresRestantes) FROM CreditHoraire c WHERE c.company = :company AND c.produit = :produit AND c.actif = true AND (c.periodeFin IS NULL OR c.periodeFin >= :date)")
    Integer sumHeuresRestantesPourProduit(@Param("company") Company company, @Param("produit") Produit produit, @Param("date") LocalDate date);
    
    // Somme des heures restantes tous produits
    @Query("SELECT SUM(c.heuresRestantes) FROM CreditHoraire c WHERE c.company = :company AND c.actif = true AND (c.periodeFin IS NULL OR c.periodeFin >= :date)")
    Integer sumHeuresRestantesActives(@Param("company") Company company, @Param("date") LocalDate date);
    
    // Crédits expirés
    @Query("SELECT c FROM CreditHoraire c WHERE c.actif = true AND c.periodeFin < :date")
    List<CreditHoraire> findCreditsExpires(@Param("date") LocalDate date);
}