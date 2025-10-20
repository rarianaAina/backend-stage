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
    System.out.println("Listing tickets for user id: " + utilisateurId);
    List<Ticket> tickets = service.listerTicketsUtilisateur(utilisateurId);
    //Debug
    System.out.println("Found " + tickets.size() + " tickets for user id: " + utilisateurId);
    return tickets;
  }

  //Ticket par utilisateur avec pagination
  @GetMapping("/utilisateur/{utilisateurId}/page/{page}/size/{size}")
  public List<Ticket> listerParUtilisateurAvecPagination(
      @PathVariable("utilisateurId") Integer utilisateurId,
      @PathVariable("page") Integer page,
      @PathVariable("size") Integer size) {
    System.out.println("Listing tickets for user id: " + utilisateurId + " page: " + page + " size: " + size);
    List<Ticket> tickets = service.listerTicketsUtilisateurAvecPagination(utilisateurId, page, size);
    //Debug
    System.out.println("Found " + tickets.size() + " tickets for user id: " + utilisateurId);
    return tickets;
  }

  // Obtenir un ticket spécifique
  @GetMapping("/{id}")
  public Ticket obtenir(@PathVariable("id") Integer id) {
    System.out.println("Obtention du ticket avec l'id : " + id);
    return repo.findById(id).orElseThrow(); 
  }

  //Créer un ticket
  @PostMapping
  public Ticket creer(@RequestBody TicketCreationRequete req) { return service.creerEtSynchroniser(req); }


  //Changer le statut d'un ticket
  @PutMapping("/{id}/statut")
  public Ticket changerStatut(@PathVariable("id") Integer id, @RequestBody java.util.Map<String, Object> requete) {
    Integer nouveauStatutId = (Integer) requete.get("statutId");
    Integer utilisateurId = (Integer) requete.get("utilisateurId");
    return service.changerStatut(id, nouveauStatutId, utilisateurId);
  }

  //Lister les tickets par company
  @GetMapping("/company/{companyId}")
  public List<Ticket> listerParCompany(@PathVariable("companyId") Integer companyId) {
    return service.listerTicketsCompany(companyId);
  }

  //Lister les tickets par consultant
  @GetMapping("/consultant/{consultantId}")
  public List<Ticket> listerParConsultant(@PathVariable("consultantId") Integer consultantId) {
    return service.listerTicketsConsultant(consultantId);
  }
}
