package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ValidationCodes")
public class ValidationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private String utilisateurId;

    @Column(nullable = false, length = 4)
    private String code;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiresat", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "isused", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "usedat")
    private LocalDateTime usedAt;

    @Column(nullable = false)
    private Integer attempts = 0;

    @Column(name = "maxattempts", nullable = false)
    private Integer maxAttempts = 3;

    // Constructeurs
    public ValidationCode() {}

    public ValidationCode(String utilisateurId, String code, LocalDateTime expiresAt) {
        this.utilisateurId = utilisateurId;
        this.code = code;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.isUsed = false;
        this.attempts = 0;
        this.maxAttempts = 3;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsUsed() { return isUsed; }
    public void setIsUsed(Boolean isUsed) { this.isUsed = isUsed; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }

    // Méthodes utilitaires
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isUsed && !isExpired() && attempts < maxAttempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
}