package com.nrstudio.portail.depots.solution;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.domaine.solution.SolutionTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class SolutionTicketSyncRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(SolutionTicketSyncRepository.class);
    
    private final SolutionTicketRepository solutionTicketRepository;
    private final SolutionRepository solutionRepository;
    private final TicketRepository ticketRepository;
    
    public SolutionTicketSyncRepository(SolutionTicketRepository solutionTicketRepository,
                                      SolutionRepository solutionRepository,
                                      TicketRepository ticketRepository) {
        this.solutionTicketRepository = solutionTicketRepository;
        this.solutionRepository = solutionRepository;
        this.ticketRepository = ticketRepository;
    }
    
    public boolean creerLiaisonSiAbsente(Integer solutionIdCrm, Integer ticketIdCrm) {
        // Trouver la solution
        Optional<Solution> solutionOpt = solutionRepository.findByIdExterneCrm(solutionIdCrm.toString());
        if (solutionOpt.isEmpty()) {
            logger.debug("Solution non trouvée avec id_externe_crm: {}", solutionIdCrm);
            return false;
        }
        
        // Trouver le ticket
        Optional<Ticket> ticketOpt = ticketRepository.findByIdExterneCrm(ticketIdCrm);
        if (ticketOpt.isEmpty()) {
            logger.debug("Ticket non trouvé avec id_externe_crm: {}", ticketIdCrm);
            return false;
        }
        
        Solution solution = solutionOpt.get();
        Ticket ticket = ticketOpt.get();
        
        // Vérifier si la liaison existe déjà
        if (solutionTicketRepository.existsBySolutionIdAndTicketId(solution.getId(), ticket.getId())) {
            return false;
        }
        
        // Créer la nouvelle liaison
        SolutionTicket solutionTicket = new SolutionTicket(solution, ticket);
        solutionTicketRepository.save(solutionTicket);
        
        logger.info("Liaison créée - Solution: {} ({}), Ticket: {} ({})", 
                   solution.getId(), solution.getTitre(), ticket.getId(), ticket.getReference());
        
        return true;
    }
    
    public Optional<Ticket> trouverTicketParIdExterne(String idExterne) {
        return ticketRepository.findByIdExterneCrm(Integer.valueOf(idExterne));
    }
    
    public Optional<Solution> trouverSolutionParIdExterne(String idExterne) {
        return solutionRepository.findByIdExterneCrm(idExterne);
    }
}