package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.dto.CreditHoraireDto;
import com.nrstudio.portail.services.CreditHoraireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/creditsHoraires")
public class CreditHoraireControleur {

    private final CreditHoraireService creditHoraireService;
    private final ProduitRepository produitRepository;

    public CreditHoraireControleur(CreditHoraireService creditHoraireService, ProduitRepository produitRepository) {
        this.creditHoraireService = creditHoraireService;
        this.produitRepository = produitRepository;
    }

    /**
     * Obtenir tous les crédits horaires d'une company
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CreditHoraireDto>> getCreditsParCompany(
            @PathVariable("companyId") Integer companyId) {
        try {
            List<CreditHoraireDto> credits = creditHoraireService.getCreditsParCompany(companyId);
            return ResponseEntity.ok(credits);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtenir les crédits horaires d'une company pour un produit spécifique
     */
    // @GetMapping("/company/{companyId}/produit/{produitId}")
    // public ResponseEntity<List<CreditHoraireDto>> getCreditsParCompanyEtProduit(
    //         @PathVariable("companyId") Integer companyId,
    //         @PathVariable("produitId") Integer produitId) {
    //     try {
    //         List<CreditHoraireDto> credits = creditHoraireService.getCreditsParCompanyEtProduit(companyId, produitId);
    //         return ResponseEntity.ok(credits);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(null);
    //     }
    // }

    @GetMapping("/company/{companyId}/produit/{produitIdExterne}")
    public ResponseEntity<List<CreditHoraireDto>> getCreditsParCompanyEtProduit(
            @PathVariable("companyId") Integer companyId,
            @PathVariable("produitIdExterne") Integer produitIdExterne) {  // Renommez pour clarifier
        // trouver l'id interne via l'idexterne à l'aide du produitrepository mais pas du service
        Integer idProduitInterne = produitRepository.findByIdExterneCrm(String.valueOf(produitIdExterne))
            .orElseThrow(() -> new RuntimeException("Produit non trouvé avec l'ID externe: " + produitIdExterne))
            .getId();
        

        try {
            List<CreditHoraireDto> credits = creditHoraireService.getCreditsParCompanyEtProduit(companyId, idProduitInterne);
            return ResponseEntity.ok(credits);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtenir tous les crédits actifs d'une company
     */
    @GetMapping("/company/{companyId}/actifs")
    public ResponseEntity<List<CreditHoraireDto>> getCreditsActifs(
            @PathVariable Integer companyId) {
        try {
            List<CreditHoraireDto> credits = creditHoraireService.getCreditsActifs(companyId);
            return ResponseEntity.ok(credits);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtenir les crédits actifs d'une company pour un produit spécifique
     */
    @GetMapping("/company/{companyId}/produit/{produitId}/actifs")
    public ResponseEntity<List<CreditHoraireDto>> getCreditsActifsPourProduit(
            @PathVariable Integer companyId,
            @PathVariable Integer produitId) {
        try {
            List<CreditHoraireDto> credits = creditHoraireService.getCreditsActifsPourProduit(companyId, produitId);
            return ResponseEntity.ok(credits);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Consommer des heures pour un produit
     */
    @PostMapping("/company/{companyId}/produit/{produitId}/consommer")
    public ResponseEntity<?> consommerHeures(
            @PathVariable Integer companyId,
            @PathVariable Integer produitId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer heures = request.get("heures");
            if (heures == null || heures <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Le nombre d'heures doit être positif"
                ));
            }

            var creditMisAJour = creditHoraireService.consommerHeures(companyId, produitId, heures);
            
            return ResponseEntity.ok(Map.of(
                "message", String.format("%d heures consommées avec succès", heures),
                "creditId", creditMisAJour.getId(),
                "heuresRestantes", creditMisAJour.getHeuresRestantes(),
                "heuresConsommees", creditMisAJour.getHeuresConsommees()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Obtenir le total des heures restantes pour un produit
     */
    @GetMapping("/company/{companyId}/produit/{produitId}/heures-restantes")
    public ResponseEntity<Map<String, Object>> getHeuresRestantesPourProduit(
            @PathVariable Integer companyId,
            @PathVariable Integer produitId) {
        try {
            Integer heuresRestantes = creditHoraireService.getHeuresRestantesPourProduit(companyId, produitId);
            
            return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "produitId", produitId,
                "heuresRestantes", heuresRestantes
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Obtenir le total des heures restantes pour tous les produits d'une company
     */
    @GetMapping("/company/{companyId}/heures-restantes-total")
    public ResponseEntity<Map<String, Object>> getHeuresRestantesTotal(
            @PathVariable Integer companyId) {
        try {
            Integer heuresRestantesTotal = creditHoraireService.getHeuresRestantesTotal(companyId);
            
            return ResponseEntity.ok(Map.of(
                "companyId", companyId,
                "heuresRestantesTotal", heuresRestantesTotal
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Endpoint administratif - Désactiver les crédits expirés
     */
    @PostMapping("/admin/desactiver-expires")
    public ResponseEntity<Map<String, Object>> desactiverCreditsExpires() {
        try {
            creditHoraireService.desactiverCreditsExpires();
            
            return ResponseEntity.ok(Map.of(
                "message", "Crédits expirés désactivés avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Erreur lors de la désactivation des crédits expirés: " + e.getMessage()
            ));
        }
    }
}