package com.nrstudio.portail.domaine.whatsapp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuration_whatsapp")
public class ConfigurationWhatsApp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "api_base_url", nullable = false)
    private String apiBaseUrl = "https://waba.360dialog.io/v1/messages";
    
    @Column(name = "api_key", nullable = false, length = 500)
    private String apiKey;
    
    @Column(name = "phone_number_id", nullable = false, length = 100)
    private String phoneNumberId;
    
    @Column(name = "business_account_id", length = 100)
    private String businessAccountId;
    
    @Column(name = "webhook_url", length = 255)
    private String webhookUrl;
    
    @Column(name = "webhook_token", length = 255)
    private String webhookToken;
    
    @Column(name = "est_actif")
    private Boolean estActif = true;
    
    @Column(name = "nom_configuration", nullable = false, length = 100)
    private String nomConfiguration = "Défaut";
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    // Constructeurs
    public ConfigurationWhatsApp() {}
    
    public ConfigurationWhatsApp(String apiKey, String phoneNumberId, String nomConfiguration) {
        this.apiKey = apiKey;
        this.phoneNumberId = phoneNumberId;
        this.nomConfiguration = nomConfiguration;
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
    
    // Méthode utilitaire pour mettre à jour la date de modification
    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "ConfigurationWhatsApp{" +
                "id=" + id +
                ", apiBaseUrl='" + apiBaseUrl + '\'' +
                ", phoneNumberId='" + phoneNumberId + '\'' +
                ", nomConfiguration='" + nomConfiguration + '\'' +
                ", estActif=" + estActif +
                '}';
    }
}