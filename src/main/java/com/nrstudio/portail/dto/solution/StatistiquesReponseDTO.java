package com.nrstudio.portail.dto.solution;

public class StatistiquesReponseDTO {
    private Integer solutionId;
    private Long totalReponses;
    private Long reponsesValides;
    private Long reponsesRejetees;
    private Double tauxValidation;
    
    // Constructeurs
    public StatistiquesReponseDTO() {}
    
    // Getters et Setters
    public Integer getSolutionId() { return solutionId; }
    public void setSolutionId(Integer solutionId) { this.solutionId = solutionId; }
    
    public Long getTotalReponses() { return totalReponses; }
    public void setTotalReponses(Long totalReponses) { this.totalReponses = totalReponses; }
    
    public Long getReponsesValides() { return reponsesValides; }
    public void setReponsesValides(Long reponsesValides) { this.reponsesValides = reponsesValides; }
    
    public Long getReponsesRejetees() { return reponsesRejetees; }
    public void setReponsesRejetees(Long reponsesRejetees) { this.reponsesRejetees = reponsesRejetees; }
    
    public Double getTauxValidation() { return tauxValidation; }
    public void setTauxValidation(Double tauxValidation) { this.tauxValidation = tauxValidation; }
}