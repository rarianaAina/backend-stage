package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CreditHoraireRepository;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.domaine.CreditHoraire;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.dto.CreditHoraireDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CreditHoraireService {

  private final CreditHoraireRepository creditHoraireRepository;
  private final CompanyRepository companyRepository;
  private final ProduitRepository produitRepository;

  public CreditHoraireService(CreditHoraireRepository creditHoraireRepository,
                              CompanyRepository companyRepository,
                              ProduitRepository produitRepository) {
    this.creditHoraireRepository = creditHoraireRepository;
    this.companyRepository = companyRepository;
    this.produitRepository = produitRepository;
  }

  public List<CreditHoraireDto> getCreditsParCompany(Integer companyId) {
    Company company = getCompany(companyId);
    List<CreditHoraire> credits = creditHoraireRepository.findByCompanyAndActifTrueOrderByPeriodeFinDesc(company);
    return credits.stream().map(this::toDto).collect(Collectors.toList());
  }

  public List<CreditHoraireDto> getCreditsParCompanyEtProduit(Integer companyId, Integer produitId) {
    Company company = getCompany(companyId);
    Produit produit = getProduit(produitId);
    List<CreditHoraire> credits = creditHoraireRepository.findByCompanyAndProduitAndActifTrueOrderByPeriodeFinDesc(company, produit);
    return credits.stream().map(this::toDto).collect(Collectors.toList());
  }

// public List<CreditHoraireDto> getCreditsParCompanyEtProduit(Integer companyId, Integer produitIdExterne) {
//     System.out.println("=== DÉBUT getCreditsParCompanyEtProduit ===");
//     System.out.println("Company ID: " + companyId + ", Produit ID externe: " + produitIdExterne);
    
//     try {
//         System.out.println("1. Recherche de la company...");
//         Company company = getCompany(companyId);
//         System.out.println("Company trouvée: " + company.getId());
        
//         System.out.println("2. Recherche du produit...");
//         Produit produit = produitRepository.findByIdExterneCrm(String.valueOf(produitIdExterne))
//             .orElseThrow(() -> {
//                 System.err.println("PRODUIT NON TROUVÉ: " + produitIdExterne);
//                 return new RuntimeException("Produit non trouvé avec l'ID externe: " + produitIdExterne);
//             });
//         System.out.println("Produit trouvé: " + produit.getLibelle());
        
//         System.out.println("3. Recherche des crédits horaires...");
//         List<CreditHoraire> credits = creditHoraireRepository.findByCompanyAndProduitAndActifTrueOrderByPeriodeFinDesc(company, produit);
//         System.out.println("4. " + credits.size() + " crédits trouvés");
        
//         return credits.stream().map(this::toDto).collect(Collectors.toList());
//     } catch (Exception e) {
//         System.err.println("ERREUR: " + e.getClass().getSimpleName() + " - " + e.getMessage());
//         e.printStackTrace();
//         return Collections.emptyList(); // ou throw selon votre besoin
//     }
// }

  public List<CreditHoraireDto> getCreditsActifs(Integer companyId) {
    LocalDate today = LocalDate.now();
    Company company = getCompany(companyId);
    List<CreditHoraire> credits = creditHoraireRepository.findCreditsActifsADate(company, today);
    return credits.stream().map(this::toDto).collect(Collectors.toList());
  }

  public List<CreditHoraireDto> getCreditsActifsPourProduit(Integer companyId, Integer produitId) {
    LocalDate today = LocalDate.now();
    Company company = getCompany(companyId);
    Produit produit = getProduit(produitId);
    List<CreditHoraire> credits = creditHoraireRepository.findCreditsActifsPourProduitADate(company, produit, today);
    return credits.stream().map(this::toDto).collect(Collectors.toList());
  }

  public CreditHoraire consommerHeures(Integer companyId, Integer produitId, Integer heures) {
    if (heures <= 0) {
      throw new RuntimeException("Le nombre d'heures doit être positif");
    }

    LocalDate today = LocalDate.now();
    Company company = getCompany(companyId);
    Produit produit = getProduit(produitId);

    // Trouver le premier crédit actif avec des heures restantes
    CreditHoraire credit = creditHoraireRepository
      .findPremierCreditActifPourProduit(company, produit, today)
      .orElseThrow(() -> new RuntimeException(
        String.format("Aucun crédit horaire actif avec des heures disponibles trouvé pour le produit: %s", produit.getLibelle())
      ));

    if (credit.getHeuresRestantes() < heures) {
      throw new RuntimeException(String.format(
        "Crédit horaire insuffisant pour le produit %s. Restant: %d heures, Demandé: %d heures",
        produit.getLibelle(), credit.getHeuresRestantes(), heures
      ));
    }

    credit.setHeuresConsommees(credit.getHeuresConsommees() + heures);
    return creditHoraireRepository.save(credit);
  }

  public Integer getHeuresRestantesPourProduit(Integer companyId, Integer produitId) {
    LocalDate today = LocalDate.now();
    Company company = getCompany(companyId);
    Produit produit = getProduit(produitId);
    Integer total = creditHoraireRepository.sumHeuresRestantesPourProduit(company, produit, today);
    return total != null ? total : 0;
  }

  public Integer getHeuresRestantesTotal(Integer companyId) {
    LocalDate today = LocalDate.now();
    Company company = getCompany(companyId);
    Integer total = creditHoraireRepository.sumHeuresRestantesActives(company, today);
    return total != null ? total : 0;
  }

  public void desactiverCreditsExpires() {
    LocalDate today = LocalDate.now();
    List<CreditHoraire> creditsExpires = creditHoraireRepository.findCreditsExpires(today);
    
    creditsExpires.forEach(credit -> {
      credit.setActif(false);
      creditHoraireRepository.save(credit);
    });
  }

  // Méthodes utilitaires
  private Company getCompany(Integer companyId) {
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new RuntimeException("Company non trouvée avec l'ID: " + companyId));
  }

  private Produit getProduit(Integer produitId) {
    return produitRepository.findById(produitId)
        .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID: " + produitId));
  }

  private CreditHoraireDto toDto(CreditHoraire credit) {
    CreditHoraireDto dto = new CreditHoraireDto();
    dto.setId(credit.getId());
    dto.setNomCompany(credit.getCompany() != null ? credit.getCompany().getNom() : "");
    dto.setNomProduit(credit.getProduit() != null ? credit.getProduit().getLibelle() : "");
    dto.setProduitId(credit.getProduit() != null ? credit.getProduit().getId() : null);
    dto.setPeriodeDebut(credit.getPeriodeDebut());
    dto.setPeriodeFin(credit.getPeriodeFin());
    dto.setHeuresAllouees(credit.getHeuresAllouees());
    dto.setHeuresConsommees(credit.getHeuresConsommees());
    dto.setHeuresRestantes(credit.getHeuresRestantes());
    
    double pourcentage = credit.getHeuresAllouees() > 0 
      ? (credit.getHeuresConsommees() * 100.0) / credit.getHeuresAllouees()
      : 0;
    dto.setPourcentageUtilisation(Math.round(pourcentage * 100.0) / 100.0);
    
    dto.setActif(credit.isActif());
    dto.setExpire(credit.getPeriodeFin() != null && credit.getPeriodeFin().isBefore(LocalDate.now()));
    
    return dto;
  }
}