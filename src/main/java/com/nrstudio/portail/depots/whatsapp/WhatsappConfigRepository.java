package com.nrstudio.portail.depots.whatsapp;

import com.nrstudio.portail.domaine.whatsapp.ConfigurationWhatsApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsappConfigRepository extends JpaRepository<ConfigurationWhatsApp, Integer> {
    
    // Trouver la configuration active
    Optional<ConfigurationWhatsApp> findByEstActifTrue();
    
    // Trouver par nom de configuration
    Optional<ConfigurationWhatsApp> findByNomConfiguration(String nomConfiguration);
    
    // Lister toutes les configurations actives
    List<ConfigurationWhatsApp> findByEstActif(Boolean estActif);
    
    // Vérifier si une configuration existe avec le même nom (pour éviter les doublons)
    boolean existsByNomConfiguration(String nomConfiguration);
    
    // Trouver par ID du numéro de téléphone
    Optional<ConfigurationWhatsApp> findByPhoneNumberId(String phoneNumberId);
    
    // Compter les configurations actives
    @Query("SELECT COUNT(c) FROM ConfigurationWhatsApp c WHERE c.estActif = true")
    long countActiveConfigurations();
}