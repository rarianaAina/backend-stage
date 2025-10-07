package com.nrstudio.portail.controleurs.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nrstudio.portail.services.WhatsAppNotificationService;

@RestController
@RequestMapping("/api/test-whatsapp")
public class TestWhatsappController {
    
    @Autowired
    private WhatsAppNotificationService whatsappService;

    @GetMapping("/test")
    public String test() {
        return "✅ Contrôleur WhatsApp fonctionnel! " + java.time.LocalDateTime.now();
    }


    @PostMapping("/ticket-cree")
    public String testWhatsappTicketCree() {
        String numeroDestinataire = "+261340626129"; // Remplacez par votre numéro de test
        String reference = "TICKET-2024-001";
        String titre = "Problème de connexion internet";
        
        try {
            whatsappService.envoyerNotificationTicketCree(numeroDestinataire, reference, titre);
            return "Notification de ticket créé envoyée avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors de l'envoi : " + e.getMessage();
        }
    }

    @PostMapping("/changement-statut")
    public String testWhatsappChangementStatut() {
        String numeroDestinataire = "+33612345678"; // Remplacez par votre numéro de test
        String reference = "TICKET-2024-001";
        String nouveauStatut = "En cours de traitement";
        
        try {
            whatsappService.envoyerNotificationChangementStatut(numeroDestinataire, reference, nouveauStatut);
            return "Notification de changement de statut envoyée avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors de l'envoi : " + e.getMessage();
        }
    }

    @PostMapping("/intervention-creee")
    public String testWhatsappInterventionCreee() {
        String numeroDestinataire = "+33612345678"; // Remplacez par votre numéro de test
        String reference = "TICKET-2024-001";
        String dateIntervention = "15/12/2024 à 14h30";
        
        try {
            whatsappService.envoyerNotificationInterventionCreee(numeroDestinataire, reference, dateIntervention);
            return "Notification d'intervention créée envoyée avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors de l'envoi : " + e.getMessage();
        }
    }

    @PostMapping("/date-validee")
    public String testWhatsappDateValidee() {
        String numeroDestinataire = "+33612345678"; // Remplacez par votre numéro de test
        String reference = "TICKET-2024-001";
        String dateIntervention = "15/12/2024 à 14h30";
        
        try {
            whatsappService.envoyerNotificationDateValidee(numeroDestinataire, reference, dateIntervention);
            return "Notification de date validée envoyée avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors de l'envoi : " + e.getMessage();
        }
    }

    @PostMapping("/message-personnalise")
    public String testWhatsappMessagePersonnalise() {
        String numeroDestinataire = "+33612345678"; // Remplacez par votre numéro de test
        String message = "Ceci est un test de message WhatsApp depuis l'API de test. Date: " + java.time.LocalDateTime.now();
        
        try {
            whatsappService.envoyerNotificationWhatsApp(numeroDestinataire, message);
            return "Message personnalisé envoyé avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors de l'envoi : " + e.getMessage();
        }
    }

    // Méthode supplémentaire pour tester avec un numéro différent
    @PostMapping("/test-complet")
    public String testCompletWhatsapp() {
        String numeroDestinataire = "+33612345678"; // Remplacez par votre numéro de test
        
        try {
            // Test 1: Création de ticket
            whatsappService.envoyerNotificationTicketCree(numeroDestinataire, "TICKET-2024-002", "Test complet - Problème technique");
            
            // Test 2: Changement de statut
            whatsappService.envoyerNotificationChangementStatut(numeroDestinataire, "TICKET-2024-002", "Assigné");
            
            // Test 3: Intervention créée
            whatsappService.envoyerNotificationInterventionCreee(numeroDestinataire, "TICKET-2024-002", "20/12/2024 à 10h00");
            
            return "Tests complets WhatsApp envoyés avec succès à " + numeroDestinataire;
        } catch (Exception e) {
            return "Erreur lors des tests : " + e.getMessage();
        }
    }
}