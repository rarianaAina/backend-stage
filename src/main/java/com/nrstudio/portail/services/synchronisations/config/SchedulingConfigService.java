package com.nrstudio.portail.services.synchronisations.config;

import com.nrstudio.portail.depots.synchronisation.SchedulingConfigurationRepository;
import com.nrstudio.portail.domaine.synchronisation.SchedulingConfiguration;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SchedulingConfigService {
    
    private final SchedulingConfigurationRepository repository;
    private final Map<String, String> cronCache = new ConcurrentHashMap<>();
    private final Map<String, Boolean> enabledCache = new ConcurrentHashMap<>();
    
    public SchedulingConfigService(SchedulingConfigurationRepository repository) {
        this.repository = repository;
    }
    
    public String getCronExpression(String jobName) {
        return cronCache.computeIfAbsent(jobName, key -> {
            Optional<SchedulingConfiguration> config = repository.findByJobName(jobName);
            if (config.isPresent() && Boolean.TRUE.equals(config.get().getEnabled())) {
                return config.get().getCronExpression();
            }
            return getDefaultCronExpression(jobName);
        });
    }
    
    public boolean isJobEnabled(String jobName) {
        return enabledCache.computeIfAbsent(jobName, key -> {
            Optional<SchedulingConfiguration> config = repository.findByJobName(jobName);
            return config.map(SchedulingConfiguration::getEnabled).orElse(true);
        });
    }
    
    public void clearCache(String jobName) {
        cronCache.remove(jobName);
        enabledCache.remove(jobName);
    }
    
    public void clearAllCache() {
        cronCache.clear();
        enabledCache.clear();
    }
    
    private String getDefaultCronExpression(String jobName) {
        Map<String, String> defaults = Map.of(
            "crm-solutick-sync", "0 * * * * *",
            "crm-solution-sync", "0 * * * * *",
            "crm-company-sync", "0 0 3 * * *",
            "crm-ch-sync", "0 0 3 * * *",
            "crm-person-sync", "0 0 3 * * *",
            "crm-product-sync", "0 0 3 * * *",
            "crm-ticket-sync", "0 0 3 * * *"
        );
        return defaults.getOrDefault(jobName, "0 0 3 * * *");
    }
}