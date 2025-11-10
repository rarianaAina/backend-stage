package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyPARCRepository;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.CompanyPARC;
import com.nrstudio.portail.domaine.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyPARCService {

    private final CompanyPARCRepository companyPARCRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyPARCService(CompanyPARCRepository companyPARCRepository, CompanyRepository companyRepository) {
        this.companyPARCRepository = companyPARCRepository;
        this.companyRepository = companyRepository;
    }

    public List<CompanyPARC> getParcsByCompanyId(Integer companyId) {
        // Rechercher la company par son ID pour obtenir l'idExterneCrm
        Optional<Company> company = companyRepository.findById(companyId);
        
        if (company.isPresent()) {
            String idExterneCrm = company.get().getIdExterneCrm();
            int idExterneCrmInt = Integer.parseInt(idExterneCrm);
            System.out.println("Recherche des PARCs pour la company avec idExterneCrm: " + idExterneCrmInt);
            // Utiliser l'idExterneCrm pour rechercher les PARCs
            return companyPARCRepository.findByParcCompanyId(idExterneCrmInt);
        } else {
            // Gérer le cas où la company n'est pas trouvée
            throw new RuntimeException("Company non trouvée avec l'ID: " + companyId);
            // Ou retourner une liste vide selon votre besoin :
            // return List.of();
        }
    }
}