package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.TypeNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TypeNotificationRepository extends JpaRepository<TypeNotification, Integer> {
    Optional<TypeNotification> findByCode(String code);
    List<TypeNotification> findByEstActifTrue();

    @Query("SELECT tn FROM TypeNotification tn WHERE tn.estActif = true ORDER BY tn.libelle")
    List<TypeNotification> findAllActifs();

    Optional<TypeNotification> findByCodeAndEstActifTrue(String code);
    
    @Query("SELECT tn FROM TypeNotification tn JOIN FETCH tn.template WHERE tn.estActif = true AND tn.template.actif = true")
    List<TypeNotification> findActiveTypesWithTemplates();

    
    // ✅ CORRECTION : Requête simplifiée
    @Query("SELECT tn FROM TypeNotification tn WHERE tn.estActif = true")
    List<TypeNotification> findActiveTypes();
    
    // ✅ CORRECTION : Requête avec join explicite
    @Query("SELECT tn FROM TypeNotification tn JOIN FETCH tn.template t WHERE tn.estActif = true AND t.actif = true")
    List<TypeNotification> findActiveTypesWithActiveTemplates();
    
 
}