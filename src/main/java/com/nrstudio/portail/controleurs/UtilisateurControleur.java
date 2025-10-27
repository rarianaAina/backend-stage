package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.services.UtilisateurService;
import com.nrstudio.portail.dto.UtilisateurCreationRequete;
import com.nrstudio.portail.dto.UtilisateurMiseAJourRequete;
import com.nrstudio.portail.dto.MotDePasseRequete;
import com.nrstudio.portail.dto.UtilisateurPageReponse;
import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@CrossOrigin
public class UtilisateurControleur {
    private final UtilisateurService service;
    
    public UtilisateurControleur(UtilisateurService service) { 
        this.service = service; 
    }

    @GetMapping
    public List<Utilisateur> lister() { 
        return service.lister(); 
    }

    @GetMapping("/recherche")
    public UtilisateurPageReponse rechercherUtilisateurs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "recherche", required = false) String recherche,
            @RequestParam(value = "actif", required = false) String actif,
            @RequestParam(value = "dateDebut", required = false) String dateDebut,
            @RequestParam(value = "dateFin", required = false) String dateFin) {
        
        return service.listerUtilisateursAvecPaginationEtFiltres(
            page, size, recherche, actif, dateDebut, dateFin);
    }

    @GetMapping("/{id}")
    public Utilisateur obtenir(@PathVariable("id") Integer id) {
        return service.obtenir(id);
    }

    @GetMapping("/create")
    public Utilisateur creerUtilisateur(
            @RequestParam("identifiant") String identifiant,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("motDePasse") String motDePasse) {

        UtilisateurCreationRequete req = new UtilisateurCreationRequete();
        req.setIdentifiant(identifiant);
        req.setNom(nom);
        req.setPrenom(prenom);
        req.setEmail(email);
        req.setMotDePasse(motDePasse);
        req.setCompanyId(896);
        req.setTelephone("0340626129");
        req.setActif(1);
        req.setIdExterneCrm("12345");
        req.setDateCreation(java.time.LocalDateTime.now());
        req.setDateMiseAJour(java.time.LocalDateTime.now());
        
        return service.creer(req);
    }

    @PutMapping("/{id}")
    public Utilisateur mettreAJour(@PathVariable("id") Integer id,
                                 @RequestBody UtilisateurMiseAJourRequete req) {
        return service.mettreAJour(id, req);
    }

    @PostMapping("/{id}/mot-de-passe")
    public void definirMotDePasse(@PathVariable("id") Integer id,
                                @RequestBody MotDePasseRequete req) {
        service.definirMotDePasse(id, req);
    }
}