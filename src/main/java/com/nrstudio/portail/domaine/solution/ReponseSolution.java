package com.nrstudio.portail.domaine.solution;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.nrstudio.portail.domaine.Utilisateur;

@Entity
@Table(name = "reponses_solution")
public class ReponseSolution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    private Solution solution;
    
    @Column(name = "est_valide", nullable = false)
    private Boolean estValide;
    
    @Column(name = "commentaire", columnDefinition = "NVARCHAR(MAX)")
    private String commentaire;
    
    @Column(name = "date_reponse", nullable = false)
    private LocalDateTime dateReponse;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par", nullable = false)
    private Utilisateur creePar;
    
    
    // Constructeurs
    public ReponseSolution() {}
    
    public ReponseSolution(Solution solution, Boolean estValide, String commentaire, Utilisateur creePar, String canalReponse) {
        this.solution = solution;
        this.estValide = estValide;
        this.commentaire = commentaire;
        this.creePar = creePar;
        this.dateReponse = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Solution getSolution() { return solution; }
    public void setSolution(Solution solution) { this.solution = solution; }
    
    public Boolean getEstValide() { return estValide; }
    public void setEstValide(Boolean estValide) { this.estValide = estValide; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    
    public LocalDateTime getDateReponse() { return dateReponse; }
    public void setDateReponse(LocalDateTime dateReponse) { this.dateReponse = dateReponse; }
    
    public Utilisateur getCreePar() { return creePar; }
    public void setCreePar(Utilisateur creePar) { this.creePar = creePar; }

}