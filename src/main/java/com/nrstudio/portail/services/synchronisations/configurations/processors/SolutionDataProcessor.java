package com.nrstudio.portail.services.synchronisations.configurations.processors;

import com.nrstudio.portail.domaine.solution.Solution;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class SolutionDataProcessor {

    public boolean aDonneesChangees(Solution solution, Map<String, Object> donneesCrm) {
        return !Objects.equals(solution.getTitre(), Objects.toString(donneesCrm.get("Soln_Description"), "")) ||
               !Objects.equals(solution.getDescription(), Objects.toString(donneesCrm.get("Soln_SolutionDetails"), "")) ||
               !Objects.equals(solution.getStatut(), Objects.toString(donneesCrm.get("Soln_Status"), "")) ||
               !Objects.equals(solution.getEtape(), Objects.toString(donneesCrm.get("Soln_Stage"), "")) ||
               aChampClotureChange(solution, donneesCrm) ||
               aChampSupprimeChange(solution, donneesCrm);
    }

    private boolean aChampClotureChange(Solution solution, Map<String, Object> donneesCrm) {
        LocalDateTime nouvelleDateCloture = convertToLocalDateTime(donneesCrm.get("Soln_Closed"));
        boolean nouvelleCloture = nouvelleDateCloture != null;
        
        return solution.isCloture() != nouvelleCloture ||
               !Objects.equals(solution.getDateCloture(), nouvelleDateCloture);
    }

    private boolean aChampSupprimeChange(Solution solution, Map<String, Object> donneesCrm) {
        boolean nouveauSupprime = toInt(donneesCrm.get("Soln_Deleted")) == 1;
        return solution.isSupprime() != nouveauSupprime;
    }

    public Solution creerNouvelleSolution(Map<String, Object> r, String idExterneCrm) {
        Solution solution = new Solution();
        
        solution.setIdExterneCrm(idExterneCrm);
        solution.setTitre(Objects.toString(r.get("Soln_Description"), ""));
        solution.setDescription(Objects.toString(r.get("Soln_SolutionDetails"), ""));
        solution.setZone(Objects.toString(r.get("Soln_Area"), ""));
        solution.setStatut(Objects.toString(r.get("Soln_Status"), ""));
        solution.setEtape(Objects.toString(r.get("Soln_Stage"), ""));
        solution.setReference(Objects.toString(r.get("Soln_ReferenceId"), ""));
        
        Integer secteur = toInt(r.get("Soln_Secterr"));
        solution.setSecteur(secteur != null ? secteur.toString() : null);
        
        LocalDateTime dateCloture = convertToLocalDateTime(r.get("Soln_Closed"));
        solution.setCloture(dateCloture != null);
        solution.setDateCloture(dateCloture);
        solution.setSupprime(false);
        
        solution.setDateCreation(convertToLocalDateTime(r.get("Soln_CreatedDate")));
        solution.setDateMiseAJour(convertToLocalDateTime(r.get("Soln_UpdatedDate")));
        solution.setDateExternalisation(convertToLocalDateTime(r.get("Soln_TimeStamp")));
        
        solution.setCreePar(toInt(r.get("Soln_CreatedBy")));
        solution.setMisAJourPar(toInt(r.get("Soln_UpdatedBy")));
        solution.setUtilisateurAttribue(toInt(r.get("Soln_AssignedUserId")));
        solution.setWorkflowId(toInt(r.get("Soln_WorkflowId")));
        solution.setCanalId(toInt(r.get("soln_ChannelId")));
        solution.setCleExterneTalend(Objects.toString(r.get("soln_TalendExterKey"), ""));
        
        solution.setDateSynchronisation(LocalDateTime.now());

        return solution;
    }

    public void mettreAJourSolutionExistante(Solution solution, Map<String, Object> r) {
        solution.setTitre(Objects.toString(r.get("Soln_Description"), ""));
        solution.setDescription(Objects.toString(r.get("Soln_SolutionDetails"), ""));
        solution.setZone(Objects.toString(r.get("Soln_Area"), ""));
        solution.setStatut(Objects.toString(r.get("Soln_Status"), ""));
        solution.setEtape(Objects.toString(r.get("Soln_Stage"), ""));
        solution.setReference(Objects.toString(r.get("Soln_ReferenceId"), ""));
        
        Integer secteur = toInt(r.get("Soln_Secterr"));
        solution.setSecteur(secteur != null ? secteur.toString() : null);
        
        LocalDateTime dateCloture = convertToLocalDateTime(r.get("Soln_Closed"));
        solution.setCloture(dateCloture != null);
        solution.setDateCloture(dateCloture);
        solution.setSupprime(toInt(r.get("Soln_Deleted")) == 1);
        
        solution.setDateMiseAJour(convertToLocalDateTime(r.get("Soln_UpdatedDate")));
        solution.setDateExternalisation(convertToLocalDateTime(r.get("Soln_TimeStamp")));
        
        solution.setMisAJourPar(toInt(r.get("Soln_UpdatedBy")));
        solution.setUtilisateurAttribue(toInt(r.get("Soln_AssignedUserId")));
        solution.setWorkflowId(toInt(r.get("Soln_WorkflowId")));
        solution.setCanalId(toInt(r.get("soln_ChannelId")));
        solution.setCleExterneTalend(Objects.toString(r.get("soln_TalendExterKey"), ""));
        
        solution.setDateSynchronisation(LocalDateTime.now());
    }

    public LocalDateTime convertToLocalDateTime(Object dateValue) {
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
            System.err.println("Erreur conversion date: " + e.getMessage());
        }
        return null;
    }

    public Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}