package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.InterventionRepository;
import com.nrstudio.portail.domaine.Intervention;
import com.nrstudio.portail.services.InterventionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interventions")
@CrossOrigin
public class InterventionControleur {

  private final InterventionRepository repo;
  private final InterventionService service;

  public InterventionControleur(InterventionRepository repo, InterventionService service) {
    this.repo = repo;
    this.service = service;
  }

  @GetMapping
  public List<Intervention> lister() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public Intervention obtenir(@PathVariable("id") Integer id) {
    return repo.findById(id).orElseThrow();
  }

  @GetMapping("/ticket/{ticketId}")
  public List<Intervention> listerParTicket(@PathVariable("ticketId") Integer ticketId) {
    return service.listerInterventionsTicket(ticketId);
  }

  @GetMapping("/consultant/{consultantId}")
  public List<Intervention> listerParConsultant(@PathVariable("consultantId") Integer consultantId) {
    return service.listerInterventionsConsultant(consultantId);
  }

  @PostMapping
  public Intervention creer(@RequestBody Map<String, Object> requete) {
    Integer ticketId = (Integer) requete.get("ticketId");
    String raison = (String) requete.get("raison");
    String dateStr = (String) requete.get("dateIntervention");
    String typeIntervention = (String) requete.get("typeIntervention");
    Integer consultantId = (Integer) requete.get("consultantId");

    LocalDateTime dateIntervention = LocalDateTime.parse(dateStr);

    return service.creerIntervention(ticketId, raison, dateIntervention, typeIntervention, consultantId);
  }

  @PutMapping("/{id}/valider-date")
  public Intervention validerDate(@PathVariable("id") Integer id, @RequestBody Map<String, Object> requete) {
    Integer utilisateurId = (Integer) requete.get("utilisateurId");
    Boolean estClient = (Boolean) requete.get("estClient");

    return service.validerDate(id, utilisateurId, estClient != null && estClient);
  }

  @PutMapping("/{id}/proposer-date")
  public Intervention proposerNouvelleDate(@PathVariable("id") Integer id, @RequestBody Map<String, Object> requete) {
    String dateStr = (String) requete.get("nouvelleDate");
    Integer utilisateurId = (Integer) requete.get("utilisateurId");
    Boolean estClient = (Boolean) requete.get("estClient");

    LocalDateTime nouvelleDate = LocalDateTime.parse(dateStr);

    return service.proposerNouvelleDate(id, nouvelleDate, utilisateurId, estClient != null && estClient);
  }

  @PutMapping("/{id}/cloturer")
  public Intervention cloturer(@PathVariable("id") Integer id, @RequestBody Map<String, Object> requete) {
    String ficheIntervention = (String) requete.get("ficheIntervention");
    Integer consultantId = (Integer) requete.get("consultantId");

    return service.cloturerIntervention(id, ficheIntervention, consultantId);
  }
}
