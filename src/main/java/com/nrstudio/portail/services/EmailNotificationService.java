package com.nrstudio.portail.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.nrstudio.portail.services.smtp.SmtpConfigService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {

    private final SmtpConfigService smtpConfigService;
    private final JavaMailSender mailSender;

    public EmailNotificationService(SmtpConfigService smtpConfigService) {
        this.smtpConfigService = smtpConfigService;
        this.mailSender = smtpConfigService.getMailSender();
    }

    private void sendEmail(String destinataire, String sujet, String texteHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Utiliser l'email de la configuration SMTP comme expéditeur
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

    // ⬇️ GARDEZ TOUTES VOS AUTRES MÉTHODES EXISTANTES ⬇️
    // Elles restent exactement les mêmes, seul le constructeur et sendEmail() changent

    private String createEmailTemplate(String contenuHtml, String typeNotification) {
        String headerColor = getHeaderColor(typeNotification);
        String icon = getIcon(typeNotification);
        
        return "<!DOCTYPE html>\n" +
                "<html lang=\"fr\">\n" +
                "<head>\n" +
                "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Notification Portail Client</title>\n" +
                "    <style>\n" +
                "        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');\n" +
                "        \n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: 'Inter', Arial, sans-serif;\n" +
                "            line-height: 1.7;\n" +
                "            color: #2D3748;\n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            margin: 0;\n" +
                "            padding: 20px;\n" +
                "            min-height: 100vh;\n" +
                "        }\n" +
                "        \n" +
                "        .container {\n" +
                "            max-width: 650px;\n" +
                "            margin: 0 auto;\n" +
                "            background: white;\n" +
                "            border-radius: 20px;\n" +
                "            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);\n" +
                "            overflow: hidden;\n" +
                "            backdrop-filter: blur(10px);\n" +
                "        }\n" +
                "        \n" +
                "        .header {\n" +
                "            background: " + headerColor + ";\n" +
                "            color: white;\n" +
                "            padding: 40px 30px;\n" +
                "            text-align: center;\n" +
                "            position: relative;\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        \n" +
                "        .header::before {\n" +
                "            content: '';\n" +
                "            position: absolute;\n" +
                "            top: -50%;\n" +
                "            left: -50%;\n" +
                "            width: 200%;\n" +
                "            height: 200%;\n" +
                "            background: radial-gradient(circle, rgba(255,255,255,0.1) 1px, transparent 1px);\n" +
                "            background-size: 20px 20px;\n" +
                "            animation: float 20s infinite linear;\n" +
                "        }\n" +
                "        \n" +
                "        @keyframes float {\n" +
                "            0% { transform: translate(0, 0) rotate(0deg); }\n" +
                "            100% { transform: translate(-20px, -20px) rotate(360deg); }\n" +
                "        }\n" +
                "        \n" +
                "        .header-content {\n" +
                "            position: relative;\n" +
                "            z-index: 2;\n" +
                "        }\n" +
                "        \n" +
                "        .icon {\n" +
                "            font-size: 48px;\n" +
                "            margin-bottom: 15px;\n" +
                "            display: block;\n" +
                "            filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));\n" +
                "        }\n" +
                "        \n" +
                "        .header h1 {\n" +
                "            margin: 0;\n" +
                "            font-size: 28px;\n" +
                "            font-weight: 700;\n" +
                "            letter-spacing: -0.5px;\n" +
                "            text-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        \n" +
                "        .header p {\n" +
                "            margin: 10px 0 0 0;\n" +
                "            font-size: 16px;\n" +
                "            opacity: 0.9;\n" +
                "            font-weight: 400;\n" +
                "        }\n" +
                "        \n" +
                "        .content {\n" +
                "            padding: 40px 35px;\n" +
                "            background: white;\n" +
                "        }\n" +
                "        \n" +
                "        .info-card {\n" +
                "            background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);\n" +
                "            border: none;\n" +
                "            border-radius: 16px;\n" +
                "            padding: 30px;\n" +
                "            margin: 25px 0;\n" +
                "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);\n" +
                "            border-left: 6px solid " + headerColor + ";\n" +
                "            transition: all 0.3s ease;\n" +
                "        }\n" +
                "        \n" +
                "        .info-card:hover {\n" +
                "            transform: translateY(-3px);\n" +
                "            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);\n" +
                "        }\n" +
                "        \n" +
                "        .info-card h3 {\n" +
                "            margin: 0 0 25px 0;\n" +
                "            font-size: 20px;\n" +
                "            font-weight: 600;\n" +
                "            color: " + headerColor + ";\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            gap: 10px;\n" +
                "        }\n" +
                "        \n" +
                "        .info-grid {\n" +
                "            display: grid;\n" +
                "            grid-template-columns: 1fr 2fr;\n" +
                "            gap: 15px;\n" +
                "            align-items: start;\n" +
                "        }\n" +
                "        \n" +
                "        .info-row {\n" +
                "            display: contents;\n" +
                "        }\n" +
                "        \n" +
                "        .info-label {\n" +
                "            font-weight: 600;\n" +
                "            color: #4A5568;\n" +
                "            font-size: 14px;\n" +
                "            padding: 12px 0;\n" +
                "            border-bottom: 1px solid #E2E8F0;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "        }\n" +
                "        \n" +
                "        .info-value {\n" +
                "            font-weight: 500;\n" +
                "            color: #2D3748;\n" +
                "            font-size: 15px;\n" +
                "            padding: 12px 0;\n" +
                "            border-bottom: 1px solid #E2E8F0;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: flex-start;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "        \n" +
                "        .info-grid .info-label:last-child,\n" +
                "        .info-grid .info-value:last-child {\n" +
                "            border-bottom: none;\n" +
                "        }\n" +
                "        \n" +
                "        .highlight {\n" +
                "            background: linear-gradient(135deg, " + headerColor + " 0%, " + lightenColor(headerColor, 20) + " 100%);\n" +
                "            color: white;\n" +
                "            padding: 8px 16px;\n" +
                "            border-radius: 20px;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 14px;\n" +
                "            display: inline-block;\n" +
                "            box-shadow: 0 4px 12px rgba(0,0,0,0.15);\n" +
                "        }\n" +
                "        \n" +
                "        .footer {\n" +
                "            background: linear-gradient(135deg, #1A202C 0%, #2D3748 100%);\n" +
                "            color: white;\n" +
                "            padding: 35px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .footer-content {\n" +
                "            max-width: 400px;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        \n" +
                "        .footer h3 {\n" +
                "            margin: 0 0 15px 0;\n" +
                "            font-size: 18px;\n" +
                "            font-weight: 600;\n" +
                "            color: #CBD5E0;\n" +
                "        }\n" +
                "        \n" +
                "        .footer p {\n" +
                "            margin: 8px 0;\n" +
                "            font-size: 14px;\n" +
                "            color: #A0AEC0;\n" +
                "            line-height: 1.6;\n" +
                "        }\n" +
                "        \n" +
                "        .badge {\n" +
                "            background: rgba(255,255,255,0.1);\n" +
                "            padding: 6px 12px;\n" +
                "            border-radius: 12px;\n" +
                "            font-size: 12px;\n" +
                "            font-weight: 500;\n" +
                "            margin-top: 15px;\n" +
                "            display: inline-block;\n" +
                "        }\n" +
                "        \n" +
                "        .action-button {\n" +
                "            display: inline-block;\n" +
                "            background: linear-gradient(135deg, " + headerColor + " 0%, " + lightenColor(headerColor, 20) + " 100%);\n" +
                "            color: white;\n" +
                "            padding: 14px 32px;\n" +
                "            border-radius: 12px;\n" +
                "            text-decoration: none;\n" +
                "            font-weight: 600;\n" +
                "            font-size: 15px;\n" +
                "            margin: 20px 0;\n" +
                "            box-shadow: 0 6px 20px rgba(0,0,0,0.15);\n" +
                "            transition: all 0.3s ease;\n" +
                "        }\n" +
                "        \n" +
                "        .action-button:hover {\n" +
                "            transform: translateY(-2px);\n" +
                "            box-shadow: 0 10px 25px rgba(0,0,0,0.2);\n" +
                "        }\n" +
                "        \n" +
                "        @media (max-width: 600px) {\n" +
                "            body { padding: 10px; }\n" +
                "            .content { padding: 25px 20px; }\n" +
                "            .header { padding: 30px 20px; }\n" +
                "            .header h1 { font-size: 24px; }\n" +
                "            .info-card { padding: 20px; }\n" +
                "            .info-grid { grid-template-columns: 1fr; gap: 8px; }\n" +
                "            .info-label, .info-value { padding: 8px 0; border-bottom: none; }\n" +
                "            .info-value { margin-bottom: 12px; }\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <div class=\"header-content\">\n" +
                "                <h1>Portail Client</h1>\n" +
                "                <p>Notification System</p>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            " + contenuHtml + "\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <div class=\"footer-content\">\n" +
                "                <h3>Votre equipe de support</h3>\n" +
                "                <p>Nous sommes la pour vous accompagner</p>\n" +
                "                <p style=\"font-size: 12px; opacity: 0.7; margin-top: 15px;\">\n" +
                "                    Message genere automatiquement • Ne pas repondre\n" +
                "                </p>\n" +
                "                <div class=\"badge\">Service Premium</div>\n" +
                "            </div>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
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

    public void envoyerNotificationTicketCree(String destinataire, String reference, String titre) {
        String sujet = "Nouveau Ticket - " + reference;
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Une nouvelle demande a ete enregistree dans notre systeme.</p>\n" +
            "<div class='info-card'>\n" +
            "    <h3>Details du Ticket</h3>\n" +
            "    <div class='info-grid'>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Reference</div>\n" +
            "            <div class='info-value'><span class='highlight'>" + reference + "</span></div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Titre</div>\n" +
            "            <div class='info-value'>" + titre + "</div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Statut Actuel</div>\n" +
            "            <div class='info-value' style=\"color: #F59E0B;\">En Attente</div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div style=\"text-align: center; margin: 30px 0;\">\n" +
            "    <a href=\"#\" class=\"action-button\">Voir le Ticket</a>\n" +
            "</div>\n" +
            "<p style=\"text-align: center; color: #718096; font-size: 14px;\">Suivez l'avancement en temps reel sur votre portail</p>";
        
        System.out.println("Envoi notification creation ticket a : " + destinataire);
        sendEmail(destinataire, sujet, createEmailTemplate(texteHtml, "creation"));
    }

    // Envoyer Code de Validation
    public void envoyerNotificationCodeValidation(String destinataire, String code) {
        
        String sujet = "🔐 Code de Validation - Portail Client";
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Voici votre code de validation a usage unique :</p>\n" +
            "<div style=\"text-align: center; margin: 30px 0;\">\n" +
            "    <span class=\"highlight\" style=\"font-size: 32px; letter-spacing: 8px; padding: 15px 30px;\">" + code + "</span>\n" +
            "</div>\n" +
            "<p style=\"color: #718096; font-size: 14px;\">Ce code est valide pendant 10 minutes. Ne le partagez avec personne.</p>";
            sendEmail(destinataire, sujet, texteHtml);
    }

    public void envoyerEmailAvecTemplate(String destinataire, String sujet, String contenuHtml) {
        // Utiliser le template de base avec le contenu personnalisé
        String emailComplet = createEmailTemplate(contenuHtml, "custom");
        sendEmail(destinataire, sujet, emailComplet);
    }

    public void envoyerNotificationChangementStatut(String destinataire, String reference, String ancienStatut, String nouveauStatut) {
        String sujet = "Statut Modifie - " + reference;
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Le statut de votre ticket a ete mis a jour.</p>\n" +
            "<div class='info-card'>\n" +
            "    <h3>Evolution du Statut</h3>\n" +
            "    <div class='info-grid'>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Ticket</div>\n" +
            "            <div class='info-value'><span class='highlight'>" + reference + "</span></div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Ancien Statut</div>\n" +
            "            <div class='info-value' style=\"color: #EF4444;\">" + ancienStatut + "</div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Nouveau Statut</div>\n" +
            "            <div class='info-value' style=\"color: #10B981;\">" + nouveauStatut + "</div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div style=\"text-align: center; margin: 25px 0;\">\n" +
            "    <a href=\"#\" class=\"action-button\">Voir les Details</a>\n" +
            "</div>";
        
        sendEmail(destinataire, sujet, createEmailTemplate(texteHtml, "statut"));
    }

    public void envoyerNotificationInterventionCreee(String destinataire, String reference, String dateIntervention) {
        String sujet = "📅 Intervention Planifiee - " + reference;
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Une intervention a ete programmee pour votre demande.</p>\n" +
            "<div class='info-card'>\n" +
            "    <h3>📅 Planning d'Intervention</h3>\n" +
            "    <div class='info-grid'>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Reference</div>\n" +
            "            <div class='info-value'><span class='highlight'>" + reference + "</span></div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Date Proposee</div>\n" +
            "            <div class='info-value' style=\"color: #8B5CF6;\">📅 " + dateIntervention + "</div>\n" +
            "        </div>\n" +
            "        <div class='info-row'>\n" +
            "            <div class='info-label'>Action Requise</div>\n" +
            "            <div class='info-value' style=\"color: #F59E0B;\">⚠️ Confirmation</div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div style=\"text-align: center; margin: 30px 0;\">\n" +
            "    <a href=\"#\" class=\"action-button\">✅ Confirmer la Date</a>\n" +
            "    <a href=\"#\" class=\"action-button\" style=\"background: linear-gradient(135deg, #6B7280 0%, #9CA3AF 100%); margin-left: 10px;\">🔄 Proposer Autre Date</a>\n" +
            "</div>";
        
        sendEmail(destinataire, sujet, createEmailTemplate(texteHtml, "intervention"));
    }

    public void envoyerNotificationDateValidee(String destinataire, String reference, String dateIntervention) {
        String sujet = "✅ Date Confirmee - " + reference;
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Super ! La date d'intervention a ete validee avec succes.</p>\n" +
            "<div class='info-card'>\n" +
            "    <h3>✅ Rendez-vous Confirme</h3>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Ticket</span>\n" +
            "        <span class='info-value highlight'>" + reference + "</span>\n" +
            "    </div>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Date Retenue</span>\n" +
            "        <span class='info-value' style=\"color: #10B981; font-size: 16px;\">🗓️ " + dateIntervention + "</span>\n" +
            "    </div>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Statut</span>\n" +
            "        <span class='info-value' style=\"color: #10B981;\">🎯 Confirme</span>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div style=\"background: linear-gradient(135deg, #10B981 0%, #34D399 100%); color: white; padding: 25px; border-radius: 16px; text-align: center; margin: 25px 0;\">\n" +
            "    <h3 style=\"margin: 0 0 10px 0; font-size: 20px;\">🎉 Parfait !</h3>\n" +
            "    <p style=\"margin: 0; opacity: 0.9;\">Notre equipe interviendra a la date convenue. Preparation en cours...</p>\n" +
            "</div>";
        
        sendEmail(destinataire, sujet, createEmailTemplate(texteHtml, "validation"));
    }

    public void envoyerNotificationInterventionCloturee(String destinataire, String reference) {
        String sujet = "🏁 Intervention Terminee - " + reference;
        String texteHtml = 
            "<p style=\"font-size: 18px; font-weight: 500; margin-bottom: 25px; color: #2D3748;\">Bonjour,</p>\n" +
            "<p style=\"margin-bottom: 20px; color: #4A5568;\">Votre intervention a ete finalisee avec succes !</p>\n" +
            "<div class='info-card'>\n" +
            "    <h3>🏁 Bilan de l'Intervention</h3>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Reference</span>\n" +
            "        <span class='info-value highlight'>" + reference + "</span>\n" +
            "    </div>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Statut Final</span>\n" +
            "        <span class='info-value' style=\"color: #10B981;\">✅ Complete</span>\n" +
            "    </div>\n" +
            "    <div class='info-item'>\n" +
            "        <span class='info-label'>Prochaine Etape</span>\n" +
            "        <span class='info-value' style=\"color: #8B5CF6;\">📄 Validation du Rapport</span>\n" +
            "    </div>\n" +
            "</div>\n" +
            "<div style=\"background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%); color: white; padding: 25px; border-radius: 16px; text-align: center; margin: 25px 0;\">\n" +
            "    <h3 style=\"margin: 0 0 10px 0; font-size: 20px;\">📋 Rapport d'Intervention</h3>\n" +
            "    <p style=\"margin: 0 0 15px 0; opacity: 0.9;\">Un compte-rendu detaille vous sera transmis sous 24h</p>\n" +
            "    <a href=\"#\" class=\"action-button\" style=\"background: rgba(255,255,255,0.2); border: 2px solid rgba(255,255,255,0.3);\">📥 Telecharger le Rapport</a>\n" +
            "</div>";
        
        sendEmail(destinataire, sujet, createEmailTemplate(texteHtml, "cloture"));
    }
}