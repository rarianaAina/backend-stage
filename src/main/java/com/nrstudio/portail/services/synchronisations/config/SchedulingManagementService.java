package com.nrstudio.portail.services.synchronisations.config;

import com.nrstudio.portail.depots.synchronisation.SchedulingConfigurationRepository;
import com.nrstudio.portail.domaine.synchronisation.SchedulingConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nrstudio.portail.dto.synchronisations.CreateSimplifiedSchedulingRequest;
import com.nrstudio.portail.dto.synchronisations.SimplifiedSchedulingRequest;
import com.nrstudio.portail.services.synchronisations.config.CronConversionService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SchedulingManagementService {

    private final SchedulingConfigurationRepository repository;
    private final CronConversionService cronConversionService;
    private final SchedulingConfigService schedulingConfigService;

    public SchedulingManagementService(SchedulingConfigurationRepository repository,
                                     CronConversionService cronConversionService,
                                     SchedulingConfigService schedulingConfigService) {
        this.repository = repository;
        this.cronConversionService = cronConversionService;
        this.schedulingConfigService = schedulingConfigService;
    }

    /**
     * Récupère toutes les configurations avec format simplifié
     */
    public List<Map<String, Object>> getAllConfigurations() {
        List<SchedulingConfiguration> configurations = repository.findAllByOrderByJobName();
        return configurations.stream()
            .map(this::toSimplifiedConfig)
            .toList();
    }

    /**
     * Crée une nouvelle configuration
     */
    @Transactional
    public Map<String, Object> createConfiguration(CreateSimplifiedSchedulingRequest request) {
        if (repository.existsByJobName(request.getJobName())) {
            throw new IllegalArgumentException("Un job avec ce nom existe déjà");
        }

        String cronExpression = cronConversionService.convertToCronExpression(request);
        String displayName = cronConversionService.generateDisplayName(request);

        SchedulingConfiguration config = new SchedulingConfiguration();
        config.setJobName(request.getJobName());
        config.setJobDescription(request.getJobDescription());
        config.setCronExpression(cronExpression);
        config.setDisplayName(displayName);
        config.setScheduleType(request.getFrequency());
        config.setEnabled(request.isEnabled());
        config.setLastModifiedBy(request.getModifiedBy());

        SchedulingConfiguration savedConfig = repository.save(config);

        return Map.of(
            "message", "Configuration créée avec succès",
            "jobName", savedConfig.getJobName(),
            "cronExpression", cronExpression,
            "displayName", displayName
        );
    }

    /**
     * Met à jour une configuration existante
     */
    @Transactional
    public Map<String, Object> updateConfiguration(String jobName, SimplifiedSchedulingRequest request) {
        Optional<SchedulingConfiguration> existingConfig = repository.findByJobName(jobName);
        if (existingConfig.isEmpty()) {
            throw new IllegalArgumentException("Configuration non trouvée: " + jobName);
        }

        String cronExpression = cronConversionService.convertToCronExpression(request);
        String displayName = cronConversionService.generateDisplayName(request);

        SchedulingConfiguration config = existingConfig.get();
        config.setCronExpression(cronExpression);
        config.setDisplayName(displayName);
        config.setScheduleType(request.getFrequency());
        config.setEnabled(request.isEnabled());
        config.setLastModified(LocalDateTime.now());
        config.setLastModifiedBy(request.getModifiedBy());

        SchedulingConfiguration updatedConfig = repository.save(config);

        // Vide le cache pour cette configuration
        schedulingConfigService.clearCache(jobName);

        return Map.of(
            "message", "Configuration mise à jour avec succès",
            "jobName", updatedConfig.getJobName(),
            "cronExpression", cronExpression,
            "displayName", displayName
        );
    }

    /**
     * Active/Désactive un job
     */
    @Transactional
    public Map<String, Object> toggleJob(String jobName, boolean enabled) {
        int updated = repository.updateEnabledStatus(jobName, enabled);
        
        if (updated == 0) {
            throw new IllegalArgumentException("Configuration non trouvée: " + jobName);
        }

        schedulingConfigService.clearCache(jobName);

        return Map.of(
            "message", "Job " + (enabled ? "activé" : "désactivé") + " avec succès",
            "jobName", jobName,
            "enabled", enabled
        );
    }

    /**
     * Convertit la configuration en format simplifié pour l'interface
     */
    private Map<String, Object> toSimplifiedConfig(SchedulingConfiguration config) {
        Map<String, Object> simplified = new HashMap<>();
        simplified.put("id", config.getId());
        simplified.put("jobName", config.getJobName());
        simplified.put("jobDescription", config.getJobDescription());
        simplified.put("cronExpression", config.getCronExpression());
        simplified.put("displayName", config.getDisplayName());
        simplified.put("scheduleType", config.getScheduleType());
        simplified.put("enabled", config.getEnabled());
        simplified.put("lastModified", config.getLastModified());
        simplified.put("lastModifiedBy", config.getLastModifiedBy());
        
        // Ajouter les paramètres décomposés pour l'interface
        Map<String, String> scheduleParams = cronConversionService.parseCronExpression(config.getCronExpression());
        simplified.put("scheduleParams", scheduleParams);
        
        return simplified;
    }
}