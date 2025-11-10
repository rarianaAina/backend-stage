package com.nrstudio.portail.depots.solution;

import com.nrstudio.portail.domaine.solution.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolutionRepository extends JpaRepository<Solution, Integer> {
    Optional<Solution> findByIdExterneCrm(String idExterneCrm);
    boolean existsByIdExterneCrm(String idExterneCrm);

        // Trouver les solutions par référence (qui correspond à ticket.reference_id)
    List<Solution> findByReference(String reference);
    
    // Trouver les solutions non supprimées par référence
    @Query("SELECT s FROM Solution s WHERE s.reference = :reference AND s.supprime = false")
    List<Solution> findSolutionsActivesByReference(@Param("reference") String reference);
    
    // Trouver les solutions par référence avec un statut spécifique
    @Query("SELECT s FROM Solution s WHERE s.reference = :reference AND s.statut = :statut AND s.supprime = false")
    List<Solution> findByReferenceAndStatut(@Param("reference") String reference, @Param("statut") String statut);
    

}