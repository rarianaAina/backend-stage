package com.nrstudio.portail.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public EmailNotificationService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  public void envoyerNotificationTicketCree(String destinataire, String reference, String titre) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(destinataire);
      message.setSubject("Nouveau ticket créé - " + reference);
      message.setText(
        "Bonjour,\n\n" +
        "Un nouveau ticket a été créé :\n\n" +
        "Référence : " + reference + "\n" +
        "Titre : " + titre + "\n\n" +
        "Vous pouvez consulter les détails sur votre portail client.\n\n" +
        "Cordialement,\n" +
        "L'équipe support"
      );
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    }
  }

  public void envoyerNotificationChangementStatut(String destinataire, String reference, String ancienStatut, String nouveauStatut) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(destinataire);
      message.setSubject("Changement de statut - " + reference);
      message.setText(
        "Bonjour,\n\n" +
        "Le statut de votre ticket " + reference + " a été modifié :\n\n" +
        "Ancien statut : " + ancienStatut + "\n" +
        "Nouveau statut : " + nouveauStatut + "\n\n" +
        "Vous pouvez consulter les détails sur votre portail client.\n\n" +
        "Cordialement,\n" +
        "L'équipe support"
      );
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    }
  }

  public void envoyerNotificationInterventionCreee(String destinataire, String reference, String dateIntervention) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(destinataire);
      message.setSubject("Intervention planifiée - " + reference);
      message.setText(
        "Bonjour,\n\n" +
        "Une intervention a été planifiée pour votre ticket " + reference + " :\n\n" +
        "Date proposée : " + dateIntervention + "\n\n" +
        "Veuillez valider ou proposer une autre date sur votre portail client.\n\n" +
        "Cordialement,\n" +
        "L'équipe support"
      );
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    }
  }

  public void envoyerNotificationDateValidee(String destinataire, String reference, String dateIntervention) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(destinataire);
      message.setSubject("Date d'intervention validée - " + reference);
      message.setText(
        "Bonjour,\n\n" +
        "La date d'intervention pour le ticket " + reference + " a été validée :\n\n" +
        "Date confirmée : " + dateIntervention + "\n\n" +
        "Nous vous contacterons à cette date.\n\n" +
        "Cordialement,\n" +
        "L'équipe support"
      );
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    }
  }

  public void envoyerNotificationInterventionCloturee(String destinataire, String reference) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(destinataire);
      message.setSubject("Intervention clôturée - " + reference);
      message.setText(
        "Bonjour,\n\n" +
        "L'intervention pour le ticket " + reference + " a été clôturée.\n\n" +
        "Une fiche d'intervention vous a été envoyée pour validation.\n\n" +
        "Cordialement,\n" +
        "L'équipe support"
      );
      mailSender.send(message);
    } catch (Exception e) {
      System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
    }
  }
}
