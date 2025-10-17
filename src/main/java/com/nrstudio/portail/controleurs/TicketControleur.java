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
  public List<Ticket> lister() { 
    System.out.println("OK");
    return repo.findAll(); 
  }

  //Ticket par utilisateur
  @GetMapping("/utilisateur/{utilisateurId}")
  public List<Ticket> listerParUtilisateur(@PathVariable("utilisateurId") Integer utilisateurId) {
    return service.listerTicketsUtilisateur(utilisateurId);
  }

  @GetMapping("/{id}")
  public Ticket obtenir(@PathVariable("id") Integer id) {
    System.out.println("Obtention du ticket avec l'id : " + id);
    return repo.findById(id).orElseThrow(); 
  }

  @PostMapping
  public Ticket creer(@RequestBody TicketCreationRequete req) { return service.creerEtSynchroniser(req); }

  @PutMapping("/{id}/statut")
  public Ticket changerStatut(@PathVariable("id") Integer id, @RequestBody java.util.Map<String, Object> requete) {
    Integer nouveauStatutId = (Integer) requete.get("statutId");
    Integer utilisateurId = (Integer) requete.get("utilisateurId");
    return service.changerStatut(id, nouveauStatutId, utilisateurId);
  }

  @GetMapping("/company/{companyId}")
  public List<Ticket> listerParCompany(@PathVariable("companyId") Integer companyId) {
    return service.listerTicketsCompany(companyId);
  }

  @GetMapping("/consultant/{consultantId}")
  public List<Ticket> listerParConsultant(@PathVariable("consultantId") Integer consultantId) {
    return service.listerTicketsConsultant(consultantId);
  }
}
