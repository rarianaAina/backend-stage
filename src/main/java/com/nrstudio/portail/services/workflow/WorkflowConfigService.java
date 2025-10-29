package com.nrstudio.portail.services.workflow;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.workflow.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowConfigService {
    
    private final WorkflowNotificationMailRepository workflowRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    
    public WorkflowConfigService(WorkflowNotificationMailRepository workflowRepository,
                               TypeNotificationRepository typeNotificationRepository,
                               UtilisateurRepository utilisateurRepository) {
        this.workflowRepository = workflowRepository;
        this.typeNotificationRepository = typeNotificationRepository;
        this.utilisateurRepository = utilisateurRepository;
    }
    
    public List<WorkflowConfigDto> getAllWorkflowConfigs() {
        List<TypeNotification> types = typeNotificationRepository.findAllActifs();
        return types.stream()
                .map(this::convertToWorkflowConfigDto)
                .collect(Collectors.toList());
    }
    
    public WorkflowConfigDto getWorkflowConfigByType(String typeNotificationCode) {
        Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(typeNotificationCode);
        if (typeOpt.isEmpty()) {
            return new WorkflowConfigDto(typeNotificationCode, new ArrayList<>());
        }
        return convertToWorkflowConfigDto(typeOpt.get());
    }
    
    @Transactional
    public void saveWorkflowConfig(WorkflowConfigDto configDto) {
        Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(configDto.getTypeNotificationCode());
        if (typeOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de notification non trouvé: " + configDto.getTypeNotificationCode());
        }
        
        TypeNotification typeNotification = typeOpt.get();
        
        // Supprimer les anciennes étapes
        workflowRepository.deleteByTypeNotificationId(typeNotification.getId());
        
        // Sauvegarder les nouvelles étapes
        for (WorkflowStepDto stepDto : configDto.getSteps()) {
            WorkflowNotificationMail workflow = new WorkflowNotificationMail();
            workflow.setOrdre(stepDto.getOrdre());
            workflow.setUtilisateurId(stepDto.getUtilisateurId());
            workflow.setTypeNotification(typeNotification);
            workflow.setEstActif(true);
            workflow.setDateCreation(LocalDateTime.now());
            
            workflowRepository.save(workflow);
        }
    }
    
    public List<UserDto> getAvailableUsers() {
        return utilisateurRepository.findUtilisateursInternesDto();
    }
    
    public List<TypeNotificationDto> getNotificationTypes() {
        return typeNotificationRepository.findAllActifs().stream()
                .map(this::convertToTypeNotificationDto)
                .collect(Collectors.toList());
    }
    
    private WorkflowConfigDto convertToWorkflowConfigDto(TypeNotification typeNotification) {
        List<WorkflowNotificationMail> workflowSteps = workflowRepository
                .findByTypeNotificationIdActif(typeNotification.getId());
        
        List<WorkflowStepDto> stepDtos = workflowSteps.stream()
                .map(this::convertToWorkflowStepDto)
                .collect(Collectors.toList());
        
        return new WorkflowConfigDto(typeNotification.getCode(), stepDtos);
    }
    
    private WorkflowStepDto convertToWorkflowStepDto(WorkflowNotificationMail workflow) {
        WorkflowStepDto dto = new WorkflowStepDto();
        dto.setId(workflow.getId());
        dto.setOrdre(workflow.getOrdre());
        dto.setUtilisateurId(workflow.getUtilisateurId());
        dto.setTypeNotificationId(workflow.getTypeNotification().getId());
        dto.setTypeNotificationLibelle(workflow.getTypeNotification().getLibelle());
        
        // Optionnel: charger le nom de l'utilisateur
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(workflow.getUtilisateurId());
        userOpt.ifPresent(user -> dto.setUtilisateurNom(user.getNom()));
        
        return dto;
    }
    
    private TypeNotificationDto convertToTypeNotificationDto(TypeNotification type) {
        TypeNotificationDto dto = new TypeNotificationDto();
        dto.setId(type.getId());
        dto.setCode(type.getCode());
        dto.setLibelle(type.getLibelle());
        dto.setDescription(type.getDescription());
        return dto;
    }
}