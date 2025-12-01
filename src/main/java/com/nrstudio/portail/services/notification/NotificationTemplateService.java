package com.nrstudio.portail.services.notification;

import com.nrstudio.portail.depots.NotificationTemplateRepository;
import com.nrstudio.portail.depots.TypeNotificationRepository;
import com.nrstudio.portail.domaine.NotificationTemplate;
import com.nrstudio.portail.domaine.TypeNotification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class NotificationTemplateService {
    
    private final NotificationTemplateRepository templateRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    
    public NotificationTemplateService(NotificationTemplateRepository templateRepository, 
                                     TypeNotificationRepository typeNotificationRepository) {
        this.templateRepository = templateRepository;
        this.typeNotificationRepository = typeNotificationRepository;
    }
    
    // Méthodes existantes
    public Optional<NotificationTemplate> getTemplateByCode(String code) {
        return templateRepository.findByCodeAndActifTrue(code);
    }
    
    public Optional<TypeNotification> getTypeNotificationByCode(String code) {
        return typeNotificationRepository.findByCodeAndEstActifTrue(code);
    }
    
    public String processTemplate(String templateContent, Map<String, Object> variables) {
        if (templateContent == null) {
            return "";
        }
        
        String processedContent = templateContent;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "\\$\\{" + entry.getKey() + "\\}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processedContent = processedContent.replaceAll(placeholder, value);
        }
        
        return processedContent;
    }
    
    public List<NotificationTemplate> getAllActiveTemplates() {
        return templateRepository.findByActifTrue();
    }
    
    // NOUVELLES MÉTHODES POUR LA GESTION
    
    /**
     * Récupérer un template par son ID
     */
    public Optional<NotificationTemplate> getTemplateById(Integer id) {
        return templateRepository.findById(id);
    }
    

    /**
     * Créer un nouveau template avec création automatique du TypeNotification associé
     */
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        
        // Vérifier si le code existe déjà
        if (templateRepository.findByCode(template.getCode()).isPresent()) {
            throw new IllegalArgumentException("Un template avec ce code existe déjà");
        }
        
        // Set template properties
        template.setActif(true);
        template.setDateCreation(LocalDateTime.now());
        template.setDateMiseAJour(LocalDateTime.now());
        
        // Sauvegarder d'abord le template pour obtenir un ID
        NotificationTemplate savedTemplate = templateRepository.save(template);
        
        // Créer automatiquement le TypeNotification associé
        createAssociatedTypeNotification(savedTemplate);
        
        return savedTemplate;
    }

    /**
     * Crée un TypeNotification associé au template
     */
    private void createAssociatedTypeNotification(NotificationTemplate template) {
        TypeNotification typeNotification = new TypeNotification();
        
        // Utiliser le code du template comme base pour le code du type
        typeNotification.setCode(template.getCode());
        
        // Utiliser le libellé du template comme base pour le libellé du type
        typeNotification.setLibelle(template.getLibelle());
        
        // Description par défaut
        typeNotification.setDescription("Type de notification généré automatiquement pour le template: " + template.getLibelle());
        
        // Associer le template
        typeNotification.setTemplate(template);
        
        // Set autres propriétés
        typeNotification.setEstActif(true);
        typeNotification.setDateCreation(LocalDateTime.now());
        typeNotification.setDateModification(LocalDateTime.now());
        
        // Sauvegarder le type de notification
        typeNotificationRepository.save(typeNotification);
    }
    
    /**
     * Mettre à jour un template existant
     */
    public NotificationTemplate updateTemplate(Integer id, NotificationTemplate templateDetails) {
        NotificationTemplate existingTemplate = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template non trouvé"));
        
        // Mettre à jour les champs modifiables
        if (templateDetails.getLibelle() != null) {
            existingTemplate.setLibelle(templateDetails.getLibelle());
        }
        if (templateDetails.getCanal() != null) {
            existingTemplate.setCanal(templateDetails.getCanal());
        }
        if (templateDetails.getSujet() != null) {
            existingTemplate.setSujet(templateDetails.getSujet());
        }
        if (templateDetails.getContenuHtml() != null) {
            existingTemplate.setContenuHtml(templateDetails.getContenuHtml());
        }
        if (templateDetails.getActif() != null) {
            existingTemplate.setActif(templateDetails.getActif());
        }
        
        existingTemplate.setDateMiseAJour(LocalDateTime.now());
        
        return templateRepository.save(existingTemplate);
    }
    
    /**
     * Activer/Désactiver un template
     */
    public Optional<NotificationTemplate> toggleTemplateActivation(Integer id) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setActif(!template.getActif());
            template.setDateMiseAJour(LocalDateTime.now());
            return Optional.of(templateRepository.save(template));
        }
        return Optional.empty();
    }
    
    /**
     * Supprimer un template (soft delete)
     */
    public boolean deleteTemplate(Integer id) {
        Optional<NotificationTemplate> templateOpt = templateRepository.findById(id);
        if (templateOpt.isPresent()) {
            NotificationTemplate template = templateOpt.get();
            template.setActif(false);
            template.setDateMiseAJour(LocalDateTime.now());
            templateRepository.save(template);
            return true;
        }
        return false;
    }
    
    /**
     * Traiter un template avec des variables pour test
     */
    public String processTemplateWithVariables(String templateCode, Map<String, Object> variables) {
        Optional<NotificationTemplate> templateOpt = getTemplateByCode(templateCode);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("Template non trouvé: " + templateCode);
        }
        
        NotificationTemplate template = templateOpt.get();
        String processedContent = processTemplate(template.getContenuHtml(), variables);
        
        return processedContent;
    }
    
    /**
     * Récupérer les templates par canal
     */
    public List<NotificationTemplate> getTemplatesByCanal(String canal) {
        return templateRepository.findByCanalAndActifTrue(canal);
    }
    
    /**
     * Récupérer tous les templates (même inactifs - pour l'admin)
     */
    public List<NotificationTemplate> getAllTemplates() {
        return templateRepository.findAll();
    }
    
    /**
     * Dupliquer un template
     */
    public NotificationTemplate duplicateTemplate(Integer id, String newCode) {
        NotificationTemplate original = templateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template non trouvé"));
        
        // Vérifier si le nouveau code existe déjà
        if (templateRepository.findByCode(newCode).isPresent()) {
            throw new IllegalArgumentException("Un template avec ce code existe déjà");
        }
        
        NotificationTemplate duplicate = new NotificationTemplate();
        duplicate.setCode(newCode);
        duplicate.setLibelle(original.getLibelle() + " (Copie)");
        duplicate.setCanal(original.getCanal());
        duplicate.setSujet(original.getSujet());
        duplicate.setContenuHtml(original.getContenuHtml());
        duplicate.setActif(true);
        duplicate.setDateCreation(LocalDateTime.now());
        duplicate.setDateMiseAJour(LocalDateTime.now());
        
        return templateRepository.save(duplicate);
    }
}