package com.nrstudio.portail.depots.company;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Company;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Transactional
public class CompanySyncRepository {
    
    private final CompanyRepository companyRepository;
    
    public CompanySyncRepository(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    
    public Company trouverOuCreerCompany(String idExterneCrm) {
        Optional<Company> companyExistante = companyRepository.findByIdExterneCrm(idExterneCrm);
        
        if (companyExistante.isPresent()) {
            Company company = companyExistante.get();
            company.setDateMiseAJour(LocalDateTime.now());
            return company;
        } else {
            Company nouvelleCompany = new Company();
            nouvelleCompany.setIdExterneCrm(idExterneCrm);
            nouvelleCompany.setActif(true);
            nouvelleCompany.setDateCreation(LocalDateTime.now());
            nouvelleCompany.setDateMiseAJour(LocalDateTime.now());
            return nouvelleCompany;
        }
    }
    
    public void sauvegarder(Company company) {
        companyRepository.save(company);
    }
}