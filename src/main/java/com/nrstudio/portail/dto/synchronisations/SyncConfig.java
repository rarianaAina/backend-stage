// package com.nrstudio.portail.dto.synchronisations;

// import com.nrstudio.portail.services.synchronisations.configurations.constants.SyncConstants;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.stereotype.Component;

// @Component
// @ConfigurationProperties(prefix = "scheduling")
// public class SyncConfig {
//     private boolean enabled = true;
//     private int batchSize = SyncConstants.BATCH_SIZE_DEFAULT;
//     private int delayMs = SyncConstants.DELAY_MS_DEFAULT;
    
//     // Utiliser les noms exacts de vos properties
//     private String crmCompanySyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmSolutionSyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmChSyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmPersonSyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmProductSyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmTicketSyncCron = SyncConstants.CRON_DEFAULT;
//     private String crmSolutickSyncCron = SyncConstants.CRON_DEFAULT;
    
//     // Getters et Setters
//     public boolean isEnabled() { return enabled; }
//     public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
//     public int getBatchSize() { return batchSize; }
//     public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
//     public int getDelayMs() { return delayMs; }
//     public void setDelayMs(int delayMs) { this.delayMs = delayMs; }
    
//     public String getCrmCompanySyncCron() { return crmCompanySyncCron; }
//     public void setCrmCompanySyncCron(String crmCompanySyncCron) { this.crmCompanySyncCron = crmCompanySyncCron; }
    
//     public String getCrmSolutionSyncCron() { return crmSolutionSyncCron; }
//     public void setCrmSolutionSyncCron(String crmSolutionSyncCron) { this.crmSolutionSyncCron = crmSolutionSyncCron; }
    
//     public String getCrmChSyncCron() { return crmChSyncCron; }
//     public void setCrmChSyncCron(String crmChSyncCron) { this.crmChSyncCron = crmChSyncCron; }
    
//     public String getCrmPersonSyncCron() { return crmPersonSyncCron; }
//     public void setCrmPersonSyncCron(String crmPersonSyncCron) { this.crmPersonSyncCron = crmPersonSyncCron; }
    
//     public String getCrmProductSyncCron() { return crmProductSyncCron; }
//     public void setCrmProductSyncCron(String crmProductSyncCron) { this.crmProductSyncCron = crmProductSyncCron; }
    
//     public String getCrmTicketSyncCron() { return crmTicketSyncCron; }
//     public void setCrmTicketSyncCron(String crmTicketSyncCron) { this.crmTicketSyncCron = crmTicketSyncCron; }
    
//     public String getCrmSolutickSyncCron() { return crmSolutickSyncCron; }
//     public void setCrmSolutickSyncCron(String crmSolutickSyncCron) { this.crmSolutickSyncCron = crmSolutickSyncCron; }
// }