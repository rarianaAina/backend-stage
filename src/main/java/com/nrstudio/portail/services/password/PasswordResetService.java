package com.nrstudio.portail.services.password;

import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.password.PasswordResetToken;
import com.nrstudio.portail.services.notification.EmailNotificationService;
import com.nrstudio.portail.depots.password.PasswordResetTokenRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;

@Service
public class PasswordResetService {
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailNotificationService emailNotificationService;
    
    // Durée de validité du token (24 heures)
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    
    public PasswordResetService(UtilisateurRepository utilisateurRepository,
                              PasswordResetTokenRepository tokenRepository,
                              EmailNotificationService emailNotificationService) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepository = tokenRepository;
        this.emailNotificationService = emailNotificationService;
    }
    
    /**
     * Crée et envoie un token de réinitialisation
     */
/**
 * Crée et envoie un token de réinitialisation - VERSION SIMPLIFIÉE
 */
    public boolean createPasswordResetToken(String email) {
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Pour des raisons de sécurité, on ne révèle pas si l'email existe
            return true;
        }
        
        Utilisateur user = userOpt.get();
        
        try {
            // APPROCHE ALTERNATIVE: Ne pas invalider les anciens tokens
            // À la place, on vérifie s'il existe déjà un token valide
            Optional<PasswordResetToken> existingToken = tokenRepository.findValidTokenByUserId(user.getId());
            
            if (existingToken.isPresent()) {
                // Réutiliser le token existant s'il est encore valide
                PasswordResetToken validToken = existingToken.get();
                return sendPasswordResetEmail(user, validToken.getToken());
            }
            
            // Sinon, créer un nouveau token
            String tokenValue = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
            
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(tokenValue);
            resetToken.setUtilisateurId(user.getId());
            resetToken.setExpiryDate(expiryDate);
            resetToken.setUsed(false);
            
            tokenRepository.save(resetToken);
            
            // Envoyer l'email
            return sendPasswordResetEmail(user, tokenValue);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Valide un token de réinitialisation
     */
    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Vérifier si le token est utilisé ou expiré
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Réinitialise le mot de passe avec un token valide
     */
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        
        // Vérifier la validité du token
        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Trouver l'utilisateur
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(resetToken.getUtilisateurId());
        if (userOpt.isEmpty()) {
            return false;
        }
        
        Utilisateur user = userOpt.get();
        
        // Hasher le nouveau mot de passe
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setMotDePasseHash(hashedPassword.getBytes());
        
        // Marquer le token comme utilisé
        resetToken.setUsed(true);
        
        // Sauvegarder
        utilisateurRepository.save(user);
        tokenRepository.save(resetToken);
        
        return true;
    }
    
    /**
     * Envoie l'email de réinitialisation
     */
    private boolean sendPasswordResetEmail(Utilisateur user, String token) {
        try {
            String resetLink = "http://localhost:5174/reset-password?token=" + token;
            String subject = "Réinitialisation de votre mot de passe OPTIMADA";
            String body = buildEmailBody(user.getNom(), resetLink);
            
            // Utiliser la méthode publique
            emailNotificationService.sendPasswordResetEmail(user.getEmail(), subject, body);
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email de réinitialisation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Construit le corps de l'email
     */
    private String buildEmailBody(String userName, String resetLink) {
        return "<h3>Bonjour " + userName + ",</h3>" +
            "<p>Vous avez demandé la réinitialisation de votre mot de passe pour votre compte OPTIMADA.</p>" +
            "<p>Cliquez sur le bouton ci-dessous pour créer un nouveau mot de passe :</p>" +
            "<p style=\"text-align: center;\">" +
            "<a href=\"" + resetLink + "\" style=\"background-color: #EC4899; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;\">" +
            "Réinitialiser mon mot de passe</a></p>" +
            "<p>Ce lien expirera dans 24 heures.</p>" +
            "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>" +
            "<br>" +
            "<p>Cordialement,<br>L'équipe OPTIMADA</p>";
    }
}