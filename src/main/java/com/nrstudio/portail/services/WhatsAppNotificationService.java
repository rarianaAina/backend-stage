package com.nrstudio.portail.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class WhatsAppNotificationService {

  @Value("${twilio.account.sid}")
  private String accountSid;

  @Value("${twilio.auth.token}")
  private String authToken;

  @Value("${twilio.whatsapp.from}")
  private String fromWhatsApp;

  @PostConstruct
  public void init() {
    if (accountSid != null && !accountSid.startsWith("VOTRE_")) {
      Twilio.init(accountSid, authToken);
    }
  }

  public void envoyerNotificationWhatsApp(String numeroDestinataire, String contenu) {
    try {
      if (accountSid == null || accountSid.startsWith("VOTRE_")) {
        System.out.println("WhatsApp non configuré. Message qui aurait été envoyé : " + contenu);
        return;
      }

      if (!numeroDestinataire.startsWith("whatsapp:")) {
        numeroDestinataire = "whatsapp:" + numeroDestinataire;
      }

      Message message = Message.creator(
        new PhoneNumber(numeroDestinataire),
        new PhoneNumber(fromWhatsApp),
        contenu
      ).create();

      System.out.println("Message WhatsApp envoyé : " + message.getSid());
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi du message WhatsApp : " + e.getMessage());
    }
  }

  public void envoyerNotificationTicketCree(String numeroDestinataire, String reference, String titre) {
    String message = String.format(
      "Nouveau ticket créé\n\n" +
      "Référence : %s\n" +
      "Titre : %s\n\n" +
      "Consultez votre portail client pour plus de détails.",
      reference, titre
    );
    envoyerNotificationWhatsApp(numeroDestinataire, message);
  }

  public void envoyerNotificationChangementStatut(String numeroDestinataire, String reference, String nouveauStatut) {
    String message = String.format(
      "Changement de statut\n\n" +
      "Ticket : %s\n" +
      "Nouveau statut : %s\n\n" +
      "Consultez votre portail client pour plus de détails.",
      reference, nouveauStatut
    );
    envoyerNotificationWhatsApp(numeroDestinataire, message);
  }

  public void envoyerNotificationInterventionCreee(String numeroDestinataire, String reference, String dateIntervention) {
    String message = String.format(
      "Intervention planifiée\n\n" +
      "Ticket : %s\n" +
      "Date proposée : %s\n\n" +
      "Veuillez valider ou proposer une autre date sur votre portail.",
      reference, dateIntervention
    );
    envoyerNotificationWhatsApp(numeroDestinataire, message);
  }

  public void envoyerNotificationDateValidee(String numeroDestinataire, String reference, String dateIntervention) {
    String message = String.format(
      "Date d'intervention validée\n\n" +
      "Ticket : %s\n" +
      "Date confirmée : %s\n\n" +
      "Nous vous contacterons à cette date.",
      reference, dateIntervention
    );
    envoyerNotificationWhatsApp(numeroDestinataire, message);
  }
}
