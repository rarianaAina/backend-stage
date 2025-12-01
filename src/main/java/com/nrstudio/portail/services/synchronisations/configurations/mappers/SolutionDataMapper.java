package com.nrstudio.portail.services.synchronisations.configurations.mappers;

import com.nrstudio.portail.domaine.solution.Solution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

@Component
public class SolutionDataMapper {
    
    public boolean estSolutionValide(Map<String, Object> donneesCrm) {
        Integer solutionId = extraireSolutionId(donneesCrm);
        Integer deletedFlag = toInt(donneesCrm.get("Soln_Deleted"));
        return solutionId != null && (deletedFlag == null || deletedFlag == 0);
    }
    
    public boolean estSolutionSupprimee(Map<String, Object> donneesCrm) {
        Integer deletedFlag = toInt(donneesCrm.get("Soln_Deleted"));
        return deletedFlag != null && deletedFlag == 1;
    }
    
    public boolean estNouvelleSolution(Map<String, Object> donneesCrm) {
        // Implémentez la logique pour déterminer si c'est une nouvelle solution
        return false; // À adapter selon votre logique métier
    }
    
    public Integer extraireSolutionId(Map<String, Object> donneesCrm) {
        return toInt(donneesCrm.get("Soln_SolutionId"));
    }
    
    public boolean mettreAJourSolution(Solution solution, Map<String, Object> donneesCrm) {
        boolean aChange = false;
        
        // Vérifier et mettre à jour chaque champ
        String nouveauTitre = Objects.toString(donneesCrm.get("Soln_Description"), "");
        if (!Objects.equals(solution.getTitre(), nouveauTitre)) {
            solution.setTitre(nouveauTitre);
            aChange = true;
        }
        
        String nouvelleDescription = Objects.toString(donneesCrm.get("Soln_SolutionDetails"), "");
        if (!Objects.equals(solution.getDescription(), nouvelleDescription)) {
            solution.setDescription(nouvelleDescription);
            aChange = true;
        }
        
        String nouveauStatut = Objects.toString(donneesCrm.get("Soln_Status"), "");
        if (!Objects.equals(solution.getStatut(), nouveauStatut)) {
            solution.setStatut(nouveauStatut);
            aChange = true;
        }
        
        String nouvelleEtape = Objects.toString(donneesCrm.get("Soln_Stage"), "");
        if (!Objects.equals(solution.getEtape(), nouvelleEtape)) {
            solution.setEtape(nouvelleEtape);
            aChange = true;
        }
        
        // Gestion de la clôture
        LocalDateTime dateCloture = convertToLocalDateTime(donneesCrm.get("Soln_Closed"));
        boolean estCloture = dateCloture != null;
        if (solution.isCloture() != estCloture || !Objects.equals(solution.getDateCloture(), dateCloture)) {
            solution.setCloture(estCloture);
            solution.setDateCloture(dateCloture);
            aChange = true;
        }
        
        // Autres champs
        solution.setZone(Objects.toString(donneesCrm.get("Soln_Area"), ""));
        solution.setReference(Objects.toString(donneesCrm.get("Soln_ReferenceId"), ""));
        
        Integer secteur = toInt(donneesCrm.get("Soln_Secterr"));
        solution.setSecteur(secteur != null ? secteur.toString() : null);
        
        solution.setDateMiseAJour(convertToLocalDateTime(donneesCrm.get("Soln_UpdatedDate")));
        solution.setDateExternalisation(convertToLocalDateTime(donneesCrm.get("Soln_TimeStamp")));
        
        solution.setMisAJourPar(toInt(donneesCrm.get("Soln_UpdatedBy")));
        solution.setUtilisateurAttribue(toInt(donneesCrm.get("Soln_AssignedUserId")));
        solution.setWorkflowId(toInt(donneesCrm.get("Soln_WorkflowId")));
        solution.setCanalId(toInt(donneesCrm.get("soln_ChannelId")));
        solution.setCleExterneTalend(Objects.toString(donneesCrm.get("soln_TalendExterKey"), ""));
        
        solution.setDateSynchronisation(LocalDateTime.now());
        
        return aChange;
    }
    
    private LocalDateTime convertToLocalDateTime(Object dateValue) {
        if (dateValue == null) return null;
        try {
            if (dateValue instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dateValue).toLocalDateTime();
            } else if (dateValue instanceof java.sql.Date) {
                return ((java.sql.Date) dateValue).toLocalDate().atStartOfDay();
            } else if (dateValue instanceof java.util.Date) {
                return new java.sql.Timestamp(((java.util.Date) dateValue).getTime()).toLocalDateTime();
            }
        } catch (Exception e) {
            // Log silencieux
        }
        return null;
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