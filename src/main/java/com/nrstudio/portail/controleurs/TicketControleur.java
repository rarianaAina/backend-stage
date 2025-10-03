package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.dto.TicketCreationRequete;
import com.nrstudio.portail.services.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketControleur {

  private final TicketRepository repo;
  private final TicketService service;

  public TicketControleur(TicketRepository repo, TicketService service) {
    this.repo = repo;
    this.service = service;
  }

  @GetMapping
  public List<Ticket> lister() { return repo.findAll(); }

  @GetMapping("/{id}")
  public Ticket obtenir(@PathVariable("id") Integer id) { return repo.findById(id).orElseThrow(); }

  @PostMapping
  public Ticket creer(@RequestBody TicketCreationRequete req) { return service.creerEtSynchroniser(req); }
}
