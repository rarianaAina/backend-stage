package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {
    Optional<NotificationTemplate> findByCodeAndActifTrue(String code);
    List<NotificationTemplate> findByActifTrue();

    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.actif = true AND nt.canal = :canal")
    List<NotificationTemplate> findActiveTemplatesByCanal(@Param("canal") String canal);
    
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.actif = true AND nt.canal = :canal")
    List<NotificationTemplate> findByCanalAndActifTrue(@Param("canal") String canal);
    
    // NOUVELLES MÉTHODES POUR LA GESTION
    
    /**
     * Trouver par code (sans filtre actif)
     */
    Optional<NotificationTemplate> findByCode(String code);
    
    /**
     * Trouver tous les templates (même inactifs)
     */
    List<NotificationTemplate> findAll();
    
    /**
     * Rechercher par libellé (avec like)
     */
    @Query("SELECT nt FROM NotificationTemplate nt WHERE nt.libelle LIKE %:libelle% AND nt.actif = true")
    List<NotificationTemplate> findByLibelleContaining(@Param("libelle") String libelle);
    
    /**
     * Compter les templates actifs
     */
    @Query("SELECT COUNT(nt) FROM NotificationTemplate nt WHERE nt.actif = true")
    long countActiveTemplates();
    
    /**
     * Récupérer les templates inactifs
     */
    List<NotificationTemplate> findByActifFalse();
}