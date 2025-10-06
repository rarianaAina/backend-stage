package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.PieceJointe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Integer> {
  List<PieceJointe> findByTicketId(Integer ticketId);
  List<PieceJointe> findByInterventionId(Integer interventionId);
}
