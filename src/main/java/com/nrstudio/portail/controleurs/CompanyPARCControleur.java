package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.domaine.CompanyPARC;
import com.nrstudio.portail.services.CompanyPARCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping(value = "/parcs/company/{companyId}", produces = "application/json")
    public List<CompanyPARC> getParcsByCompanyId(@PathVariable("companyId") Integer companyId) {
        System.out.println("Company ID: " + companyId);
        return companyPARCService.getParcsByCompanyId(companyId);
    }

}
