package com.nrstudio.portail.dto;

public class CodeValidationRequest {
    private String utilisateurId;
    private String code;

    // Constructeurs
    public CodeValidationRequest() {}

    public CodeValidationRequest(String utilisateurId, String code) {
        this.utilisateurId = utilisateurId;
        this.code = code;
    }

    // Getters et Setters
    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}