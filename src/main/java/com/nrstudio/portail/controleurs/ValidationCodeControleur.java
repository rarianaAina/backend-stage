package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.CodeValidationRequest;
import com.nrstudio.portail.dto.CodeValidationResponse;
import com.nrstudio.portail.services.ValidationCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class ValidationCodeControleur {

    private final ValidationCodeService validationCodeService;

    public ValidationCodeControleur(ValidationCodeService validationCodeService) {
        this.validationCodeService = validationCodeService;
    }

    @PostMapping("/valider-code")
    public ResponseEntity<CodeValidationResponse> validerCode(@RequestBody CodeValidationRequest request) {
        System.out.println("Appel de la validation de code");
        System.out.println("Utilisateur ID: " + request.getUtilisateurId());
        System.out.println("Code fourni: " + request.getCode());
        boolean isValid = validationCodeService.validateCode(request.getUtilisateurId(), request.getCode());
        
        CodeValidationResponse response = new CodeValidationResponse(
            isValid,
            isValid ? "Code valide" : "Code invalide ou expiré",
            null
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/regenerer-code")
    public ResponseEntity<CodeValidationResponse> regenererCode(@RequestParam("utilisateurId") String utilisateurId) {
        try {
            var codeGenere = validationCodeService.generateCode(utilisateurId, "");
            
            // TODO: Envoyer le code par email/SMS
            System.out.println("Nouveau code généré: " + codeGenere.getCode());
            
            CodeValidationResponse response = new CodeValidationResponse(
                true,
                "Nouveau code généré avec succès",
                codeGenere.getCode() // À supprimer en production
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CodeValidationResponse response = new CodeValidationResponse(
                false,
                "Erreur lors de la génération du code",
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}