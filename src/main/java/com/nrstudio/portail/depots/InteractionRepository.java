package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Integer> {
  List<Interaction> findByTicketIdOrderByDateCreationDesc(Integer ticketId);
  List<Interaction> findByInterventionIdOrderByDateCreationDesc(Integer interventionId);
  List<Interaction> findByTicketId(Integer ticketId);
}
