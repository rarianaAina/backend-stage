package com.nrstudio.portail.services;

import com.nrstudio.portail.domaine.ValidationCode;
import com.nrstudio.portail.depots.ValidationCodeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class ValidationCodeService {

    private final ValidationCodeRepository validationCodeRepository;
    
    @Value("${app.validation.code.expiry-minutes:10}")
    private int expiryMinutes;
    
    @Value("${app.validation.code.max-attempts:3}")
    private int maxAttempts;

    @Autowired
    private EmailNotificationService emailNotificationService;
    
    public ValidationCodeService(ValidationCodeRepository validationCodeRepository) {
        this.validationCodeRepository = validationCodeRepository;
    }

    /**
     * Génère et sauvegarde un nouveau code de validation
     */
    public ValidationCode generateCode(String utilisateurId, String email) {
        // Désactiver les anciens codes non utilisés
        deactivateOldCodes(utilisateurId);
        
        // Générer un code à 4 chiffres
        String code = generateRandomCode();
        
        // Calculer la date d'expiration
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);
        
        // Créer et sauvegarder le code
        ValidationCode validationCode = new ValidationCode(utilisateurId, code, expiresAt);
        validationCode.setMaxAttempts(maxAttempts);
        emailNotificationService.envoyerNotificationCodeValidation(email, code);
        return validationCodeRepository.save(validationCode);
    }

    /**
     * Valide un code pour un utilisateur
     */
    public boolean validateCode(String utilisateurId, String code) {
        Optional<ValidationCode> optionalCode = validationCodeRepository
                .findByUtilisateurIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
                    utilisateurId, code, LocalDateTime.now());
        
        if (optionalCode.isPresent()) {
            ValidationCode validationCode = optionalCode.get();
            
            // Vérifier si le code n'a pas dépassé le nombre maximal de tentatives
            if (validationCode.getAttempts() >= validationCode.getMaxAttempts()) {
                return false;
            }
            
            // Marquer le code comme utilisé
            validationCode.markAsUsed();
            validationCodeRepository.save(validationCode);
            return true;
        } else {
            // Incrémenter le compteur de tentatives pour ce code (si existant mais invalide)
            incrementAttempts(utilisateurId, code);
            return false;
        }
    }

    /**
     * Vérifie si un code est valide sans le consommer
     */
    public boolean isCodeValid(String utilisateurId, String code) {
        Optional<ValidationCode> optionalCode = validationCodeRepository
                .findByUtilisateurIdAndCodeAndIsUsedFalseAndExpiresAtAfter(
                    utilisateurId, code, LocalDateTime.now());
        
        return optionalCode.isPresent() && 
               optionalCode.get().getAttempts() < optionalCode.get().getMaxAttempts();
    }

    /**
     * Incrémente le compteur de tentatives pour un code
     */
    private void incrementAttempts(String utilisateurId, String code) {
        // Trouver le code le plus récent non utilisé pour cet utilisateur/code
        validationCodeRepository.findByUtilisateurIdAndIsUsedFalse(utilisateurId)
                .stream()
                .filter(vc -> vc.getCode().equals(code))
                .findFirst()
                .ifPresent(vc -> {
                    vc.incrementAttempts();
                    validationCodeRepository.save(vc);
                });
    }

    /**
     * Désactive les anciens codes non utilisés pour un utilisateur
     */
    private void deactivateOldCodes(String utilisateurId) {
        validationCodeRepository.findByUtilisateurIdAndIsUsedFalse(utilisateurId)
                .forEach(vc -> {
                    vc.setIsUsed(true); // Marquer comme utilisé pour le désactiver
                    validationCodeRepository.save(vc);
                });
    }

    /**
     * Génère un code numérique à 4 chiffres
     */
    private String generateRandomCode() {
        Random random = new Random();
        int number = random.nextInt(10000); // Génère un nombre entre 0 et 9999
        return String.format("%04d", number); // Formatte avec des zéros devant
    }

    /**
     * Nettoie les codes expirés de la base de données
     */
    public void cleanupExpiredCodes() {
        validationCodeRepository.deleteExpiredCodes(LocalDateTime.now().minusHours(1));
    }
}