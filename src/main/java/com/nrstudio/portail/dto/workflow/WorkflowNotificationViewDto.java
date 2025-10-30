package com.nrstudio.portail.dto.workflow;

public class WorkflowNotificationViewDto {
    private String typeNotificationCode;
    private Integer id;
    private Integer ordre;
    private Integer utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private Integer typeNotificationId;
    private String typeNotificationLibelle;

    // Constructeur pour la projection
    public WorkflowNotificationViewDto(String typeNotificationCode, Integer id, Integer ordre, 
                                      Integer utilisateurId, String utilisateurNom, String utilisateurPrenom,
                                      String utilisateurEmail, Integer typeNotificationId, String typeNotificationLibelle) {
        this.typeNotificationCode = typeNotificationCode;
        this.id = id;
        this.ordre = ordre;
        this.utilisateurId = utilisateurId;
        this.utilisateurNom = utilisateurNom;
        this.utilisateurPrenom = utilisateurPrenom;
        this.utilisateurEmail = utilisateurEmail;
        this.typeNotificationId = typeNotificationId;
        this.typeNotificationLibelle = typeNotificationLibelle;
    }

    // Getters et Setters
    public String getTypeNotificationCode() { return typeNotificationCode; }
    public void setTypeNotificationCode(String typeNotificationCode) { this.typeNotificationCode = typeNotificationCode; }
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }
    
    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public String getUtilisateurNom() { return utilisateurNom; }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }
    
    public String getUtilisateurPrenom() { return utilisateurPrenom; }
    public void setUtilisateurPrenom(String utilisateurPrenom) { this.utilisateurPrenom = utilisateurPrenom; }
    
    public String getUtilisateurEmail() { return utilisateurEmail; }
    public void setUtilisateurEmail(String utilisateurEmail) { this.utilisateurEmail = utilisateurEmail; }
    
    public Integer getTypeNotificationId() { return typeNotificationId; }
    public void setTypeNotificationId(Integer typeNotificationId) { this.typeNotificationId = typeNotificationId; }
    
    public String getTypeNotificationLibelle() { return typeNotificationLibelle; }
    public void setTypeNotificationLibelle(String typeNotificationLibelle) { this.typeNotificationLibelle = typeNotificationLibelle; }
}