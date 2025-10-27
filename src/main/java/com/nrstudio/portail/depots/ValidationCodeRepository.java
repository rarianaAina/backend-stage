package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.ValidationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationCodeRepository extends JpaRepository<ValidationCode, Long> {
    
    // Trouver un code valide pour un utilisateur
    Optional<ValidationCode> findByUtilisateurIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
            String utilisateurId, String code, LocalDateTime now);
    
    // Trouver tous les codes non utilisés pour un utilisateur
    List<ValidationCode> findByUtilisateurIdAndIsUsedFalse(String utilisateurId);
    
    // Compter les tentatives récentes pour un utilisateur
    @Query("SELECT COUNT(v) FROM ValidationCode v WHERE v.utilisateurId = :utilisateurId AND v.createdAt > :since")
    Long countRecentAttempts(@Param("utilisateurId") String utilisateurId, 
                            @Param("since") LocalDateTime since);
    
    // Nettoyer les codes expirés
    @Modifying
    @Query("DELETE FROM ValidationCode v WHERE v.expiresAt < :cutoff")
    void deleteExpiredCodes(@Param("cutoff") LocalDateTime cutoff);
    
    // Incrémenter le compteur de tentatives
    @Modifying
    @Query("UPDATE ValidationCode v SET v.attempts = v.attempts + 1 WHERE v.id = :id")
    void incrementAttempts(@Param("id") Long id);
}