package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
  Optional<Ticket> findByIdExterneCrm(Integer idExterneCrm);
  List<Ticket> findByCompanyId(Integer companyId);
  List<Ticket> findByAffecteAUtilisateurId(Integer utilisateurId);
  
    // Méthode pour filtrer par période
    @Query("SELECT t FROM Ticket t WHERE t.dateCreation BETWEEN :debut AND :fin")
    List<Ticket> findByDateCreationBetween(@Param("debut") LocalDateTime debut, 
                                          @Param("fin") LocalDateTime fin);

    Optional<Ticket> findByReferenceId(String referenceId);

}
