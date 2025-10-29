package com.nrstudio.portail.services;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailRealSendTest {

    private JavaMailSender createRealMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("rarianamiadana@gmail.com");
        mailSender.setPassword("qcuw wsrj dfen bkhz");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @Test
    void testEnvoiEmailDirect() {
        JavaMailSender mailSender = createRealMailSender();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("rarianamiadana@gmail.com");
        message.setTo("rarianamiadana@gmail.com");
        message.setSubject("🎉 Test Réel Email - " + System.currentTimeMillis());
        message.setText("""
            Félicitations ! 
            
            Ceci est un test réel d'envoi d'email depuis votre application Spring Boot.
            
            📅 Date: %s
            🔧 Environnement: Test d'intégration
            ✅ Statut: Fonctionne parfaitement !
            
            Si vous recevez ce message, votre configuration Gmail SMTP est correcte.
            
            Cordialement,
            Votre application Portail Client
            """.formatted(java.time.LocalDateTime.now()));

        System.out.println("🚀 Tentative d'envoi d'email réel...");

        assertDoesNotThrow(() -> {
            mailSender.send(message);
        }, "L'envoi d'email ne devrait pas échouer");

        System.out.println("✅ Email réel envoyé avec succès!");
        System.out.println("📧 Vérifiez votre boîte Gmail dans quelques instants...");
    }

    // @Test
    // void testEnvoiAvecEmailNotificationService() throws Exception {
    //     // Création manuelle du service pour éviter la configuration Spring complète
    //     JavaMailSender mailSender = createRealMailSender();
    //     EmailNotificationService emailService = new EmailNotificationService(mailSender);

    //     // Injection manuelle de l'email expéditeur
    //     java.lang.reflect.Field fromEmailField = EmailNotificationService.class.getDeclaredField("fromEmail");
    //     fromEmailField.setAccessible(true);
    //     fromEmailField.set(emailService, "rarianamiadana@gmail.com");

    //     String destinataire = "rarianamiadana@gmail.com";
    //     String reference = "TCK-MANUEL-" + System.currentTimeMillis();
    //     String titre = "Test manuel email service";

    //     System.out.println("=== TEST AVEC EMAIL NOTIFICATION SERVICE ===");

    //     assertDoesNotThrow(() -> {
    //         emailService.envoyerNotificationTicketCree(destinataire, reference, titre);
    //     });

    //     System.out.println("✅ Email envoyé via EmailNotificationService!");
    //}
}