package com.nrstudio.portail.controleurs.test;

import com.nrstudio.portail.services.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-email")
public class TestEmailController {

    @Autowired
    private EmailNotificationService emailService;

    @GetMapping("/test")
    public String test() {
        return "✅ Contrôleur WhatsApp fonctionnel! " + java.time.LocalDateTime.now();
    }
    @PostMapping("/ticket-cree")
    public String testEmailTicketCree() {
        emailService.envoyerNotificationTicketCree(
                "rarianamiadana@gmail.com",
                "TCK-TEST-" + System.currentTimeMillis(),
                "Test de création de ticket"
        );
        return "Email envoyé ! Vérifiez votre boîte Gmail.";
    }

    @PostMapping("/changement-statut")
    public String testEmailChangementStatut() {
        emailService.envoyerNotificationChangementStatut(
                "rarianamiadana@gmail.com",
                "TCK-STATUT-" + System.currentTimeMillis(),
                "En attente",
                "En cours"
        );
        return "Email de changement de statut envoyé !";
    }

    @PostMapping("/intervention-cree")
    public String testEmailInterventionCree() {
        emailService.envoyerNotificationInterventionCreee(
                "rarianamiadana@gmail.com",
                "TCK-INTERV-" + System.currentTimeMillis(),
                "15/10/2024 à 14:00"
        );
        return "Email d'intervention créée envoyé !";
    }

    @PostMapping("/date-validee")
    public String testEmailDateValidee() {
        emailService.envoyerNotificationDateValidee(
                "rarianamiadana@gmail.com",
                "TCK-DATE-" + System.currentTimeMillis(),
                "20/10/2024 à 10:00"
        );
        return "Email de date validée envoyé !";
    }

    @PostMapping("/intervention-cloturee")
    public String testEmailInterventionCloturee() {
        emailService.envoyerNotificationInterventionCloturee(
                "rarianamiadana@gmail.com",
                "TCK-CLOT-" + System.currentTimeMillis()
        );
        return "Email d'intervention clôturée envoyé !";
    }

    @GetMapping("/test-simple")
    public String testSimple() {
        return "Le contrôleur de test email est actif ! Utilisez POST pour envoyer des emails.";
    }
}