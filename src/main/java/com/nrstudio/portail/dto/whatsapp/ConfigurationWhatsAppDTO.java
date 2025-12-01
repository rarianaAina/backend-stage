package com.nrstudio.portail.dto.whatsapp;

import java.time.LocalDateTime;
import com.nrstudio.portail.domaine.whatsapp.ConfigurationWhatsApp;

public class ConfigurationWhatsAppDTO {
    private Integer id;
    private String apiBaseUrl;
    private String apiKey;
    private String phoneNumberId;
    private String businessAccountId;
    private String webhookUrl;
    private String webhookToken;
    private Boolean estActif;
    private String nomConfiguration;
    private String description;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    
    // Constructeurs
    public ConfigurationWhatsAppDTO() {}
    
    public ConfigurationWhatsAppDTO(ConfigurationWhatsApp entity) {
        this.id = entity.getId();
        this.apiBaseUrl = entity.getApiBaseUrl();
        this.apiKey = entity.getApiKey();
        this.phoneNumberId = entity.getPhoneNumberId();
        this.businessAccountId = entity.getBusinessAccountId();
        this.webhookUrl = entity.getWebhookUrl();
        this.webhookToken = entity.getWebhookToken();
        this.estActif = entity.getEstActif();
        this.nomConfiguration = entity.getNomConfiguration();
        this.description = entity.getDescription();
        this.dateCreation = entity.getDateCreation();
        this.dateModification = entity.getDateModification();
    }
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
    
    public String getBusinessAccountId() { return businessAccountId; }
    public void setBusinessAccountId(String businessAccountId) { this.businessAccountId = businessAccountId; }
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public String getWebhookToken() { return webhookToken; }
    public void setWebhookToken(String webhookToken) { this.webhookToken = webhookToken; }
    
    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }
    
    public String getNomConfiguration() { return nomConfiguration; }
    public void setNomConfiguration(String nomConfiguration) { this.nomConfiguration = nomConfiguration; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
    
    // Méthode pour convertir en entité
    public ConfigurationWhatsApp toEntity() {
        ConfigurationWhatsApp entity = new ConfigurationWhatsApp();
        entity.setId(this.id);
        entity.setApiBaseUrl(this.apiBaseUrl);
        entity.setApiKey(this.apiKey);
        entity.setPhoneNumberId(this.phoneNumberId);
        entity.setBusinessAccountId(this.businessAccountId);
        entity.setWebhookUrl(this.webhookUrl);
        entity.setWebhookToken(this.webhookToken);
        entity.setEstActif(this.estActif);
        entity.setNomConfiguration(this.nomConfiguration);
        entity.setDescription(this.description);
        entity.setDateCreation(this.dateCreation);
        entity.setDateModification(this.dateModification);
        return entity;
    }
}