package com.nrstudio.portail.controleurs.smtp;

import com.nrstudio.portail.domaine.smtp.ConfigurationSmtp;
import com.nrstudio.portail.services.smtp.SmtpConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/configurations/email")
@CrossOrigin(origins = "*")
public class EmailConfigControleur {
    
    private final SmtpConfigService smtpConfigService;
    
    public EmailConfigControleur(SmtpConfigService smtpConfigService) {
        this.smtpConfigService = smtpConfigService;
    }
    
    @GetMapping
    public ResponseEntity<?> getEmailConfig() {
        try {
            ConfigurationSmtp config = smtpConfigService.getActiveConfig();
            // Ne pas renvoyer le mot de passe déchiffré
            config.setPassword(""); // ou masquer complètement
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur lors de la récupération de la configuration: " + e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> updateEmailConfig(@RequestBody ConfigurationSmtp config) {
        try {
            smtpConfigService.updateConfig(config);
            return ResponseEntity.ok().body(Map.of("message", "Configuration email mise à jour avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<?> testEmailConfig(@RequestBody ConfigurationSmtp config) {
        try {
            // Créer un mailSender temporaire pour tester
            JavaMailSender testMailSender = createTestMailSender(config);
            
            MimeMessage message = testMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(config.getUsername()); // Envoyer à soi-même pour le test
            helper.setSubject("✅ Test de configuration SMTP - Portail Client");
            helper.setText(createTestEmailContent(), true); // true pour HTML
            
            testMailSender.send(message);
            
            return ResponseEntity.ok().body(Map.of("message", "Email de test envoyé avec succès à " + config.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Échec de l'envoi: " + e.getMessage()));
        }
    }
    
    /**
     * Crée un JavaMailSender temporaire pour tester la configuration
     */
    private JavaMailSender createTestMailSender(ConfigurationSmtp config) {
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
            props.put("mail.smtp.starttls.enable", "false"); // IMPORTANT: désactiver STARTTLS
        } else if (config.getPort() == 587) {
            // Port 587 - STARTTLS
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", config.getHost());
        } else {
            // Autres ports
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", config.getHost());
        }
        
        mailSender.setJavaMailProperties(props);
        
        return mailSender;
    }
    
    /**
     * Crée le contenu HTML de l'email de test
     */
    private String createTestEmailContent() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }\n" +
                "        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }\n" +
                "        .success { color: #10B981; font-weight: bold; font-size: 18px; }\n" +
                "        .info { background: #e0f2fe; padding: 15px; border-radius: 5px; margin: 15px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h1>Test Réussi !</h1>\n" +
                "            <p>Configuration SMTP Opérationnelle</p>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p class=\"success\">Votre configuration SMTP fonctionne parfaitement !</p>\n" +
                "            \n" +
                "            <div class=\"info\">\n" +
                "                <h3>Détails de la configuration :</h3>\n" +
                "                <p><strong>Serveur :</strong> SMTP</p>\n" +
                "                <p><strong>Statut :</strong> Configuration validée avec succès</p>\n" +
                "                <p><strong>Date du test :</strong> " + java.time.LocalDateTime.now() + "</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p>Vous pouvez maintenant utiliser le système de notification email de votre portail client.</p>\n" +
                "            \n" +
                "            <p><strong>Prochaines étapes :</strong></p>\n" +
                "            <ul>\n" +
                "                <li>Configurer les workflows de notification</li>\n" +
                "                <li>Personnaliser les templates d'emails</li>\n" +
                "                <li>Tester les notifications automatiques</li>\n" +
                "            </ul>\n" +
                "            \n" +
                "            <p style=\"margin-top: 30px; font-size: 12px; color: #666;\">\n" +
                "                Cet email a été généré automatiquement par le système de test de configuration.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}