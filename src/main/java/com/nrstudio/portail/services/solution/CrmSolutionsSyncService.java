package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import com.nrstudio.portail.services.synchronisations.configurations.executor.SolutionSynchronisationExecutor;
import com.nrstudio.portail.services.synchronisations.configurations.processors.SolutionDataProcessor;
import java.time.LocalDateTime;

@Service
public class CrmSolutionsSyncService {

    private final JdbcTemplate crmJdbc;
    private final SolutionRepository solutionRepository;
    private final SynchronisationManager synchronisationManager;
    private final SolutionDataProcessor solutionDataProcessor;

    public CrmSolutionsSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                  SolutionRepository solutionRepository,
                                  SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.solutionRepository = solutionRepository;
        this.synchronisationManager = synchronisationManager;
        this.solutionDataProcessor = new SolutionDataProcessor();
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
        SolutionSynchronisationExecutor executor = new SolutionSynchronisationExecutor(
            crmJdbc, solutionRepository, solutionDataProcessor, false
        );
        executor.executer();
    }

    private void executerSynchronisationManuelle() {
        SolutionSynchronisationExecutor executor = new SolutionSynchronisationExecutor(
            crmJdbc, solutionRepository, solutionDataProcessor, true
        );
        executor.executerManuellement(synchronisationManager);
    }
}