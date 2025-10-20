package com.nrstudio.portail.depots;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nrstudio.portail.domaine.PrioriteTicket;

public interface PrioriteTicketRepository extends JpaRepository<PrioriteTicket, Integer> {
    
}
