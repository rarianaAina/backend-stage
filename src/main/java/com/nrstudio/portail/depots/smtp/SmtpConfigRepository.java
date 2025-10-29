package com.nrstudio.portail.depots.smtp;

import com.nrstudio.portail.domaine.smtp.ConfigurationSmtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SmtpConfigRepository extends JpaRepository<ConfigurationSmtp, Integer> {
    
    Optional<ConfigurationSmtp> findByEstActifTrue();
    
    @Modifying
    @Transactional
    @Query("UPDATE ConfigurationSmtp c SET c.estActif = false")
    void deactivateAll();
}