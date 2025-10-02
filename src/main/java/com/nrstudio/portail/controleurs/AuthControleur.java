package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.ConnexionReponse;
import com.nrstudio.portail.dto.ConnexionRequete;
import com.nrstudio.portail.securite.JwtSimple;
import com.nrstudio.portail.services.UtilisateurService;
import com.nrstudio.portail.domaine.Utilisateur;
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
  private final JwtSimple jwt;

  public AuthControleur(UtilisateurService utilisateurs, JwtSimple jwt) {
    this.utilisateurs = utilisateurs;
    this.jwt = jwt;
  }

  @PostMapping("/connexion")
  public ResponseEntity<?> connexion(@RequestBody ConnexionRequete req) {
    System.out.println("Tato");
    Optional<Utilisateur> opt = utilisateurs.trouverParIdentifiant(req.getIdentifiant());
    if (opt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
    }

    Utilisateur u = opt.get();
    String stocke = (u.getMotDePasseHash() == null) ? null
                    : new String(u.getMotDePasseHash(), StandardCharsets.UTF_8);

    if (stocke != null && BCrypt.checkpw(req.getMotDePasse(), stocke)) {
      String jeton = jwt.generer(u.getIdentifiant());
      return ResponseEntity.ok(new ConnexionReponse(jeton, u.getIdentifiant()));
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
  }
}
