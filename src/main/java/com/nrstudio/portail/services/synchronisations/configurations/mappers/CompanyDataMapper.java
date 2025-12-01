package com.nrstudio.portail.services.synchronisations.configurations.mappers;

import com.nrstudio.portail.domaine.Company;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class CompanyDataMapper {
    
    public boolean estCompanyValide(Map<String, Object> donneesCrm) {
        Integer companyId = extraireCompanyId(donneesCrm);
        Integer deletedFlag = toInt(donneesCrm.get("Comp_Deleted"));
        
        return companyId != null && (deletedFlag == null || deletedFlag == 0);
    }
    
    public Integer extraireCompanyId(Map<String, Object> donneesCrm) {
        return toInt(donneesCrm.get("Comp_CompanyId"));
    }
    
    public void mettreAJourCompany(Company company, Map<String, Object> donneesCrm, Integer companyId) {
        String nom = Objects.toString(donneesCrm.get("Comp_Name"), "Société " + companyId);
        company.setNom(nom);
        
        if (company.getCodeCompany() == null) {
            company.setCodeCompany("COMP-" + companyId);
        }
    }
    
    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}