package com.nrstudio.portail.dto.solution;

public class CreateReponseSolutionRequest {
    private Integer solutionId;
    private Boolean estValide;
    private String commentaire;
    private Integer utilisateurId;
    
    // Constructeurs
    public CreateReponseSolutionRequest() {}
    
    // Getters et Setters
    public Integer getSolutionId() { return solutionId; }
    public void setSolutionId(Integer solutionId) { this.solutionId = solutionId; }
    
    public Boolean getEstValide() { return estValide; }
    public void setEstValide(Boolean estValide) { this.estValide = estValide; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }


    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
}