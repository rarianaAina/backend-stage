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
    List<CreditHoraire> credits = creditHoraireRepository
      .findByCompanyIdAndActifTrueOrderByPeriodeFinDesc(companyId);
    
    return credits.stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }

  public List<CreditHoraireDto> getCreditsActifs(Integer companyId) {
    LocalDate today = LocalDate.now();
    List<CreditHoraire> credits = creditHoraireRepository
      .findCreditsActifsADate(companyId, today);
    
    return credits.stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }

  public CreditHoraire consommerHeures(Integer companyId, Integer produitId, Integer heures) {
    LocalDate today = LocalDate.now();
    CreditHoraire credit = creditHoraireRepository
      .findCreditActifPourProduit(companyId, produitId, today)
      .orElseThrow(() -> new RuntimeException("Aucun crédit horaire actif trouvé"));

    if (credit.getHeuresRestantes() < heures) {
      throw new RuntimeException("Crédit horaire insuffisant");
    }

    credit.setHeuresConsommees(credit.getHeuresConsommees() + heures);
    return creditHoraireRepository.save(credit);
  }

  public Integer getHeuresRestantesTotal(Integer companyId) {
    LocalDate today = LocalDate.now();
    Integer total = creditHoraireRepository.sumHeuresRestantesActives(companyId, today);
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

  private CreditHoraireDto toDto(CreditHoraire credit) {
    CreditHoraireDto dto = new CreditHoraireDto();
    dto.setId(credit.getId());
    
    Company company = companyRepository.findById(credit.getCompanyId()).orElse(null);
    dto.setNomCompany(company != null ? company.getNom() : "");
    
    if (credit.getProduitId() != null) {
      Produit produit = produitRepository.findById(credit.getProduitId()).orElse(null);
      dto.setNomProduit(produit != null ? produit.getLibelle() : "");
    }
    
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
    dto.setExpire(credit.getPeriodeFin().isBefore(LocalDate.now()));
    
    return dto;
  }
}
