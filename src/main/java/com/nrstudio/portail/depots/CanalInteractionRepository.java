package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.CanalInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface CanalInteractionRepository extends JpaRepository<CanalInteraction, Integer> {
    
    // Trouver par code
    Optional<CanalInteraction> findByCode(String code);
    
    // Trouver par libellé (recherche insensible à la casse)
    List<CanalInteraction> findByLibelleContainingIgnoreCase(String libelle);
    
    // Vérifier l'existence par code
    boolean existsByCode(String code);
    
    // Récupérer tous triés par libellé
    List<CanalInteraction> findAllByOrderByLibelleAsc();
}