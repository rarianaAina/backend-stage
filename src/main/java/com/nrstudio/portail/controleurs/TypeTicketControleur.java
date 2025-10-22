package com.nrstudio.portail.controleurs;
import com.nrstudio.portail.depots.TypeTicketRepository;
import com.nrstudio.portail.domaine.TypeTicket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/typeTickets")
@CrossOrigin
public class TypeTicketControleur {
    
    @Autowired
    private TypeTicketRepository typeTicketRepository;

    @GetMapping(produces = "application/json")
    public List<TypeTicket> lister() {
        List<TypeTicket> types = typeTicketRepository.findAll();
        System.out.println("Liste des types de ticket: " + types.size() + " trouvés.");

        for (TypeTicket t : types) {
            System.out.println(t);
        }

        return typeTicketRepository.findAll();
    }
}
