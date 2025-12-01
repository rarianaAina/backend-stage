package com.nrstudio.portail.services.smtp;

import com.nrstudio.portail.depots.smtp.SmtpConfigRepository;
import com.nrstudio.portail.domaine.smtp.ConfigurationSmtp;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

@Service
public class SmtpConfigService {
    
    private final SmtpConfigRepository smtpConfigRepository;
    private JavaMailSender mailSender;
    private static final String SECRET_KEY = "YourSecretKey12345"; // À mettre dans les properties !
    
    public SmtpConfigService(SmtpConfigRepository smtpConfigRepository) {
        this.smtpConfigRepository = smtpConfigRepository;
        // Charger la configuration au démarrage
        try {
            this.mailSender = createMailSender();
        } catch (Exception e) {
            this.mailSender = null;
            System.err.println("Aucune configuration SMTP active trouvée au démarrage");
        }
    }
    
    public ConfigurationSmtp getActiveConfig() {
        return smtpConfigRepository.findByEstActifTrue()
                .orElseThrow(() -> new RuntimeException("Aucune configuration SMTP active trouvée"));
    }
    
    // @Transactional
    // public void updateConfig(ConfigurationSmtp newConfig) {
    //     // Désactiver toutes les configurations
    //     smtpConfigRepository.deactivateAll();
        
    //     // Chiffrer le mot de passe avant sauvegarde
    //     newConfig.setPassword(encrypt(newConfig.getPassword()));
    //     newConfig.setEstActif(true);
    //     newConfig.setDateModification(LocalDateTime.now());
        
    //     smtpConfigRepository.save(newConfig);
        
    //     // Recharger la configuration SMTP
    //     this.mailSender = createMailSender();
    // }

    @Transactional
    public void updateConfig(ConfigurationSmtp newConfig) {
        // Désactiver toutes les configurations
        smtpConfigRepository.deactivateAll();
        
        // newConfig.setPassword(encrypt(newConfig.getPassword()));
        
        newConfig.setEstActif(true);
        newConfig.setDateModification(LocalDateTime.now());
        
        smtpConfigRepository.save(newConfig);
        
        // Recharger la configuration SMTP
        this.mailSender = createMailSender();
    }
    
    private JavaMailSender createMailSender() {
        ConfigurationSmtp config = getActiveConfig();
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getHost());
        mailSender.setPort(config.getPort());
        mailSender.setUsername(config.getUsername());
        mailSender.setPassword(config.getPassword());
        
        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "false");
        
        // Configuration différente selon le port
        if (config.getPort() == 465) {
            // Port 465 - SSL direct
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", config.getHost());
            // Désactiver STARTTLS pour le port 465
            props.put("mail.smtp.starttls.enable", "false");
        } else if (config.getPort() == 587) {
            // Port 587 - STARTTLS
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", config.getHost());
        } else {
            // Autres ports - configuration par défaut
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", config.getHost());
        }
        
        mailSender.setJavaMailProperties(props);
        
        return mailSender;
    }
    public JavaMailSender getMailSender() {
        if (this.mailSender == null) {
            this.mailSender = createMailSender();
        }
        return this.mailSender;
    }
    
    // Méthodes de chiffrement/déchiffrement basiques
    private String encrypt(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement", e);
        }
    }
    
    private String decrypt(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement", e);
        }
    }
}