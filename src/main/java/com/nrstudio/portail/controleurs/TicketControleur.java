package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.dto.TicketAvecDetails;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.dto.TicketAvecProduitPageReponse;
import com.nrstudio.portail.dto.TicketCreationRequete;
import com.nrstudio.portail.dto.TicketPageReponse;
import com.nrstudio.portail.services.TicketService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin
public class TicketControleur {

  private final TicketRepository repo;
  private final TicketService service;

  @Autowired
  private ProduitRepository produitRepository;

  public TicketControleur(TicketRepository repo, TicketService service) {
    this.repo = repo;
    this.service = service;
  }

  @GetMapping
  public List<Ticket> lister() { 
    System.out.println("OK");
    return repo.findAll();
  }

  // Endpoint pour l'admin avec pagination et filtres
@GetMapping("/admin")
public TicketAvecProduitPageReponse listerTicketsAdmin(
    @RequestParam(value = "page", defaultValue = "0") int page,
    @RequestParam(value = "size", defaultValue = "10") int size,
    @RequestParam(value = "etat", required = false) String etat,
    @RequestParam(value = "reference", required = false) String reference,
    @RequestParam(value = "produit", required = false) String produit,
    @RequestParam(value = "dateDebut", required = false) String dateDebut,
    @RequestParam(value = "dateFin", required = false) String dateFin,
    @RequestParam(value = "societe", required = false) String societe,
    @RequestParam(value = "priorite", required = false) String priorite) {

    System.out.println("Récupération des tickets admin avec filtres:");
    
    TicketAvecProduitPageReponse response = service.listerTicketsAdminAvecPaginationEtFiltres(
        page, size, etat, reference, produit, dateDebut, dateFin, societe, priorite);

    System.out.println("Total éléments: " + response.getTotalElements());
    System.out.println("Total pages: " + response.getTotalPages());
    System.out.println("Tickets retournés: " + response.getTickets().size());

    return response;
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

  // Obtenir un ticket spécifique
  @GetMapping("/{id}")
  public TicketAvecDetails obtenir(@PathVariable("id") Integer id) {
      System.out.println("Obtention du ticket avec l'id : " + id);
      
      Ticket ticket = repo.findById(id).orElseThrow();
      
      // Créez un DTO avec les informations jointes
      TicketAvecDetails ticketAvecDetails = new TicketAvecDetails();
      ticketAvecDetails.setId(ticket.getId());
      ticketAvecDetails.setReference(ticket.getReference());
      ticketAvecDetails.setTitre(ticket.getTitre());
      ticketAvecDetails.setDescription(ticket.getDescription());
      ticketAvecDetails.setDateCreation(ticket.getDateCreation());
      ticketAvecDetails.setDateCloture(ticket.getDateCloture());
      ticketAvecDetails.setProduitId(ticket.getProduitId());
      ticketAvecDetails.setPrioriteTicketId(ticket.getPrioriteTicketId());
      ticketAvecDetails.setStatutTicketId(ticket.getStatutTicketId());
      ticketAvecDetails.setTypeTicketId(ticket.getTypeTicketId());
      
      // Récupérez le nom du produit
      if (ticket.getProduitId() != null) {
          try {
              Produit produit = produitRepository.findById(ticket.getProduitId()).orElse(null);
              if (produit != null) {
                  ticketAvecDetails.setProduitNom(produit.getCodeProduit());
              }
          } catch (Exception e) {
              System.err.println("Erreur lors de la récupération du produit: " + e.getMessage());
          }
      }
      System.out.println("Ticket avec détails obtenu: " + ticketAvecDetails.getId());
      System.out.println("Produit ID : " + ticketAvecDetails.getProduitId());
      System.out.println("Produit Nom: " + ticketAvecDetails.getProduitNom());
      return ticketAvecDetails;
  }

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
