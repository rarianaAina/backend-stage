package com.nrstudio.portail.dto.template;

import java.time.LocalDateTime;

public class TemplateDTO {
    private Integer id;
    private String code;
    private String libelle;
    private String canal;
    private String sujet;
    private String contenuHtml;
    private Boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    
    // Constructeurs
    public TemplateDTO() {}
    
    public TemplateDTO(Integer id, String code, String libelle, String canal, String sujet, 
                      String contenuHtml, Boolean actif, LocalDateTime dateCreation, LocalDateTime dateMiseAJour) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.canal = canal;
        this.sujet = sujet;
        this.contenuHtml = contenuHtml;
        this.actif = actif;
        this.dateCreation = dateCreation;
        this.dateMiseAJour = dateMiseAJour;
    }
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
    
    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
    
    public String getContenuHtml() { return contenuHtml; }
    public void setContenuHtml(String contenuHtml) { this.contenuHtml = contenuHtml; }
    
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    
    public LocalDateTime getDateMiseAJour() { return dateMiseAJour; }
    public void setDateMiseAJour(LocalDateTime dateMiseAJour) { this.dateMiseAJour = dateMiseAJour; }
}