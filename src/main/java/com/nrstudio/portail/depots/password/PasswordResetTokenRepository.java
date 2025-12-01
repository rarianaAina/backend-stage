package com.nrstudio.portail.depots.password;

import com.nrstudio.portail.domaine.password.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.utilisateurId = :userId AND prt.used = false")
    void invalidateExistingTokens(@Param("userId") Long userId);
    
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.utilisateurId = :userId AND prt.used = false AND prt.expiryDate > CURRENT_TIMESTAMP")
    Optional<PasswordResetToken> findValidTokenByUserId(@Param("userId") Integer userId);
    
    // AJOUTEZ CETTE MÉTHODE POUR TROUVER LES TOKENS ACTIFS
    @Query("SELECT prt FROM PasswordResetToken prt WHERE prt.utilisateurId = :userId AND prt.used = false")
    List<PasswordResetToken> findActiveTokensByUserId(@Param("userId") Integer userId);
}