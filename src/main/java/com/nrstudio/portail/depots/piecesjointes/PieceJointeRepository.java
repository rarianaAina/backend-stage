package com.nrstudio.portail.depots.piecesjointes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nrstudio.portail.domaine.PieceJointe;

@Repository
public interface PieceJointeRepository extends JpaRepository<PieceJointe, Integer> {
    List<PieceJointe> findByTicketId(Integer ticketId);
    List<PieceJointe> findByInterventionId(Integer interventionId);
    List<PieceJointe> findByInteractionId(Integer interactionId);
    Optional<PieceJointe> findByIdExterneCrm(String idExterneCrm);
}
