package com.nrstudio.portail.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.nrstudio.portail.domaine.NotificationTemplate;
import com.nrstudio.portail.services.smtp.SmtpConfigService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private final SmtpConfigService smtpConfigService;
    private final JavaMailSender mailSender;
    private final NotificationTemplateService templateService;
    
    // Cache pour stocker les templates en mémoire
    private final Map<String, NotificationTemplate> templateCache = new ConcurrentHashMap<>();

    public EmailNotificationService(SmtpConfigService smtpConfigService, 
                                  NotificationTemplateService templateService) {
        this.smtpConfigService = smtpConfigService;
        this.templateService = templateService;
        this.mailSender = smtpConfigService.getMailSender();
        preloadTemplates();
    }
    
    private void preloadTemplates() {
        List<NotificationTemplate> templates = templateService.getAllActiveTemplates();
        templates.forEach(template -> 
            templateCache.put(template.getCode().toLowerCase(), template)
        );
    }
    
    private void refreshTemplateCache() {
        templateCache.clear();
        preloadTemplates();
    }

    private void sendEmail(String destinataire, String sujet, String texteHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String fromEmail = smtpConfigService.getActiveConfig().getUsername();
            helper.setFrom(fromEmail, "Portail Client");
            helper.setTo(destinataire);
            helper.setSubject(sujet);
            helper.setText(texteHtml, true);
            
            mailSender.send(message);
            System.out.println("✓ Email envoyé avec succès à : " + destinataire);
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'envoi de l'email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendTemplateEmail(String destinataire, String templateCode, Map<String, Object> variables) {
        try {
            NotificationTemplate template = templateCache.get(templateCode.toLowerCase());
            
            if (template == null) {
                // Recharger le cache au cas où
                refreshTemplateCache();
                template = templateCache.get(templateCode.toLowerCase());
                
                if (template == null) {
                    throw new IllegalArgumentException("Template non trouvé: " + templateCode);
                }
            }
            
            String sujet = templateService.processTemplate(template.getSujet(), variables);
            String contenuHtml = templateService.processTemplate(template.getContenuHtml(), variables);
            
            // Encapsuler dans le template de base si nécessaire
            String emailComplet = createEmailTemplate(contenuHtml, templateCode);
            System.out.println(emailComplet);
            sendEmail(destinataire, sujet, emailComplet);
            
        } catch (Exception e) {
            System.err.println("✗ Erreur lors de l'envoi de l'email avec template: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthodes existantes adaptées pour utiliser les templates
    public void envoyerNotificationTicketCree(String destinataire, String reference, String titre) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reference", reference);
        variables.put("titre", titre);
        
        sendTemplateEmail(destinataire, "TICKET_CREATION", variables);
    }

    public void envoyerNotificationCodeValidation(String destinataire, String code) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("code", code);
        
        sendTemplateEmail(destinataire, "CODE_VALIDATION", variables);
    }

    public void envoyerNotificationChangementStatut(String destinataire, String reference, String ancienStatut, String nouveauStatut) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reference", reference);
        variables.put("ancienStatut", ancienStatut);
        variables.put("nouveauStatut", nouveauStatut);
        
        sendTemplateEmail(destinataire, "STATUT_CHANGE", variables);
    }

    // Gardez la méthode createEmailTemplate existante pour le wrapper CSS
    private String createEmailTemplate(String contenuHtml, String typeNotification) {
        // Votre méthode existante avec le CSS...
        // (le code CSS reste le même)
        
        // String headerColor = getHeaderColor(typeNotification);
        // String icon = getIcon(typeNotification);
        
        return "<!DOCTYPE html>\n" +
                // ... votre template CSS existant ...
                "        <div class=\"content\">\n" +
                "            " + contenuHtml + "\n" +
                "        </div>\n" +
                // ... reste du template ...
                "</html>";
    }

    public void envoyerEmailAvecTemplate(String destinataire, String sujet, String contenuHtml) {
        // Utiliser le template de base avec le contenu personnalisé
        String emailComplet = createEmailTemplate(contenuHtml, "custom");
        sendEmail(destinataire, sujet, emailComplet);
    }

    private String getHeaderColor(String type) {
        switch (type) {
            case "creation": return "#10B981"; // Vert
            case "statut": return "#3B82F6";   // Bleu
            case "intervention": return "#F59E0B"; // Orange
            case "validation": return "#8B5CF6"; // Violet
            case "cloture": return "#EF4444";  // Rouge
            default: return "#6B7280"; // Gris
        }
    }

    private String getIcon(String type) {
        switch (type) {
            case "creation": return "🎫";
            case "statut": return "🔄";
            case "intervention": return "📅";
            case "validation": return "✅";
            case "cloture": return "🏁";
            default: return "📧";
        }
    }

    private String lightenColor(String color, int percent) {
        // Simplification pour l'exemple - en réalité il faudrait parser la couleur hex
        return color;
    }
}