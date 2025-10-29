package com.nrstudio.portail.controleurs.workflow;

import com.nrstudio.portail.dto.workflow.*;
import com.nrstudio.portail.services.WorkflowConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@CrossOrigin(origins = "*")
public class WorkflowConfigControleur {
    
    private final WorkflowConfigService workflowConfigService;
    
    public WorkflowConfigControleur(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }
    
    @GetMapping("/configurations")
    public ResponseEntity<List<WorkflowConfigDto>> getAllWorkflowConfigs() {
        try {
            List<WorkflowConfigDto> configs = workflowConfigService.getAllWorkflowConfigs();
            return ResponseEntity.ok(configs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/configurations/{typeNotificationCode}")
    public ResponseEntity<WorkflowConfigDto> getWorkflowConfigByType(
            @PathVariable String typeNotificationCode) {
        try {
            WorkflowConfigDto config = workflowConfigService.getWorkflowConfigByType(typeNotificationCode);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/configurations")
    public ResponseEntity<Void> saveWorkflowConfig(@RequestBody WorkflowConfigDto configDto) {
        try {
            workflowConfigService.saveWorkflowConfig(configDto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/utilisateurs/internes")
    public ResponseEntity<List<UserDto>> getAvailableUsers() {
        try {
            List<UserDto> users = workflowConfigService.getAvailableUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/type-notifications")
    public ResponseEntity<List<TypeNotificationDto>> getNotificationTypes() {
        try {
            List<TypeNotificationDto> types = workflowConfigService.getNotificationTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}