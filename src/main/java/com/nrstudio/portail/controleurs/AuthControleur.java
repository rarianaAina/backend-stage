package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.password.*;
import com.nrstudio.portail.dto.ConnexionRequete;
import com.nrstudio.portail.dto.ConnexionReponse;
import com.nrstudio.portail.securite.JwtSimple;
import com.nrstudio.portail.services.UtilisateurService;
import com.nrstudio.portail.services.ValidationCodeService;
import com.nrstudio.portail.services.notification.EmailNotificationService;
import com.nrstudio.portail.services.password.PasswordResetService;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.ValidationCode;
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
    private final ValidationCodeService validationCodeService;
    private final EmailNotificationService emailNotificationService;
    private final PasswordResetService passwordResetService;
    
    public AuthControleur(UtilisateurService utilisateurs, 
                         JwtSimple jwt, 
                         CompanyRepository companyRepository,
                         ValidationCodeService validationCodeService,
                         EmailNotificationService emailNotificationService,
                         PasswordResetService passwordResetService) {
        this.utilisateurs = utilisateurs;
        this.jwt = jwt;
        this.companyRepository = companyRepository;
        this.validationCodeService = validationCodeService;
        this.emailNotificationService = emailNotificationService;
        this.passwordResetService = passwordResetService;
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

        // Récupérer companyName
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
            System.out.println("=== GÉNÉRATION DU CODE ===");
            ValidationCode codeGenere = validationCodeService.generateCode(u.getId().toString(), u.getEmail());
            System.out.println("=== ENVOI EMAIL ===");
            System.out.println("=== FIN CONNEXION ===");
            System.out.println("Code de validation généré pour " + u.getEmail() + ": " + codeGenere.getCode());
            
            String jeton = jwt.generer(u.getEmail());
            return ResponseEntity.ok(
                new ConnexionReponse(jeton, u.getEmail(), u.getId(), u.getNom(), u.getCompanyId(), companyName, codeGenere.getCode())
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            boolean emailSent = passwordResetService.createPasswordResetToken(request.getEmail());
            
            if (emailSent) {
                // Pour des raisons de sécurité, on retourne toujours le même message
                return ResponseEntity.ok(new ApiResponse(true, 
                    "Si l'email existe dans notre système, un lien de réinitialisation a été envoyé."));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Erreur lors de l'envoi de l'email de réinitialisation"));
            }
        } catch (Exception e) {
            System.err.println("Erreur dans forgot-password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Erreur serveur lors de la demande de réinitialisation"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Validation des données
            if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Le mot de passe doit contenir au moins 6 caractères"));
            }
            
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Les mots de passe ne correspondent pas"));
            }
            
            // Réinitialisation du mot de passe
            boolean resetSuccess = passwordResetService.resetPassword(
                request.getToken(), 
                request.getNewPassword()
            );
            
            if (resetSuccess) {
                return ResponseEntity.ok(new ApiResponse(true, "Mot de passe réinitialisé avec succès"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lien de réinitialisation invalide ou expiré"));
            }
        } catch (Exception e) {
            System.err.println("Erreur dans reset-password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Erreur serveur lors de la réinitialisation"));
        }
    }

    @PostMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse> validateResetToken(@RequestBody ResetPasswordRequest request) {
        try {
            boolean isValid = passwordResetService.validatePasswordResetToken(request.getToken());
            
            if (isValid) {
                return ResponseEntity.ok(new ApiResponse(true, "Token valide"));
            } else {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Lien de réinitialisation invalide ou expiré"));
            }
        } catch (Exception e) {
            System.err.println("Erreur dans validate-reset-token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Erreur serveur lors de la validation du token"));
        }
    }
}