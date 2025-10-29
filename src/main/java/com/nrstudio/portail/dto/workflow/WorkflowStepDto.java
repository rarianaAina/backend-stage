package com.nrstudio.portail.dto.workflow;

public class WorkflowStepDto {
    private Integer id;
    private Integer ordre;
    private Integer utilisateurId;
    private Integer typeNotificationId;
    private String utilisateurNom;
    private String typeNotificationLibelle;
    
    // Constructors
    public WorkflowStepDto() {}
    
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
    public Integer getTypeNotificationId() { return typeNotificationId; }
    public void setTypeNotificationId(Integer typeNotificationId) { this.typeNotificationId = typeNotificationId; }
    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }
    public String getTypeNotificationLibelle() { return typeNotificationLibelle; }
    public void setTypeNotificationLibelle(String typeNotificationLibelle) { this.typeNotificationLibelle = typeNotificationLibelle; }
}