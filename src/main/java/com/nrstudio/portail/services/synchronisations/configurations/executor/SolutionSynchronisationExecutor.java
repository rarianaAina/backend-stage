package com.nrstudio.portail.services.synchronisations.configurations.executor;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.services.synchronisations.configurations.processors.SolutionDataProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SolutionSynchronisationExecutor {

    private final JdbcTemplate crmJdbc;
    private final SolutionRepository solutionRepository;
    private final SolutionDataProcessor solutionDataProcessor;
    private final boolean estManuelle;
    
    private int compteurNouveaux = 0;
    private int compteurMaj = 0;
    private int compteurSupprimes = 0;
    private int compteurErreurs = 0;

    public SolutionSynchronisationExecutor(JdbcTemplate crmJdbc,
                                          SolutionRepository solutionRepository,
                                          SolutionDataProcessor solutionDataProcessor,
                                          boolean estManuelle) {
        this.crmJdbc = crmJdbc;
        this.solutionRepository = solutionRepository;
        this.solutionDataProcessor = solutionDataProcessor;
        this.estManuelle = estManuelle;
    }

    public void executer() {
        final String sql = getSqlRequete();
        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        
        for (Map<String, Object> r : rows) {
            traiterLigne(r);
        }
        
        System.out.println("✅ Synchronisation planifiée terminée - {} nouveaux, {} mis à jour, {} supprimés, {} erreurs"
            .formatted(compteurNouveaux, compteurMaj, compteurSupprimes, compteurErreurs));
    }

    public void executerManuellement(SynchronisationManager synchronisationManager) {
        final String typeSync = "solutions";
        
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des solutions est déjà en cours");
        }

        synchronisationManager.demarrerSynchronisation(typeSync);
        
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                executerAvecInterruption(synchronisationManager, typeSync);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la synchronisation manuelle des solutions: " + e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
            }
        });
        
        syncThread.start();
    }

    private void executerAvecInterruption(SynchronisationManager synchronisationManager, String typeSync) {
        final String sql = getSqlRequete();
        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        
        for (Map<String, Object> r : rows) {
            if (synchronisationManager.doitArreter(typeSync)) {
                System.out.println("🛑 Synchronisation manuelle des solutions arrêtée à la demande");
                return;
            }

            traiterLigne(r);
            
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
    }

    private void traiterLigne(Map<String, Object> r) {
        try {
            Integer solutionId = solutionDataProcessor.toInt(r.get("Soln_SolutionId"));
            if (solutionId == null) return;
            
            if (solutionDataProcessor.toInt(r.get("Soln_Deleted")) == 1) {
                traiterSuppression(solutionId);
                return;
            }

            String idExterneCrm = solutionId.toString();
            solutionRepository.findByIdExterneCrm(idExterneCrm).ifPresentOrElse(
                solutionExistante -> {
                    if (solutionDataProcessor.aDonneesChangees(solutionExistante, r)) {
                        solutionDataProcessor.mettreAJourSolutionExistante(solutionExistante, r);
                        solutionRepository.save(solutionExistante);
                        System.out.println("🔄 Solution mise à jour: " + solutionExistante.getIdExterneCrm());
                        compteurMaj++;
                    }
                },
                () -> {
                    Solution nouvelleSolution = solutionDataProcessor.creerNouvelleSolution(r, idExterneCrm);
                    solutionRepository.save(nouvelleSolution);
                    System.out.println("✅ Solution créée: " + idExterneCrm + " - " + nouvelleSolution.getTitre());
                    compteurNouveaux++;
                }
            );
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de la solution: " + e.getMessage());
            compteurErreurs++;
        }
    }

    private void traiterSuppression(Integer solutionId) {
        solutionRepository.findByIdExterneCrm(solutionId.toString())
            .ifPresent(solution -> {
                solution.setSupprime(true);
                solution.setDateSynchronisation(LocalDateTime.now());
                solutionRepository.save(solution);
                System.out.println("Solution marquée comme supprimée: " + solutionId);
                compteurSupprimes++;
            });
    }

    private String getSqlRequete() {
        return "SELECT Soln_SolutionId, Soln_CreatedBy, Soln_CreatedDate, Soln_UpdatedBy, " +
               "       Soln_UpdatedDate, Soln_TimeStamp, ISNULL(Soln_Deleted,0) AS Soln_Deleted, " +
               "       Soln_AssignedUserId, Soln_Area, Soln_SolutionDetails, Soln_Description, " +
               "       Soln_Stage, Soln_Status, Soln_ReferenceId, Soln_Closed, " +
               "       Soln_Secterr, Soln_WorkflowId, soln_ChannelId, soln_TalendExterKey " +
               "FROM dbo.Solutions";
    }
}