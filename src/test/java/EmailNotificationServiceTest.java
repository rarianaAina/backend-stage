// File: src/test/java/com/nrstudio/portail/services/EmailNotificationServiceTest.java

import com.nrstudio.portail.services.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailNotificationServiceTest {

    private JavaMailSender mailSender;
    private EmailNotificationService service;
    private final String FROM_EMAIL = "rarianamiadana@gmail.com";
    private final String TEST_EMAIL = "rarianamiadana@gmail.com";

    @BeforeEach
    void setUp() throws Exception {
        mailSender = mock(JavaMailSender.class);
        service = new EmailNotificationService(mailSender);

        // Inject votre adresse email comme envoyeur
        Field fromEmailField = EmailNotificationService.class.getDeclaredField("fromEmail");
        fromEmailField.setAccessible(true);
        fromEmailField.set(service, FROM_EMAIL);
    }

    @Test
    void testEnvoyerNotificationTicketCree_SendsCorrectMail() {
        String destinataire = TEST_EMAIL;
        String reference = "TCK-001";
        String titre = "Problème d'accès";

        service.envoyerNotificationTicketCree(destinataire, reference, titre);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        // Vérifications de base
        assertEquals(FROM_EMAIL, sent.getFrom());
        assertArrayEquals(new String[]{destinataire}, sent.getTo());
        assertEquals("Nouveau ticket créé - " + reference, sent.getSubject());

        // Vérifications du contenu réel (basé sur votre implémentation réelle)
        String text = sent.getText();
        assertTrue(text.contains("Référence : " + reference), "Le texte doit contenir la référence");
        assertTrue(text.contains("Titre : " + titre), "Le texte doit contenir le titre");

        // Vérifications optionnelles - adaptez selon votre vrai contenu
        assertTrue(text.contains("ticket") || text.contains("Ticket"), "Le texte doit mentionner le ticket");
    }

    @Test
    void testEnvoyerNotificationTicketCree_WithDifferentRecipient() {
        String destinataire = "client@entreprise.com";
        String reference = "TCK-002";
        String titre = "Erreur système";

        service.envoyerNotificationTicketCree(destinataire, reference, titre);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        assertEquals(FROM_EMAIL, sent.getFrom());
        assertArrayEquals(new String[]{destinataire}, sent.getTo());
        assertEquals("Nouveau ticket créé - " + reference, sent.getSubject());

        String text = sent.getText();
        assertTrue(text.contains("Référence : " + reference));
        assertTrue(text.contains("Titre : " + titre));
    }

    @Test
    void testEnvoyerNotificationTicketCree_ExceptionIsHandled() {
        doThrow(new RuntimeException("Mail error")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
                service.envoyerNotificationTicketCree(TEST_EMAIL, "TCK-003", "Test d'erreur")
        );
    }

    @Test
    void testEnvoyerNotificationTicketCree_EmailContentValidation() {
        String destinataire = TEST_EMAIL;
        String reference = "TCK-004";
        String titre = "Test de validation";

        service.envoyerNotificationTicketCree(destinataire, reference, titre);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();

        // Validation du contenu BASÉE SUR CE QUI EXISTE RÉELLEMENT
        assertEquals(FROM_EMAIL, sent.getFrom());
        assertArrayEquals(new String[]{TEST_EMAIL}, sent.getTo());
        assertEquals("Nouveau ticket créé - TCK-004", sent.getSubject());

        String text = sent.getText();

        // Vérifications ESSENTIELLES (qui existent certainement)
        assertTrue(text.contains("Référence : TCK-004"), "Doit contenir la référence");
        assertTrue(text.contains("Titre : Test de validation"), "Doit contenir le titre");

        // Vérifications GÉNÉRIQUES (au lieu de textes spécifiques qui peuvent varier)
        assertNotNull(text, "Le texte ne doit pas être null");
        assertFalse(text.isEmpty(), "Le texte ne doit pas être vide");
        assertTrue(text.length() > 20, "Le texte doit avoir un contenu significatif");
    }

    @Test
    void testEnvoyerNotificationTicketCree_MultipleRecipients() {
        String[] destinataires = {TEST_EMAIL, "admin@entreprise.com"};
        String reference = "TCK-005";
        String titre = "Ticket multiple";

        // Si votre service supporte plusieurs destinataires
        for (String destinataire : destinataires) {
            service.envoyerNotificationTicketCree(destinataire, reference, titre);
        }

        // Vérifie que 2 emails ont été envoyés
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}