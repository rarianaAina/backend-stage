package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.services.TicketService;
import com.nrstudio.portail.services.synchronisations.configurations.executor.TicketSyncExecutor;
import com.nrstudio.portail.services.synchronisations.configurations.processors.TicketDataProcessor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CrmTicketSyncService {

    private final JdbcTemplate crmJdbc;
    private final TicketRepository tickets;
    private final CompanyRepository companies;
    private final UtilisateurRepository users;
    private final UtilisateurInterneRepository utilisateurs;
    private final TicketService ticketService;
    private final SynchronisationManager synchronisationManager;
    private final ProduitRepository produitRepository;
    private final TicketDataProcessor dataProcessor;
    private final TicketSyncExecutor syncExecutor;
    
    private static final Logger log = LoggerFactory.getLogger(CrmTicketSyncService.class);

    public CrmTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              TicketRepository tickets,
                              CompanyRepository companies,
                              UtilisateurRepository users,
                              UtilisateurInterneRepository utilisateurs,
                              TicketService ticketService,
                              SynchronisationManager synchronisationManager,
                              ProduitRepository produitRepository) {
        this.crmJdbc = crmJdbc;
        this.tickets = tickets;
        this.companies = companies;
        this.users = users;
        this.utilisateurs = utilisateurs;
        this.ticketService = ticketService;
        this.synchronisationManager = synchronisationManager;
        this.produitRepository = produitRepository;
        this.dataProcessor = new TicketDataProcessor();
        this.syncExecutor = new TicketSyncExecutor(
            crmJdbc,
            tickets,
            companies,
            users,
            utilisateurs,
            ticketService,
            produitRepository,
            dataProcessor
        );
    }

    // Synchronisation planifiée - non interruptible
    @Transactional
    public void importerDepuisCrm() {
        log.info("🚀 Début de la synchronisation planifiée des tickets - {}", LocalDateTime.now());
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle - interruptible
    @Transactional
    public void importerDepuisCrmManuellement() {
        log.info("🚀 Début de la synchronisation manuelle des tickets - {}", LocalDateTime.now());
        executerSynchronisationManuelle();
    }

    private void executerSynchronisationPlanifiee() {
        syncExecutor.executerSynchronisationPlanifiee();
    }

    private void executerSynchronisationManuelle() {
        syncExecutor.executerSynchronisationManuelle(synchronisationManager);
    }
}