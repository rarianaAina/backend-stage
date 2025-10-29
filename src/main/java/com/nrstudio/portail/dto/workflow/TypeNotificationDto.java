package com.nrstudio.portail.dto.workflow;

public class TypeNotificationDto {
    private Integer id;
    private String code;
    private String libelle;
    private String description;
    

    public TypeNotificationDto() {}
    
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}