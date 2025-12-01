package com.nrstudio.portail.controleurs.whatsapp;

import com.nrstudio.portail.dto.whatsapp.ConfigurationWhatsAppDTO;
import com.nrstudio.portail.services.whatsapp.WhatsAppConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/configurations/whatsapp")
@CrossOrigin(origins = "*")
public class WhatsappConfigControleur {
    
    private final WhatsAppConfigService whatsAppConfigService;
    
    public WhatsappConfigControleur(WhatsAppConfigService whatsAppConfigService) {
        this.whatsAppConfigService = whatsAppConfigService;
    }
    
    // GET - Récupérer toutes les configurations
    @GetMapping
    public ResponseEntity<List<ConfigurationWhatsAppDTO>> getAllConfigurations() {
        try {
            List<ConfigurationWhatsAppDTO> configurations = whatsAppConfigService.getAllConfigurations();
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // GET - Récupérer une configuration par ID
    @GetMapping("/{id}")
    public ResponseEntity<ConfigurationWhatsAppDTO> getConfigurationById(@PathVariable Integer id) {
        try {
            Optional<ConfigurationWhatsAppDTO> config = whatsAppConfigService.getConfigurationById(id);
            return config.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // GET - Récupérer la configuration active
    @GetMapping("/active")
    public ResponseEntity<ConfigurationWhatsAppDTO> getActiveConfiguration() {
        try {
            Optional<ConfigurationWhatsAppDTO> config = whatsAppConfigService.getActiveConfiguration();
            return config.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // POST - Créer ou mettre à jour une configuration
    @PostMapping
    public ResponseEntity<?> saveConfiguration(@RequestBody ConfigurationWhatsAppDTO configDTO) {
        try {
            ConfigurationWhatsAppDTO savedConfig = whatsAppConfigService.saveConfiguration(configDTO);
            return ResponseEntity.ok(savedConfig);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    // PUT - Activer une configuration
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateConfiguration(@PathVariable Integer id) {
        try {
            boolean success = whatsAppConfigService.activateConfiguration(id);
            if (success) {
                return ResponseEntity.ok().body("Configuration activée avec succès");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors de l'activation: " + e.getMessage());
        }
    }
    
    // PUT - Désactiver une configuration
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateConfiguration(@PathVariable Integer id) {
        try {
            boolean success = whatsAppConfigService.deactivateConfiguration(id);
            if (success) {
                return ResponseEntity.ok().body("Configuration désactivée avec succès");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors de la désactivation: " + e.getMessage());
        }
    }
    
    // POST - Tester une configuration
    @PostMapping("/{id}/test")
    public ResponseEntity<?> testConfiguration(@PathVariable Integer id) {
        try {
            boolean testSuccess = whatsAppConfigService.testConfiguration(id);
            if (testSuccess) {
                return ResponseEntity.ok().body("Test de configuration WhatsApp réussi");
            } else {
                return ResponseEntity.badRequest().body("Test de configuration WhatsApp échoué");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors du test: " + e.getMessage());
        }
    }
    
    // DELETE - Supprimer une configuration
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConfiguration(@PathVariable Integer id) {
        try {
            boolean success = whatsAppConfigService.deleteConfiguration(id);
            if (success) {
                return ResponseEntity.ok().body("Configuration supprimée avec succès");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}