package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.InteractionRepository;
import com.nrstudio.portail.domaine.Interaction;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin
public class InteractionControleur {

  private final InteractionRepository repo;

  public InteractionControleur(InteractionRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Interaction> lister() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public Interaction obtenir(@PathVariable("id") Integer id) {
    return repo.findById(id).orElseThrow();
  }

  @GetMapping("/ticket/{ticketId}")
  public List<Interaction> listerParTicket(@PathVariable("ticketId") Integer ticketId) {
    return repo.findByTicketIdOrderByDateCreationDesc(ticketId);
  }

  @GetMapping("/intervention/{interventionId}")
  public List<Interaction> listerParIntervention(@PathVariable("interventionId") Integer interventionId) {
    return repo.findByInterventionIdOrderByDateCreationDesc(interventionId);
  }

  @PostMapping
  public Interaction creer(@RequestBody Map<String, Object> requete) {
    Interaction interaction = new Interaction();
    interaction.setTicketId((Integer) requete.get("ticketId"));
    interaction.setInterventionId((Integer) requete.get("interventionId"));
    interaction.setMessage((String) requete.get("message"));
    interaction.setTypeInteraction((String) requete.get("typeInteraction"));
    interaction.setAuteurUtilisateurId((Integer) requete.get("auteurUtilisateurId"));
    interaction.setDateCreation(LocalDateTime.now());
    interaction.setVisibleClient((Boolean) requete.getOrDefault("visibleClient", true));

    return repo.save(interaction);
  }
}
