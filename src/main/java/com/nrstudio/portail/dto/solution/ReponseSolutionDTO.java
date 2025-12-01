package com.nrstudio.portail.dto.solution;

import java.time.LocalDateTime;

public class ReponseSolutionDTO {
    private Integer id;
    private Integer solutionId;
    private Boolean estValide;
    private String commentaire;
    private LocalDateTime dateReponse;
    private Integer creeParId;
    private String creeParNom;
    private String canalReponse;
    
    // Constructeurs
    public ReponseSolutionDTO() {}
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getSolutionId() { return solutionId; }
    public void setSolutionId(Integer solutionId) { this.solutionId = solutionId; }
    
    public Boolean getEstValide() { return estValide; }
    public void setEstValide(Boolean estValide) { this.estValide = estValide; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    
    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }
    
    public Integer getCreeParId() { return creeParId; }
    public void setCreeParId(Integer creeParId) { this.creeParId = creeParId; }
    
    public String getCreeParNom() { return creeParNom; }
    public void setCreeParNom(String creeParNom) { this.creeParNom = creeParNom; }
    
    public String getCanalReponse() { return canalReponse; }
    public void setCanalReponse(String canalReponse) { this.canalReponse = canalReponse; }
}