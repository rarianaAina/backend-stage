package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmSolutionsSyncService {

    private final JdbcTemplate crmJdbc;
    private final SolutionRepository solutionRepository;
    private final SynchronisationManager synchronisationManager;

    public CrmSolutionsSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                  SolutionRepository solutionRepository,
                                  SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.solutionRepository = solutionRepository;
        this.synchronisationManager = synchronisationManager;
    }

    // Synchronisation planifiée - non interruptible
    @Transactional
    public void synchroniserSolutions() {
        System.out.println("🚀 Début de la synchronisation planifiée des solutions - " + LocalDateTime.now());
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle - interruptible
    @Transactional
    public void synchroniserSolutionsManuellement() {
        System.out.println("🚀 Début de la synchronisation manuelle des solutions - " + LocalDateTime.now());
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        final String sql =
            "SELECT Soln_SolutionId, Soln_CreatedBy, Soln_CreatedDate, Soln_UpdatedBy, " +
            "       Soln_UpdatedDate, Soln_TimeStamp, ISNULL(Soln_Deleted,0) AS Soln_Deleted, " +
            "       Soln_AssignedUserId, Soln_Area, Soln_SolutionDetails, Soln_Description, " +
            "       Soln_Stage, Soln_Status, Soln_ReferenceId, Soln_Closed, " +
            "       Soln_Secterr, Soln_WorkflowId, soln_ChannelId, soln_TalendExterKey " +
            "FROM dbo.Solutions";

        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        
        int compteurNouveaux = 0;
        int compteurMaj = 0;
        int compteurSupprimes = 0;
        int compteurErreurs = 0;

        for (Map<String, Object> r : rows) {
            try {
                Integer solutionId = toInt(r.get("Soln_SolutionId"));
                if (solutionId == null) continue;
                
                // Vérifier si la solution est marquée comme supprimée
                if (toInt(r.get("Soln_Deleted")) == 1) {
                    solutionRepository.findByIdExterneCrm(solutionId.toString())
                        .ifPresent(solution -> {
                            solution.setSupprime(true);
                            solution.setDateSynchronisation(LocalDateTime.now());
                            solutionRepository.save(solution);
                            System.out.println("Solution marquée comme supprimée: " + solutionId);
                            
                        });
                    continue;
                }

                String idExterneCrm = solutionId.toString();
                solutionRepository.findByIdExterneCrm(idExterneCrm).ifPresentOrElse(
                    solutionExistante -> {
                        // Vérifier si les données ont vraiment changé avant de mettre à jour
                        if (aDonneesChangees(solutionExistante, r)) {
                            mettreAJourSolutionExistante(solutionExistante, r);
                            
                        }
                    },
                    () -> {
                        // Créer une nouvelle solution
                        creerNouvelleSolution(r, idExterneCrm);
                        
                    }
                );
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du traitement de la solution: " + e.getMessage());
                compteurErreurs++;
            }
        }
        
        System.out.println("✅ Synchronisation planifiée terminée - {} nouveaux, {} mis à jour, {} supprimés, {} erreurs"
            .formatted(compteurNouveaux, compteurMaj, compteurSupprimes, compteurErreurs));
    }

    private void executerSynchronisationManuelle() {
        final String typeSync = "solutions";
        
        // Vérifier si une synchronisation est déjà en cours
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des solutions est déjà en cours");
        }

        // Démarrer la synchronisation
        synchronisationManager.demarrerSynchronisation(typeSync);
        
        // Exécuter dans un thread séparé pour permettre l'interruption
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                
                final String sql =
                    "SELECT Soln_SolutionId, Soln_CreatedBy, Soln_CreatedDate, Soln_UpdatedBy, " +
                    "       Soln_UpdatedDate, Soln_TimeStamp, ISNULL(Soln_Deleted,0) AS Soln_Deleted, " +
                    "       Soln_AssignedUserId, Soln_Area, Soln_SolutionDetails, Soln_Description, " +
                    "       Soln_Stage, Soln_Status, Soln_ReferenceId, Soln_Closed, " +
                    "       Soln_Secterr, Soln_WorkflowId, soln_ChannelId, soln_TalendExterKey " +
                    "FROM dbo.Solutions";

                List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
                
                int compteurNouveaux = 0;
                int compteurMaj = 0;
                int compteurSupprimes = 0;
                int compteurErreurs = 0;

                for (Map<String, Object> r : rows) {
                    // Vérifier si l'arrêt a été demandé
                    if (synchronisationManager.doitArreter(typeSync)) {
                        System.out.println("🛑 Synchronisation manuelle des solutions arrêtée à la demande");
                        return;
                    }

                    try {
                        Integer solutionId = toInt(r.get("Soln_SolutionId"));
                        if (solutionId == null) continue;
                        
                        // Vérifier si la solution est marquée comme supprimée
                        if (toInt(r.get("Soln_Deleted")) == 1) {
                            solutionRepository.findByIdExterneCrm(solutionId.toString())
                                .ifPresent(solution -> {
                                    solution.setSupprime(true);
                                    solution.setDateSynchronisation(LocalDateTime.now());
                                    solutionRepository.save(solution);
                                    System.out.println("Solution marquée comme supprimée: " + solutionId);
                                    
                                });
                            continue;
                        }

                        String idExterneCrm = solutionId.toString();
                        solutionRepository.findByIdExterneCrm(idExterneCrm).ifPresentOrElse(
                            solutionExistante -> {
                                // Vérifier si les données ont vraiment changé avant de mettre à jour
                                if (aDonneesChangees(solutionExistante, r)) {
                                    mettreAJourSolutionExistante(solutionExistante, r);
                                    
                                }
                            },
                            () -> {
                                // Créer une nouvelle solution
                                creerNouvelleSolution(r, idExterneCrm);
                                
                            }
                        );
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors du traitement de la solution: " + e.getMessage());
                        compteurErreurs++;
                    }
                    
                    // Petit délai pour permettre une interruption plus réactive
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println("🛑 Synchronisation manuelle interrompue");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                System.out.println("✅ Synchronisation manuelle terminée - {} nouveaux, {} mis à jour, {} supprimés, {} erreurs"
                    .formatted(compteurNouveaux, compteurMaj, compteurSupprimes, compteurErreurs));
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la synchronisation manuelle des solutions: " + e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
            }
        });
        
        syncThread.start();
    }

    private boolean aDonneesChangees(Solution solution, Map<String, Object> donneesCrm) {
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

    private void creerNouvelleSolution(Map<String, Object> r, String idExterneCrm) {
        Solution solution = new Solution();
        
        // Mapping des champs
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

        solutionRepository.save(solution);
        System.out.println("✅ Solution créée: " + idExterneCrm + " - " + solution.getTitre());
    }

    private void mettreAJourSolutionExistante(Solution solution, Map<String, Object> r) {
        // Mettre à jour uniquement les champs qui peuvent changer
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

        solutionRepository.save(solution);
        System.out.println("🔄 Solution mise à jour: " + solution.getIdExterneCrm());
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
            System.err.println("Erreur conversion date: " + e.getMessage());
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