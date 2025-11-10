package com.nrstudio.portail.services.whatsapp;

import com.nrstudio.portail.depots.whatsapp.WhatsappConfigRepository;
import com.nrstudio.portail.domaine.whatsapp.ConfigurationWhatsApp;
import com.nrstudio.portail.dto.whatsapp.ConfigurationWhatsAppDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WhatsAppConfigService {
    
    private final WhatsappConfigRepository whatsappConfigRepository;
    
    public WhatsAppConfigService(WhatsappConfigRepository whatsappConfigRepository) {
        this.whatsappConfigRepository = whatsappConfigRepository;
    }
    
    // Récupérer toutes les configurations
    public List<ConfigurationWhatsAppDTO> getAllConfigurations() {
        return whatsappConfigRepository.findAll()
                .stream()
                .map(ConfigurationWhatsAppDTO::new)
                .collect(Collectors.toList());
    }
    
    // Récupérer une configuration par ID
    public Optional<ConfigurationWhatsAppDTO> getConfigurationById(Integer id) {
        return whatsappConfigRepository.findById(id)
                .map(ConfigurationWhatsAppDTO::new);
    }
    
    // Récupérer la configuration active
    public Optional<ConfigurationWhatsAppDTO> getActiveConfiguration() {
        return whatsappConfigRepository.findByEstActifTrue()
                .map(ConfigurationWhatsAppDTO::new);
    }
    
    // Sauvegarder une configuration
    public ConfigurationWhatsAppDTO saveConfiguration(ConfigurationWhatsAppDTO configDTO) {
        // Vérifier si c'est une nouvelle configuration ou une mise à jour
        boolean isNew = configDTO.getId() == null;
        
        ConfigurationWhatsApp entity = configDTO.toEntity();
        
        // Mettre à jour la date de modification
        entity.setDateModification(LocalDateTime.now());
        
        // Si c'est une nouvelle configuration et qu'elle est active, désactiver les autres
        if (isNew && Boolean.TRUE.equals(entity.getEstActif())) {
            deactivateOtherConfigurations();
        }
        // Si c'est une mise à jour et qu'on active cette configuration, désactiver les autres
        else if (!isNew && Boolean.TRUE.equals(entity.getEstActif())) {
            deactivateOtherConfigurationsExcept(entity.getId());
        }
        
        ConfigurationWhatsApp savedEntity = whatsappConfigRepository.save(entity);
        return new ConfigurationWhatsAppDTO(savedEntity);
    }
    
    // Désactiver une configuration
    public boolean deactivateConfiguration(Integer id) {
        Optional<ConfigurationWhatsApp> configOpt = whatsappConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            ConfigurationWhatsApp config = configOpt.get();
            config.setEstActif(false);
            config.setDateModification(LocalDateTime.now());
            whatsappConfigRepository.save(config);
            return true;
        }
        return false;
    }
    
    // Activer une configuration
    public boolean activateConfiguration(Integer id) {
        Optional<ConfigurationWhatsApp> configOpt = whatsappConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            // Désactiver toutes les autres configurations
            deactivateOtherConfigurations();
            
            // Activer celle-ci
            ConfigurationWhatsApp config = configOpt.get();
            config.setEstActif(true);
            config.setDateModification(LocalDateTime.now());
            whatsappConfigRepository.save(config);
            return true;
        }
        return false;
    }
    
    // Supprimer une configuration
    public boolean deleteConfiguration(Integer id) {
        if (whatsappConfigRepository.existsById(id)) {
            whatsappConfigRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Tester la configuration (méthode simulée - à adapter avec l'API 360dialog)
    public boolean testConfiguration(Integer id) {
        Optional<ConfigurationWhatsApp> configOpt = whatsappConfigRepository.findById(id);
        if (configOpt.isPresent()) {
            ConfigurationWhatsApp config = configOpt.get();
            // Ici, vous implémenteriez le test réel avec l'API 360dialog
            // Pour l'instant, on simule un test réussi
            try {
                // Simulation d'un test d'API
                // TODO: Implémenter le vrai test avec l'API 360dialog
                Thread.sleep(1000); // Simulation délai réseau
                return isValidConfiguration(config);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
    
    // Méthodes privées utilitaires
    private void deactivateOtherConfigurations() {
        List<ConfigurationWhatsApp> activeConfigs = whatsappConfigRepository.findByEstActif(true);
        for (ConfigurationWhatsApp config : activeConfigs) {
            config.setEstActif(false);
            config.setDateModification(LocalDateTime.now());
        }
        whatsappConfigRepository.saveAll(activeConfigs);
    }
    
    private void deactivateOtherConfigurationsExcept(Integer excludedId) {
        List<ConfigurationWhatsApp> activeConfigs = whatsappConfigRepository.findByEstActif(true);
        for (ConfigurationWhatsApp config : activeConfigs) {
            if (!config.getId().equals(excludedId)) {
                config.setEstActif(false);
                config.setDateModification(LocalDateTime.now());
            }
        }
        whatsappConfigRepository.saveAll(activeConfigs);
    }
    
    private boolean isValidConfiguration(ConfigurationWhatsApp config) {
        // Validation basique des champs requis
        return config.getApiKey() != null && !config.getApiKey().trim().isEmpty() &&
               config.getPhoneNumberId() != null && !config.getPhoneNumberId().trim().isEmpty() &&
               config.getApiBaseUrl() != null && !config.getApiBaseUrl().trim().isEmpty();
    }
}