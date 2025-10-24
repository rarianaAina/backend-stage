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

  // @PostMapping
  // public Utilisateur creer(@RequestBody UtilisateurCreationRequete req) {  // ← bien @RequestBody
  //   return service.creer(req);
  // }

  @GetMapping("/create")
  public Utilisateur creerUtilisateur(
      @RequestParam("identifiant") String identifiant,
      @RequestParam("nom") String nom,
      @RequestParam("prenom") String prenom,
      @RequestParam("email") String email,
      @RequestParam("motDePasse") String motDePasse) {

    // Création de l'UtilisateurCreationRequete avec les paramètres de l'URL
    UtilisateurCreationRequete req = new UtilisateurCreationRequete();
    req.setIdentifiant(identifiant);
    req.setNom(nom);
    req.setPrenom(prenom);
    req.setEmail(email);
    //req.setActif(actif);
    req.setMotDePasse(motDePasse);
    req.setCompanyId(896); // Valeur par défaut ou à modifier selon le besoin
    req.setTelephone("0340626129"); // Valeur par défaut ou à modifier selon le besoin
    req.setActif(1);    // Appel au service pour créer l'utilisateur
    req.setIdExterneCrm("12345"); // Valeur par défaut ou à modifier selon le besoin
    //Date de création de l'utilisateur
    req.setDateCreation(java.time.LocalDateTime.now());
    req.setDateMiseAJour(java.time.LocalDateTime.now());
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

