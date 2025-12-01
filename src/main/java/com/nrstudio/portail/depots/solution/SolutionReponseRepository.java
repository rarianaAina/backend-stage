package com.nrstudio.portail.depots.solution;

import com.nrstudio.portail.domaine.solution.ReponseSolution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionReponseRepository extends JpaRepository<ReponseSolution, Long> {
    
    // Trouver toutes les réponses d'une solution
    List<ReponseSolution> findBySolutionIdOrderByDateReponseDesc(Integer solutionId);
    
    // Trouver les réponses par statut de validation
    List<ReponseSolution> findByEstValide(Boolean estValide);
    
    // Trouver les réponses par utilisateur
    List<ReponseSolution> findByCreeParIdOrderByDateReponseDesc(Integer utilisateurId);
    
    // Trouver les réponses dans une période
    List<ReponseSolution> findByDateReponseBetween(LocalDateTime start, LocalDateTime end);
    
    // Trouver la dernière réponse d'une solution
    @Query("SELECT rs FROM ReponseSolution rs WHERE rs.solution.id = :solutionId ORDER BY rs.dateReponse DESC")
    Optional<ReponseSolution> findLatestBySolutionId(@Param("solutionId") Integer solutionId);
    
    // Compter les réponses par statut pour une solution
    @Query("SELECT COUNT(rs) FROM ReponseSolution rs WHERE rs.solution.id = :solutionId AND rs.estValide = :estValide")
    Long countBySolutionIdAndEstValide(@Param("solutionId") Integer solutionId, @Param("estValide") Boolean estValide);
    
    // Vérifier si une solution a déjà une réponse
    boolean existsBySolutionId(Integer solutionId);
}