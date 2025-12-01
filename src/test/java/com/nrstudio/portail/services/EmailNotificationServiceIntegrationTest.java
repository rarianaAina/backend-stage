package com.nrstudio.portail.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.nrstudio.portail.services.notification.EmailNotificationService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = EmailNotificationService.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class EmailNotificationServiceIntegrationTest {

    @Autowired
    private EmailNotificationService emailService;

    @Test
    void testEnvoiEmailReel() {
        String destinataire = "rarianamiadana@gmail.com";
        String reference = "TEST-REEL-" + System.currentTimeMillis();
        String titre = "Test d'envoi réel d'email";

        System.out.println("=== DÉBUT TEST ENVOI RÉEL EMAIL ===");
        System.out.println("Destinataire: " + destinataire);
        System.out.println("Référence: " + reference);
        System.out.println("Titre: " + titre);

        // Cette méthode devrait envoyer un email réel
        assertDoesNotThrow(() -> {
            emailService.envoyerNotificationTicketCree(destinataire, reference, titre);
        }, "L'envoi d'email ne devrait pas lever d'exception");

        System.out.println("✅ Email envoyé avec succès!");
        System.out.println("📧 Vérifiez votre boîte de réception: " + destinataire);
        System.out.println("=== FIN TEST ENVOI RÉEL EMAIL ===");

        // Petite pause pour laisser le temps à l'email d'arriver
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}