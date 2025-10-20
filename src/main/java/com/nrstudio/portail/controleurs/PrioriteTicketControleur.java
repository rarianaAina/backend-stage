package com.nrstudio.portail.controleurs;
import com.nrstudio.portail.depots.PrioriteTicketRepository;
import com.nrstudio.portail.domaine.PrioriteTicket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prioriteTickets")
@CrossOrigin
public class PrioriteTicketControleur {
    
    @Autowired
    private PrioriteTicketRepository prioriteTicketRepository;

    @GetMapping
    public List<PrioriteTicket> lister() {
        prioriteTicketRepository.findAll();
        return prioriteTicketRepository.findAll();
    }
}
