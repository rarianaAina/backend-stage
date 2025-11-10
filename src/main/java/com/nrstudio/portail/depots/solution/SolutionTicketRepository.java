package com.nrstudio.portail.depots.solution;

import com.nrstudio.portail.domaine.solution.SolutionTicket;
import com.nrstudio.portail.domaine.solution.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolutionTicketRepository extends JpaRepository<SolutionTicket, Long> {
    
    // Trouver toutes les liaisons pour un ticket
    @Query("SELECT st FROM SolutionTicket st WHERE st.ticket.id = :ticketId")
    List<SolutionTicket> findByTicketId(@Param("ticketId") Integer ticketId);
    
    // Trouver toutes les liaisons pour une solution
    @Query("SELECT st FROM SolutionTicket st WHERE st.solution.id = :solutionId")
    List<SolutionTicket> findBySolutionId(@Param("solutionId") Integer solutionId);
    
    // Vérifier si une liaison existe
    boolean existsBySolutionIdAndTicketId(Integer solutionId, Integer ticketId);
    
    // Trouver les solutions d'un ticket avec jointure
    @Query("SELECT st.solution FROM SolutionTicket st WHERE st.ticket.id = :ticketId")
    List<Solution> findSolutionsByTicketId(@Param("ticketId") Integer ticketId);
}