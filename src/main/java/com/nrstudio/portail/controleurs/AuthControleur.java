package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.ConnexionReponse;
import com.nrstudio.portail.dto.ConnexionRequete;
import com.nrstudio.portail.securite.JwtSimple;
import com.nrstudio.portail.services.UtilisateurService;
import com.nrstudio.portail.services.ValidationCodeService; // Nouveau service
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.ValidationCode; // Nouvelle entity
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthControleur {

    private final UtilisateurService utilisateurs;
    private final CompanyRepository companyRepository;
    private final JwtSimple jwt;
    private final ValidationCodeService validationCodeService; // Nouveau service

    public AuthControleur(UtilisateurService utilisateurs, 
                         JwtSimple jwt, 
                         CompanyRepository companyRepository,
                         ValidationCodeService validationCodeService) { // Injection
        this.utilisateurs = utilisateurs;
        this.jwt = jwt;
        this.companyRepository = companyRepository;
        this.validationCodeService = validationCodeService;
    }

    @PostMapping("/connexion")
    public ResponseEntity<?> connexion(@RequestBody ConnexionRequete req) {
        System.out.println(req.getEmail());
        System.out.println(req.getMotDePasse());
        Optional<Utilisateur> opt = utilisateurs.trouverParEmail(req.getEmail());
        System.out.println(opt);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
        }

        Utilisateur u = opt.get();
        String stocke = (u.getMotDePasseHash() == null) ? null
                : new String(u.getMotDePasseHash(), StandardCharsets.UTF_8);

        // Récupérer companyName - Version avec gestion d'erreur
        String companyName = "Société non trouvée";
        if (u.getCompanyId() != null) {
            try {
                companyName = companyRepository.findNomById(u.getCompanyId());
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de la company: " + e.getMessage());
                companyName = "Erreur société";
            }
        }
        
        System.out.println("Company Name: " + companyName);
        
        if (stocke != null && BCrypt.checkpw(req.getMotDePasse(), stocke)) {
            
            // ✅ GÉNÉRATION DU CODE DE VALIDATION
            ValidationCode codeGenere = validationCodeService.generateCode(u.getId().toString());
            
            // TODO: Envoyer le code par email/SMS ici
            System.out.println("Code de validation généré pour " + u.getEmail() + ": " + codeGenere.getCode());
            
            // Pour l'instant, on retourne le code dans la réponse (à supprimer en production)
            String jeton = jwt.generer(u.getEmail());
            return ResponseEntity.ok(
                new ConnexionReponse(jeton, u.getEmail(), u.getId(), u.getNom(), u.getCompanyId(), companyName, codeGenere.getCode())
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
    }
}