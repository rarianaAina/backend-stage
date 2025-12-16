package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.depots.solution.SolutionTicketRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.domaine.solution.SolutionTicket;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.services.TicketService;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import com.nrstudio.portail.services.synchronisations.configurations.processors.SolutionTicketDataProcessor;
import com.nrstudio.portail.services.synchronisations.configurations.executor.SolutionTicketSyncExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CrmSolutionTicketSyncService {

    private final JdbcTemplate crmJdbc;
    private final SolutionTicketRepository solutionTicketRepository;
    private final SolutionRepository solutionRepository;
    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final SynchronisationManager synchronisationManager;
    private final SolutionTicketDataProcessor dataProcessor;
    private final SolutionTicketSyncExecutor syncExecutor;

    public CrmSolutionTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                       SolutionTicketRepository solutionTicketRepository,
                                       SolutionRepository solutionRepository,
                                       TicketRepository ticketRepository,
                                       TicketService ticketService,
                                       SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.solutionTicketRepository = solutionTicketRepository;
        this.solutionRepository = solutionRepository;
        this.ticketRepository = ticketRepository;
        this.ticketService = ticketService;
        this.synchronisationManager = synchronisationManager;
        this.dataProcessor = new SolutionTicketDataProcessor();
        this.syncExecutor = new SolutionTicketSyncExecutor(
            crmJdbc, 
            solutionTicketRepository,
            solutionRepository,
            ticketRepository,
            ticketService,
            dataProcessor
        );
    }

    // Synchronisation planifiée - non interruptible
    @Transactional
    public void synchroniserLiaisonsSolutionsTicketsDynamique() {
        System.out.println("🚀 Synchronisation via CRON dynamique (DB)");
        syncExecutor.setSynchronisationManuelleEnCours(false);
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle - interruptible
    @Transactional
    public void synchroniserLiaisonsSolutionsTicketsManuellement() {
        System.out.println("🚀 Début de la synchronisation manuelle des liaisons solutions-tickets");
        syncExecutor.setSynchronisationManuelleEnCours(true);
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        syncExecutor.executerSynchronisationPlanifiee();
    }

    private void executerSynchronisationManuelle() {
        syncExecutor.executerSynchronisationManuelle(synchronisationManager);
    }
}