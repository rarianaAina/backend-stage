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

    @GetMapping(produces = "application/json")
    public List<PrioriteTicket> lister() {
        List<PrioriteTicket> priorites = prioriteTicketRepository.findAll();
        System.out.println("Liste des priorités de ticket: " + priorites.size() + " trouvées.");

        for (PrioriteTicket p : priorites) {
            System.out.println(p);
        }

        return prioriteTicketRepository.findAll();
    }
}
