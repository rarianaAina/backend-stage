package com.nrstudio.portail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scheduling")
public class SchedulingConfig {

    private String crmPersonSyncCron;

    // Getter et Setter
    public String getCrmPersonSyncCron() {
        return crmPersonSyncCron;
    }

    public void setCrmPersonSyncCron(String crmPersonSyncCron) {
        this.crmPersonSyncCron = crmPersonSyncCron;
    }
}