package com.nrstudio.portail.controleurs.workflow;

import com.nrstudio.portail.dto.workflow.*;
import com.nrstudio.portail.services.workflow.WorkflowConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*")
public class WorkflowConfigControleur {
    
    private final WorkflowConfigService workflowConfigService;
    
    public WorkflowConfigControleur(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }
    
    /**
     * Récupère tous les workflows de notification
     */
    @GetMapping("/configurations")
    public ResponseEntity<?> getAllWorkflowConfigs() {
        try {
            List<WorkflowConfigDto> configs = workflowConfigService.getAllWorkflowConfigs();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération des workflows: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère un workflow spécifique par type de notification
     */
    @GetMapping("/configurations/{typeNotificationCode}")
    public ResponseEntity<?> getWorkflowConfigByType(
            @PathVariable String typeNotificationCode) {
        try {
            WorkflowConfigDto config = workflowConfigService.getWorkflowConfigByType(typeNotificationCode);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération du workflow: " + e.getMessage()));
        }
    }
    
    /**
     * Sauvegarde un workflow de notification
     */
    @PostMapping("/configurations")
    public ResponseEntity<?> saveWorkflowConfig(@RequestBody WorkflowConfigDto configDto) {
        try {
            workflowConfigService.saveWorkflowConfig(configDto);
            return ResponseEntity.ok().body(Map.of("message", "Workflow sauvegardé avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la sauvegarde du workflow: " + e.getMessage()));
        }
    }
    
    /**
     * Met à jour une étape spécifique du workflow
     */
    @PutMapping("/steps/{stepId}")
    public ResponseEntity<?> updateWorkflowStep(
            @PathVariable Integer stepId,
            @RequestBody WorkflowStepDto stepDto) {
        try {
            // S'assurer que l'ID du chemin correspond à l'ID du body
            if (!stepId.equals(stepDto.getId())) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID incohérent"));
            }
            
            workflowConfigService.updateWorkflowStep(stepDto);
            return ResponseEntity.ok().body(Map.of("message", "Étape mise à jour avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la mise à jour de l'étape: " + e.getMessage()));
        }
    }
    
    /**
     * Supprime une étape du workflow
     */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<?> deleteWorkflowStep(@PathVariable Integer stepId) {
        try {
            workflowConfigService.deleteWorkflowStep(stepId);
            return ResponseEntity.ok().body(Map.of("message", "Étape supprimée avec succès"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la suppression de l'étape: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère la liste des utilisateurs internes disponibles
     */
    @GetMapping("/utilisateurs/internes")
    public ResponseEntity<?> getAvailableUsers() {
        try {
            List<UserDto> users = workflowConfigService.getAvailableUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération des utilisateurs: " + e.getMessage()));
        }
    }
    
    /**
     * Récupère tous les types de notification
     */
    @GetMapping("/type-notifications")
    public ResponseEntity<?> getNotificationTypes() {
        try {
            List<TypeNotificationDto> types = workflowConfigService.getNotificationTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération des types de notification: " + e.getMessage()));
        }
    }
    
    /**
     * Vérifie si un utilisateur est interne
     */
    @GetMapping("/utilisateurs/{userId}/est-interne")
    public ResponseEntity<?> isUtilisateurInterne(@PathVariable Integer userId) {
        try {
            boolean isInterne = workflowConfigService.isUtilisateurInterne(userId);
            return ResponseEntity.ok(Map.of("estInterne", isInterne));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la vérification: " + e.getMessage()));
        }
    }
}