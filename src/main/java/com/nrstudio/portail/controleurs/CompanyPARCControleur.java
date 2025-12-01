package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.domaine.CompanyPARC;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.dto.companyparc.CompanyPARCDto;
import com.nrstudio.portail.dto.companyparc.InterlocuteurDto;
import com.nrstudio.portail.services.company.CompanyPARCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin
public class CompanyPARCControleur {

    private final CompanyPARCService companyPARCService;

    @Autowired
    public CompanyPARCControleur(CompanyPARCService companyPARCService) {
        this.companyPARCService = companyPARCService;
    }

    // Endpoint existant retournant les entités
    @GetMapping(value = "/parcs/company/{companyId}", produces = "application/json")
    public List<CompanyPARC> getParcsByCompanyId(@PathVariable("companyId") Integer companyId) {
        System.out.println("Company ID: " + companyId);
        return companyPARCService.getParcsByCompanyId(companyId);
    }

    // NOUVEAU Endpoint retournant les DTOs avec le nom complet de l'utilisateur
    @GetMapping(value = "/parcs/company/{companyId}/with-username", produces = "application/json")
    public List<CompanyPARCDto> getParcsWithUserFullNameByCompanyId(@PathVariable("companyId") Integer companyId) {
        System.out.println("Company ID (avec nom utilisateur): " + companyId);
        return companyPARCService.getParcsDtosByCompanyId(companyId);
    }

    // Récupérer l'ID utilisateur interne associé à un CompanyPARC
    @GetMapping(value = "/parcs/{parcId}/user", produces = "application/json")
    public Integer getUserIdByParcId(@PathVariable("parcId") Integer parcId) {
        return companyPARCService.getUserIdByParcId(parcId);
    }

    // NOUVEAU Endpoint : Récupérer le nom complet de l'utilisateur par parcId
    @GetMapping(value = "/parcs/{parcId}/user-fullname", produces = "application/json")
    public String getUserFullNameByParcId(@PathVariable("parcId") Integer parcId) {
        return companyPARCService.getUserFullNameByParcId(parcId);
    }

    // NOUVEAU Endpoint : Récupérer les détails complets de l'utilisateur par parcId
    @GetMapping(value = "/parcs/{parcId}/utilisateur-interne", produces = "application/json")
    public UtilisateurInterne getUtilisateurInterneByParcId(@PathVariable("parcId") Integer parcId) {
        return companyPARCService.getUtilisateurInterneByParcId(parcId);
    }

        
    // NOUVEAU : Récupérer tous les interlocuteurs distincts pour une company (juste ID et nom)
    @GetMapping(value = "/company/{companyId}/interlocuteurs", produces = "application/json")
    public List<InterlocuteurDto> getInterlocuteursByCompanyId(@PathVariable("companyId") String companyId) {
        System.out.println("Récupération des interlocuteurs pour company ID externe: " + companyId);
        return companyPARCService.getInterlocuteursByCompanyId(companyId);
    }
}