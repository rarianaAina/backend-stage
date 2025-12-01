package com.nrstudio.portail.controleurs.synchronisations;

import com.nrstudio.portail.depots.synchronisation.SchedulingConfigurationRepository;
import com.nrstudio.portail.domaine.synchronisation.SchedulingConfiguration;
import com.nrstudio.portail.services.synchronisations.config.CronConversionService;
import com.nrstudio.portail.services.synchronisations.config.SchedulingManagementService;
import com.nrstudio.portail.services.synchronisations.config.SchedulingConfigService;
import com.nrstudio.portail.dto.synchronisations.CreateSimplifiedSchedulingRequest;
import com.nrstudio.portail.dto.synchronisations.SimplifiedSchedulingRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/scheduling")
@CrossOrigin
public class SchedulingConfigControleur {

    private final SchedulingConfigurationRepository repository;
    private final SchedulingManagementService schedulingManagementService;
    private final CronConversionService cronConversionService;
    private final SchedulingConfigService schedulingConfigService;

    public SchedulingConfigControleur(SchedulingConfigurationRepository repository,
                                    SchedulingManagementService schedulingManagementService,
                                    CronConversionService cronConversionService,
                                    SchedulingConfigService schedulingConfigService) {
        this.repository = repository;
        this.schedulingManagementService = schedulingManagementService;
        this.cronConversionService = cronConversionService;
        this.schedulingConfigService = schedulingConfigService;
    }

    /**
     * Récupère toutes les configurations avec format simplifié
     */
    @GetMapping("/configurations")
    public ResponseEntity<?> getAllConfigurations() {
        try {
            List<Map<String, Object>> configurations = schedulingManagementService.getAllConfigurations();
            return ResponseEntity.ok(configurations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la récupération des configurations: " + e.getMessage()));
        }
    }

    /**
     * Récupère une configuration spécifique
     */
    @GetMapping("/configurations/{jobName}")
    public ResponseEntity<?> getConfiguration(@PathVariable String jobName) {
        try {
            Optional<SchedulingConfiguration> configuration = repository.findByJobName(jobName);
            if (configuration.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> simplifiedConfig = toSimplifiedConfig(configuration.get());
            return ResponseEntity.ok(simplifiedConfig);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la récupération de la configuration: " + e.getMessage()));
        }
    }

    /**
     * Récupère les options disponibles pour la configuration
     */
    @GetMapping("/configuration-options")
    public ResponseEntity<?> getConfigurationOptions() {
        try {
            Map<String, Object> options = cronConversionService.getConfigurationOptions();
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la récupération des options: " + e.getMessage()));
        }
    }

    /**
     * Crée une nouvelle configuration
     */
    @PostMapping("/configurations")
    public ResponseEntity<?> createConfiguration(@Valid @RequestBody CreateSimplifiedSchedulingRequest request) {
        try {
            Map<String, Object> result = schedulingManagementService.createConfiguration(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la création: " + e.getMessage()));
        }
    }

    /**
     * Met à jour une configuration existante
     */
    @PutMapping("/configurations/{jobName}")
    public ResponseEntity<?> updateConfiguration(@PathVariable String jobName,
                                               @Valid @RequestBody SimplifiedSchedulingRequest request) {
        try {
            Map<String, Object> result = schedulingManagementService.updateConfiguration(jobName, request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la mise à jour: " + e.getMessage()));
        }
    }

    /**
     * Active/Désactive un job
     */
    @PatchMapping("/configurations/{jobName}/toggle")
    public ResponseEntity<?> toggleJob(@PathVariable String jobName,
                                      @RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            if (enabled == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Le champ 'enabled' est requis"));
            }
            
            Map<String, Object> result = schedulingManagementService.toggleJob(jobName, enabled);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la modification: " + e.getMessage()));
        }
    }

    /**
     * Supprime une configuration
     */
    @DeleteMapping("/configurations/{jobName}")
    public ResponseEntity<?> deleteConfiguration(@PathVariable String jobName) {
        try {
            Optional<SchedulingConfiguration> config = repository.findByJobName(jobName);
            if (config.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            repository.delete(config.get());
            schedulingConfigService.clearCache(jobName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Configuration supprimée avec succès");
            response.put("jobName", jobName);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la suppression: " + e.getMessage()));
        }
    }

    /**
     * Teste une expression cron
     */
    @PostMapping("/test-cron")
    public ResponseEntity<?> testCronExpression(@RequestBody Map<String, String> request) {
        try {
            String cronExpression = request.get("cronExpression");
            if (cronExpression == null || cronExpression.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Expression cron vide"));
            }
            
            if (!cronExpression.matches("^[0-9*/, -]+$")) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Format d'expression cron invalide"));
            }
            
            String[] parts = cronExpression.split("\\s+");
            if (parts.length != 6) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Expression cron doit avoir 6 champs"));
            }
            
            return ResponseEntity.ok(Map.of("valid", true, "message", "Expression cron valide"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }

    /**
     * Récupère le statut actuel de tous les jobs
     */
    @GetMapping("/status")
    public ResponseEntity<?> getSchedulingStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            String[] jobs = {
                "crm-solutick-sync", "crm-solution-sync", "crm-company-sync",
                "crm-person-sync", "crm-product-sync", "crm-ticket-sync", "crm-ch-sync"
            };
            
            Map<String, Map<String, Object>> jobsStatus = new HashMap<>();
            for (String job : jobs) {
                Map<String, Object> jobStatus = new HashMap<>();
                jobStatus.put("enabled", schedulingConfigService.isJobEnabled(job));
                jobStatus.put("cronExpression", schedulingConfigService.getCronExpression(job));
                jobsStatus.put(job, jobStatus);
            }
            
            status.put("jobs", jobsStatus);
            status.put("lastChecked", LocalDateTime.now().toString());
            status.put("totalJobs", jobs.length);
            status.put("activeJobs", jobsStatus.values().stream()
                .filter(job -> Boolean.TRUE.equals(job.get("enabled")))
                .count());
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la récupération du statut: " + e.getMessage()));
        }
    }

    /**
     * Force le rechargement du cache pour tous les jobs
     */
    @PostMapping("/reload-cache")
    public ResponseEntity<?> reloadCache() {
        try {
            schedulingConfigService.clearAllCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cache rechargé avec succès");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors du rechargement du cache: " + e.getMessage()));
        }
    }

    /**
     * Récupère les statistiques des jobs
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            List<SchedulingConfiguration> allConfigs = repository.findAll();
            
            long totalJobs = allConfigs.size();
            long enabledJobs = allConfigs.stream()
                .filter(config -> Boolean.TRUE.equals(config.getEnabled()))
                .count();
            long disabledJobs = totalJobs - enabledJobs;
            
            Map<String, Long> scheduleTypeCount = new HashMap<>();
            for (SchedulingConfiguration config : allConfigs) {
                String scheduleType = config.getScheduleType();
                scheduleTypeCount.merge(scheduleType, 1L, Long::sum);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalJobs", totalJobs);
            statistics.put("enabledJobs", enabledJobs);
            statistics.put("disabledJobs", disabledJobs);
            statistics.put("scheduleTypeDistribution", scheduleTypeCount);
            statistics.put("lastUpdated", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erreur lors de la récupération des statistiques: " + e.getMessage()));
        }
    }

    // Méthode utilitaire pour convertir en configuration simplifiée
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
        simplified.put("createdDate", config.getCreatedDate());
        
        // Ajouter les paramètres décomposés pour l'interface
        Map<String, String> scheduleParams = cronConversionService.parseCronExpression(config.getCronExpression());
        simplified.put("scheduleParams", scheduleParams);
        
        return simplified;
    }
}
