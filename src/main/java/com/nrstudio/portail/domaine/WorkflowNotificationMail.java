package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workflow_notification_mail",
       uniqueConstraints = @UniqueConstraint(columnNames = {"ordre", "type_notification_id"}))
public class WorkflowNotificationMail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "ordre", nullable = false)
    private Integer ordre;
    
    @Column(name = "utilisateur_id", nullable = false)
    private Integer utilisateurId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_notification_id", nullable = false)
    private TypeNotification typeNotification;
    
    @Column(name = "est_actif", nullable = false)
    private Boolean estActif = true;
    
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(name = "date_modification")
    private LocalDateTime dateModification;
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
    public TypeNotification getTypeNotification() { return typeNotification; }
    public void setTypeNotification(TypeNotification typeNotification) { this.typeNotification = typeNotification; }
    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }
}