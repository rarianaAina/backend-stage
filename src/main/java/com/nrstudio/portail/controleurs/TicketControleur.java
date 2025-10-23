package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.dto.TicketAvecProduitPageReponse;
import com.nrstudio.portail.dto.TicketCreationRequete;
import com.nrstudio.portail.dto.TicketPageReponse;
import com.nrstudio.portail.services.TicketService;

import org.springframework.http.MediaType;
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

  // @GetMapping("/utilisateur/{utilisateurId}/page/{page}/size/{size}")
  // public TicketPageReponse listerParUtilisateurAvecPagination(
  //     @PathVariable("utilisateurId") Integer utilisateurId,
  //     @PathVariable("page") Integer page,
  //     @PathVariable("size") Integer size,
  //     @RequestParam(value = "etat", required = false) String etat,
  //     @RequestParam(value = "reference", required = false) String reference,
  //     @RequestParam(value = "produit", required = false) String produit,
  //     @RequestParam(value = "dateDebut", required = false) String dateDebut,
  //     @RequestParam(value = "dateFin", required = false) String dateFin) {

  //   List<Ticket> tickets = service.listerTicketsUtilisateurAvecPaginationEtFiltres(
  //       utilisateurId, page, size, etat, reference, produit, dateDebut, dateFin);

  //   System.out.println(tickets.toString());
  //   Long totalElements = service.countTicketsUtilisateurAvecFiltres(
  //       utilisateurId, etat, reference, produit, dateDebut, dateFin);
  //   int totalPages = (int) Math.ceil((double) totalElements / size);
    
  //   System.out.println("Total elements: " + totalElements);
  //   System.out.println("Total pages: " + totalPages);

  //   TicketPageReponse response = new TicketPageReponse();
  //   response.setTickets(tickets);
  //   response.setCurrentPage(page);
  //   response.setTotalPages(totalPages);
  //   response.setTotalElements(totalElements);
  //   response.setPageSize(size);

  //   return response;
  // }

  @GetMapping("/utilisateur/{utilisateurId}/page/{page}/size/{size}")
  public TicketAvecProduitPageReponse listerParUtilisateurAvecPagination(
      @PathVariable("utilisateurId") Integer utilisateurId,
      @PathVariable("page") Integer page,
      @PathVariable("size") Integer size,
      @RequestParam(value = "etat", required = false) String etat,
      @RequestParam(value = "reference", required = false) String reference,
      @RequestParam(value = "produit", required = false) String produit,
      @RequestParam(value = "dateDebut", required = false) String dateDebut,
      @RequestParam(value = "dateFin", required = false) String dateFin) {

      TicketAvecProduitPageReponse response = service.listerTicketsUtilisateurAvecPaginationEtFiltres(
          utilisateurId, page, size, etat, reference, produit, dateDebut, dateFin);

      System.out.println("Total elements: " + response.getTotalElements());
      System.out.println("Total pages: " + response.getTotalPages());

      return response;
  }

  @GetMapping("/{id}")
  public Ticket obtenir(@PathVariable("id") Integer id) {
    System.out.println("Obtention du ticket avec l'id : " + id);
    return repo.findById(id).orElseThrow(); 
  }

  // @PostMapping
  // public String creer(@RequestBody TicketCreationRequete req) {
  //   System.out.println(req);
  //   return "OK";
  //   //return service.creerEtSynchroniser(req); 
  // }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Ticket creer(@ModelAttribute TicketCreationRequete req) {
      System.out.println("Raison: " + req.getRaison());
      System.out.println("Logiciel: " + req.getProduitId());
      System.out.println("Type: " + req.getTypeTicketId());
      System.out.println("Description: " + req.getDescription());
      System.out.println("Niveau: " + req.getPrioriteTicketId());
      System.out.println("Company: " + req.getCompanyId());
      System.out.println("Utilisateur: " + req.getClientId());
      
      if (req.getFichiers() != null) {
          System.out.println("Nombre de fichiers: " + req.getFichiers().size());
      }
      
      return service.creerEtSynchroniser(req);
  }

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
