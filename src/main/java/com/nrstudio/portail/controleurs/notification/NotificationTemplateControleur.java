package com.nrstudio.portail.controleurs.notification;

import com.nrstudio.portail.domaine.NotificationTemplate;
import com.nrstudio.portail.services.NotificationTemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification-templates")
@CrossOrigin(origins = "*")
public class NotificationTemplateControleur {

    @Autowired
    private  NotificationTemplateService templateService;



    /**
     * Récupérer tous les templates actifs
     */
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getAllTemplates() {
        try {
            List<NotificationTemplate> templates = templateService.getAllActiveTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer un template par son code
     */
    @GetMapping("/{code}")
    public ResponseEntity<NotificationTemplate> getTemplateByCode(@PathVariable("code") String code) {
        try {
            Optional<NotificationTemplate> template = templateService.getTemplateByCode(code);
            return template.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer un template par son ID
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<NotificationTemplate> getTemplateById(@PathVariable("id") Integer id) {
        try {
            Optional<NotificationTemplate> template = templateService.getTemplateById(id);
            return template.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Créer un nouveau template
     */
    @PostMapping
    public ResponseEntity<NotificationTemplate> createTemplate(@RequestBody NotificationTemplate template) {
        try {
            // Validation basique
            if (template.getCode() == null || template.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            NotificationTemplate savedTemplate = templateService.createTemplate(template);
            return ResponseEntity.ok(savedTemplate);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mettre à jour un template existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable("id") Integer id, 
            @RequestBody NotificationTemplate templateDetails) {
        try {
            System.out.println("🔄 Mise à jour du template ID: " + id);
            
            Optional<NotificationTemplate> existingTemplate = templateService.getTemplateById(id);
            if (existingTemplate.isEmpty()) {
                System.out.println("❌ Template non trouvé avec ID: " + id);
                return ResponseEntity.notFound().build();
            }

            NotificationTemplate updatedTemplate = templateService.updateTemplate(id, templateDetails);
            System.out.println("✅ Template mis à jour avec succès: " + updatedTemplate.getId());
            return ResponseEntity.ok(updatedTemplate);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du template ID: " + id);
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Activer/Désactiver un template
     */
    @PatchMapping("/{id}/toggle-activation")
    public ResponseEntity<NotificationTemplate> toggleTemplateActivation(@PathVariable("id") Integer id) {
        try {
            Optional<NotificationTemplate> updatedTemplate = templateService.toggleTemplateActivation(id);
            return updatedTemplate.map(ResponseEntity::ok)
                               .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprimer un template (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable("id") Integer id) {
        try {
            boolean deleted = templateService.deleteTemplate(id);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Tester un template avec des variables
     */
    @PostMapping("/{code}/test")
    public ResponseEntity<String> testTemplate(
            @PathVariable("code") String code,
            @RequestBody TemplateTestRequest testRequest) {
        try {
            String processedContent = templateService.processTemplateWithVariables(code, testRequest.getVariables());
            return ResponseEntity.ok(processedContent);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Récupérer les templates par canal
     */
    @GetMapping("/canal/{canal}")
    public ResponseEntity<List<NotificationTemplate>> getTemplatesByCanal(@PathVariable("canal") String canal) {
        try {
            List<NotificationTemplate> templates = templateService.getTemplatesByCanal(canal);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTO pour les requêtes de test
    public static class TemplateTestRequest {
        private java.util.Map<String, Object> variables;

        public java.util.Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(java.util.Map<String, Object> variables) {
            this.variables = variables;
        }
    }
}