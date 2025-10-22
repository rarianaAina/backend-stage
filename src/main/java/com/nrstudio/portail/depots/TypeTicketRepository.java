package com.nrstudio.portail.depots;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nrstudio.portail.domaine.TypeTicket;

public interface TypeTicketRepository extends JpaRepository<TypeTicket, Integer> {
    
}