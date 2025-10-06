package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Integer> {
  List<Intervention> findByTicketId(Integer ticketId);
  List<Intervention> findByCreeParUtilisateurId(Integer utilisateurId);
  Optional<Intervention> findByIdExterneCrm(Integer idExterneCrm);
}
