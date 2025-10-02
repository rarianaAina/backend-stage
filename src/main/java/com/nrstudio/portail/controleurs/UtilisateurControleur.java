package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.services.UtilisateurService;
import com.nrstudio.portail.dto.UtilisateurCreationRequete;
import com.nrstudio.portail.dto.UtilisateurMiseAJourRequete;
import com.nrstudio.portail.dto.MotDePasseRequete;
import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin
public class UtilisateurControleur {
  private final UtilisateurService service;
  public UtilisateurControleur(UtilisateurService service) { this.service = service; }

  @GetMapping
  public List<Utilisateur> lister() { return service.lister(); }

  @GetMapping("/{id}")
  public Utilisateur obtenir(@PathVariable("id") Integer id) {   // ← nom explicite
    return service.obtenir(id);
  }

  @PostMapping
  public Utilisateur creer(@RequestBody UtilisateurCreationRequete req) {  // ← bien @RequestBody
    return service.creer(req);
  }

  @PutMapping("/{id}")
  public Utilisateur mettreAJour(@PathVariable("id") Integer id,           // ← nom explicite
                                 @RequestBody UtilisateurMiseAJourRequete req) {
    return service.mettreAJour(id, req);
  }

  @PostMapping("/{id}/mot-de-passe")
  public void definirMotDePasse(@PathVariable("id") Integer id,            // ← nom explicite
                                @RequestBody MotDePasseRequete req) {
    service.definirMotDePasse(id, req);
  }
}

