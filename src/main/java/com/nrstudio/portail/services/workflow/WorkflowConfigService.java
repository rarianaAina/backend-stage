package com.nrstudio.portail.services.workflow;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.depots.workflow.WorkflowNotificationViewRepository;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.dto.utilisateur.UtilisateurInterneDto;
import com.nrstudio.portail.dto.workflow.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowConfigService {
    
    private final WorkflowNotificationMailRepository workflowRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    private final UtilisateurInterneRepository utilisateurInterneRepository;
    private final WorkflowNotificationViewRepository workflowViewRepository;
    
    public WorkflowConfigService(WorkflowNotificationMailRepository workflowRepository,
                               TypeNotificationRepository typeNotificationRepository,
                               UtilisateurInterneRepository utilisateurInterneRepository,
                               WorkflowNotificationViewRepository workflowViewRepository) {
        this.workflowRepository = workflowRepository;
        this.typeNotificationRepository = typeNotificationRepository;
        this.utilisateurInterneRepository = utilisateurInterneRepository;
        this.workflowViewRepository = workflowViewRepository;
    }
    
    /**
     * Récupère tous les workflows de notification configurés - VERSION OPTIMISÉE
     */
    public List<WorkflowConfigDto> getAllWorkflowConfigs() {
        try {
            List<Object[]> results = workflowViewRepository.findCompleteActiveWorkflows();
            return transformResultsToWorkflowConfigs(results);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération des workflows", e);
        }
    }
    
    /**
     * Récupère un workflow spécifique par type de notification - VERSION OPTIMISÉE
     */
    public WorkflowConfigDto getWorkflowConfigByType(String typeNotificationCode) {
        try {
            List<Object[]> results = workflowViewRepository.findActiveByTypeNotificationCode(typeNotificationCode);
            List<WorkflowStepDto> stepDtos = results.stream()
                    .map(this::createWorkflowStepDto)
                    .collect(Collectors.toList());
            
            return new WorkflowConfigDto(typeNotificationCode, stepDtos);
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
            
            // Valider que tous les utilisateurs existent dans utilisateur_interne
            if (!validateWorkflowUsers(configDto.getSteps())) {
                throw new IllegalArgumentException("Un ou plusieurs utilisateurs ne sont pas des utilisateurs internes valides");
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
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du workflow", e);
        }
    }
    
    /**
     * Récupère la liste des utilisateurs internes
     */
    public List<UtilisateurInterneDto> getAvailableUsers() {
        try {
            return utilisateurInterneRepository.findAllUtilisateurs();
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
     * Vérifie si un utilisateur existe dans utilisateur_interne
     */
    public boolean isUtilisateurInterne(Integer utilisateurId) {
        try {
            return utilisateurInterneRepository.existsById(utilisateurId);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la vérification de l'utilisateur interne", e);
        }
    }
    
    /**
     * Valide que tous les utilisateurs dans le workflow existent dans utilisateur_interne
     */
    public boolean validateWorkflowUsers(List<WorkflowStepDto> steps) {
        if (steps == null || steps.isEmpty()) {
            return true;
        }
        
        List<Integer> userIds = steps.stream()
                .map(WorkflowStepDto::getUtilisateurId)
                .distinct()
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return true;
        }
        
        long existingUsersCount = utilisateurInterneRepository.countByIdIn(userIds);
        return existingUsersCount == userIds.size();
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
    
    // Méthodes privées pour le mapping
    
    private List<WorkflowConfigDto> transformResultsToWorkflowConfigs(List<Object[]> results) {
        Map<String, List<WorkflowStepDto>> workflowsByType = new LinkedHashMap<>();

        for (Object[] row : results) {
            String typeCode = getStringValue(row[0]);
            WorkflowStepDto stepDto = createWorkflowStepDto(row);
            
            workflowsByType
                .computeIfAbsent(typeCode, k -> new ArrayList<>())
                .add(stepDto);
        }

        return workflowsByType.entrySet().stream()
                .map(entry -> new WorkflowConfigDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
    
    private WorkflowStepDto createWorkflowStepDto(Object[] row) {
        WorkflowStepDto stepDto = new WorkflowStepDto();
        
        stepDto.setId(getIntegerValue(row[1]));
        stepDto.setOrdre(getIntegerValue(row[2]));
        stepDto.setUtilisateurId(getIntegerValue(row[3]));
        
        String nom = getStringValue(row[4]);
        String prenom = getStringValue(row[5]);
        if (nom != null && prenom != null) {
            stepDto.setUtilisateurNom(prenom + " " + nom);
        } else if (nom != null) {
            stepDto.setUtilisateurNom(nom);
        } else if (prenom != null) {
            stepDto.setUtilisateurNom(prenom);
        }
        
        stepDto.setTypeNotificationId(getIntegerValue(row[7]));
        stepDto.setTypeNotificationLibelle(getStringValue(row[8]));
        
        return stepDto;
    }
    
    private String getStringValue(Object value) {
        return value != null ? value.toString() : null;
    }
    
    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private TypeNotificationDto convertToTypeNotificationDto(TypeNotification type) {
        TypeNotificationDto dto = new TypeNotificationDto();
        dto.setId(type.getId());
        dto.setCode(type.getCode());
        dto.setLibelle(type.getLibelle());
        dto.setDescription(type.getDescription());
        return dto;
    }
    
    /**
     * Récupère un utilisateur interne par son ID
     */
    public Optional<UtilisateurInterneDto> getUtilisateurInterneById(Integer id) {
        try {
            Optional<UtilisateurInterne> utilisateurOpt = utilisateurInterneRepository.findById(id);
            return utilisateurOpt.map(this::convertToUtilisateurInterneDto);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de l'utilisateur interne", e);
        }
    }
    
    private UtilisateurInterneDto convertToUtilisateurInterneDto(UtilisateurInterne utilisateur) {
        UtilisateurInterneDto dto = new UtilisateurInterneDto();
        dto.setId(utilisateur.getId());
        dto.setCompanyId(utilisateur.getCompanyId());
        dto.setIdExterneCrm(utilisateur.getIdExterneCrm());
        dto.setIdentifiant(utilisateur.getIdentifiant());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setTelephone(utilisateur.getTelephone());
        dto.setWhatsappNumero(utilisateur.getWhatsappNumero());
        dto.setActif(utilisateur.isActif());
        dto.setDateDerniereConnexion(utilisateur.getDateDerniereConnexion());
        dto.setDateCreation(utilisateur.getDateCreation());
        dto.setDateMiseAJour(utilisateur.getDateMiseAJour());
        
        if (utilisateur.getCompany() != null) {
            dto.setCompanyName(utilisateur.getCompany().getNom());
        }
        
        return dto;
    }
}