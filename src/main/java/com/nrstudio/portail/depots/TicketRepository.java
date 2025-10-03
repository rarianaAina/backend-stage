package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
  Optional<Ticket> findByIdExterneCrm(Integer idExterneCrm);
}
