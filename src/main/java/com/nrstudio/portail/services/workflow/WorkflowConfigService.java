package com.nrstudio.portail.services.workflow;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.workflow.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class WorkflowConfigService {
    
    private final WorkflowNotificationMailRepository workflowRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    
    public WorkflowConfigService(WorkflowNotificationMailRepository workflowRepository,
                               TypeNotificationRepository typeNotificationRepository,
                               UtilisateurRepository utilisateurRepository,
                               UtilisateurRoleRepository utilisateurRoleRepository) {
        this.workflowRepository = workflowRepository;
        this.typeNotificationRepository = typeNotificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurRoleRepository = utilisateurRoleRepository;
    }
    
    /**
     * Récupère tous les workflows de notification configurés
     */
    public List<WorkflowConfigDto> getAllWorkflowConfigs() {
        try {
            List<TypeNotification> types = typeNotificationRepository.findAllActifs();
            return types.stream()
                    .map(this::convertToWorkflowConfigDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des workflows", e);
        }
    }
    
    /**
     * Récupère un workflow spécifique par type de notification
     */
    public WorkflowConfigDto getWorkflowConfigByType(String typeNotificationCode) {
        try {
            Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(typeNotificationCode);
            if (typeOpt.isEmpty()) {
                return new WorkflowConfigDto(typeNotificationCode, new ArrayList<>());
            }
            return convertToWorkflowConfigDto(typeOpt.get());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération du workflow pour le type: " + typeNotificationCode, e);
        }
    }
    
    /**
     * Sauvegarde un workflow de notification
     */

    @Transactional
    public void saveWorkflowConfig(WorkflowConfigDto configDto) {
        try {
            Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(configDto.getTypeNotificationCode());
            if (typeOpt.isEmpty()) {
                throw new IllegalArgumentException("Type de notification non trouvé: " + configDto.getTypeNotificationCode());
            }
            
            // Valider que tous les utilisateurs sont internes
            if (!validateWorkflowUsers(configDto.getSteps())) {
                throw new IllegalArgumentException("Un ou plusieurs utilisateurs ne sont pas des utilisateurs internes valides (rôles 2 ou 3 requis)");
            }
            
            TypeNotification typeNotification = typeOpt.get();
            
            // Récupérer les étapes existantes
            List<WorkflowNotificationMail> existingSteps = workflowRepository.findByTypeNotificationIdActif(typeNotification.getId());
            Map<Integer, WorkflowNotificationMail> existingStepsMap = existingSteps.stream()
                    .collect(Collectors.toMap(WorkflowNotificationMail::getId, Function.identity()));
            
            // Map pour suivre les étapes à conserver
            Set<Integer> stepsToKeep = new HashSet<>();
            
            // Traiter chaque étape du DTO
            for (WorkflowStepDto stepDto : configDto.getSteps()) {
                if (stepDto.getId() != null && stepDto.getId() > 0) {
                    // Étape existante - mise à jour
                    WorkflowNotificationMail existingStep = existingStepsMap.get(stepDto.getId());
                    if (existingStep != null) {
                        existingStep.setOrdre(stepDto.getOrdre());
                        existingStep.setUtilisateurId(stepDto.getUtilisateurId());
                        existingStep.setDateModification(LocalDateTime.now());
                        workflowRepository.save(existingStep);
                        stepsToKeep.add(existingStep.getId());
                    }
                } else {
                    // Nouvelle étape - création
                    WorkflowNotificationMail newStep = new WorkflowNotificationMail();
                    newStep.setOrdre(stepDto.getOrdre());
                    newStep.setUtilisateurId(stepDto.getUtilisateurId());
                    newStep.setTypeNotification(typeNotification);
                    newStep.setEstActif(true);
                    newStep.setDateCreation(LocalDateTime.now());
                    
                    WorkflowNotificationMail savedStep = workflowRepository.save(newStep);
                    stepsToKeep.add(savedStep.getId());
                }
            }
            
            // Supprimer les étapes qui ne sont plus dans la configuration
            for (WorkflowNotificationMail existingStep : existingSteps) {
                if (!stepsToKeep.contains(existingStep.getId())) {
                    workflowRepository.delete(existingStep);
                }
            }
            
        } catch (IllegalArgumentException e) {
            throw e; // Propager les erreurs de validation
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du workflow", e);
        }
    }
    // @Transactional
    // public void saveWorkflowConfig(WorkflowConfigDto configDto) {
    //     try {
    //         Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(configDto.getTypeNotificationCode());
    //         if (typeOpt.isEmpty()) {
    //             throw new IllegalArgumentException("Type de notification non trouvé: " + configDto.getTypeNotificationCode());
    //         }
            
    //         // Valider que tous les utilisateurs sont internes
    //         if (!validateWorkflowUsers(configDto.getSteps())) {
    //             throw new IllegalArgumentException("Un ou plusieurs utilisateurs ne sont pas des utilisateurs internes valides (rôles 2 ou 3 requis)");
    //         }
            
    //         TypeNotification typeNotification = typeOpt.get();
            
    //         // Supprimer les anciennes étapes
    //         workflowRepository.deleteByTypeNotificationId(typeNotification.getId());
            
    //         // Sauvegarder les nouvelles étapes
    //         for (WorkflowStepDto stepDto : configDto.getSteps()) {
    //             WorkflowNotificationMail workflow = new WorkflowNotificationMail();
    //             workflow.setOrdre(stepDto.getOrdre());
    //             workflow.setUtilisateurId(stepDto.getUtilisateurId());
    //             workflow.setTypeNotification(typeNotification);
    //             workflow.setEstActif(true);
    //             workflow.setDateCreation(LocalDateTime.now());
                
    //             workflowRepository.save(workflow);
    //         }
    //     } catch (IllegalArgumentException e) {
    //         throw e; // Propager les erreurs de validation
    //     } catch (Exception e) {
    //         throw new RuntimeException("Erreur lors de la sauvegarde du workflow", e);
    //     }
    // }

    
    /**
     * Récupère la liste des utilisateurs internes (rôles 2 ou 3)
     */
    public List<UserDto> getAvailableUsers() {
    try {
        // Récupérer tous les utilisateurs actifs
        List<Utilisateur> allUsers = utilisateurRepository.findAllUtilisateurs();
        
        // Filtrer pour ne garder que les utilisateurs internes
        return allUsers.stream()
                .filter(user -> isUtilisateurInterne(user.getId()))
                .map(user -> {
                    String nomComplet = (user.getPrenom() != null && !user.getPrenom().isEmpty() ? 
                                       user.getPrenom() + " " + user.getNom() : user.getNom());
                    return new UserDto(user.getId(), nomComplet, user.getEmail());
                })
                .collect(Collectors.toList());
    } catch (Exception e) {
        throw new RuntimeException("Erreur lors de la récupération des utilisateurs internes", e);
    }
}
    
    /**
     * Récupère tous les types de notification actifs
     */
    public List<TypeNotificationDto> getNotificationTypes() {
        try {
            return typeNotificationRepository.findAllActifs().stream()
                    .map(this::convertToTypeNotificationDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des types de notification", e);
        }
    }
    
    /**
     * Vérifie si un utilisateur est interne (a un rôle 2 ou 3)
     */
    public boolean isUtilisateurInterne(Integer utilisateurId) {
        try {
            return utilisateurRoleRepository.existsByUtilisateurIdAndRoleIdIn(
                utilisateurId, Arrays.asList(2, 3));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'utilisateur interne", e);
        }
    }
    
    /**
     * Valide que tous les utilisateurs dans le workflow sont bien des utilisateurs internes
     */
    public boolean validateWorkflowUsers(List<WorkflowStepDto> steps) {
        if (steps == null || steps.isEmpty()) {
            return true;
        }
        
        for (WorkflowStepDto step : steps) {
            if (!isUtilisateurInterne(step.getUtilisateurId())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Met à jour une étape spécifique du workflow
     */
    @Transactional
    public void updateWorkflowStep(WorkflowStepDto stepDto) {
        try {
            Optional<WorkflowNotificationMail> stepOpt = workflowRepository.findById(stepDto.getId());
            if (stepOpt.isEmpty()) {
                throw new IllegalArgumentException("Étape de workflow non trouvée: " + stepDto.getId());
            }
            
            // Valider que l'utilisateur est interne
            if (!isUtilisateurInterne(stepDto.getUtilisateurId())) {
                throw new IllegalArgumentException("L'utilisateur n'est pas un utilisateur interne valide");
            }
            
            WorkflowNotificationMail step = stepOpt.get();
            step.setOrdre(stepDto.getOrdre());
            step.setUtilisateurId(stepDto.getUtilisateurId());
            step.setDateModification(LocalDateTime.now());
            
            workflowRepository.save(step);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'étape du workflow", e);
        }
    }
    
    /**
     * Supprime une étape du workflow
     */
    @Transactional
    public void deleteWorkflowStep(Integer stepId) {
        try {
            if (!workflowRepository.existsById(stepId)) {
                throw new IllegalArgumentException("Étape de workflow non trouvée: " + stepId);
            }
            workflowRepository.deleteById(stepId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression de l'étape du workflow", e);
        }
    }
    
    // Méthodes de conversion privées
    
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
        
        // Charger le nom complet de l'utilisateur
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(workflow.getUtilisateurId());
        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            String nomComplet = (user.getPrenom() != null ? user.getPrenom() + " " : "") + user.getNom();
            dto.setUtilisateurNom(nomComplet);
        }
        
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