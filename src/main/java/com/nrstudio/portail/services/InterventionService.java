package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.InterventionRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.domaine.Intervention;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class InterventionService {

  private final InterventionRepository interventions;
  private final TicketRepository tickets;
  private final UtilisateurRepository utilisateurs;
  private final JdbcTemplate crmJdbc;
  private final EmailNotificationService emailService;
  private final WhatsAppNotificationService whatsAppService;

  public InterventionService(InterventionRepository interventions,
                             TicketRepository tickets,
                             UtilisateurRepository utilisateurs,
                             @Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                             EmailNotificationService emailService,
                             WhatsAppNotificationService whatsAppService) {
    this.interventions = interventions;
    this.tickets = tickets;
    this.utilisateurs = utilisateurs;
    this.crmJdbc = crmJdbc;
    this.emailService = emailService;
    this.whatsAppService = whatsAppService;
  }

  @Transactional
  public Intervention creerIntervention(Integer ticketId, String raison, LocalDateTime dateIntervention,
                                        String typeIntervention, Integer consultantId) {
    Ticket ticket = tickets.findById(ticketId)
      .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable"));

    Intervention intervention = new Intervention();
    intervention.setTicketId(ticketId);
    intervention.setReference("INT-" + System.currentTimeMillis());
    intervention.setRaison(raison);
    intervention.setDateIntervention(dateIntervention);
    intervention.setTypeIntervention(typeIntervention);
    intervention.setStatutInterventionId(1);
    intervention.setCreeParUtilisateurId(consultantId);
    intervention.setDateCreation(LocalDateTime.now());
    intervention.setDateMiseAJour(LocalDateTime.now());
    intervention.setValideeParClient(false);

    intervention = interventions.save(intervention);

    synchroniserAvecCrm(intervention, ticket);
    envoyerNotificationsCreation(intervention, ticket);

    return intervention;
  }

  @Transactional
  public Intervention validerDate(Integer interventionId, Integer utilisateurId, boolean estClient) {
    Intervention intervention = interventions.findById(interventionId)
      .orElseThrow(() -> new IllegalArgumentException("Intervention introuvable"));

    Ticket ticket = tickets.findById(intervention.getTicketId()).orElse(null);

    if (estClient) {
      intervention.setValideeParClient(true);
      intervention.setStatutInterventionId(2);
    } else {
      if (intervention.getDateProposeeClient() != null) {
        intervention.setDateIntervention(intervention.getDateProposeeClient());
        intervention.setDateProposeeClient(null);
        intervention.setValideeParClient(true);
        intervention.setStatutInterventionId(2);
      }
    }

    intervention.setDateMiseAJour(LocalDateTime.now());
    intervention = interventions.save(intervention);

    if (intervention.getIdExterneCrm() != null && ticket != null) {
      synchroniserAvecCrm(intervention, ticket);
    }

    envoyerNotificationsValidation(intervention, ticket);

    return intervention;
  }

  @Transactional
  public Intervention proposerNouvelleDate(Integer interventionId, LocalDateTime nouvelleDate,
                                           Integer utilisateurId, boolean estClient) {
    Intervention intervention = interventions.findById(interventionId)
      .orElseThrow(() -> new IllegalArgumentException("Intervention introuvable"));

    Ticket ticket = tickets.findById(intervention.getTicketId()).orElse(null);

    if (estClient) {
      intervention.setDateProposeeClient(nouvelleDate);
      intervention.setValideeParClient(false);
      intervention.setStatutInterventionId(1);
    } else {
      intervention.setDateIntervention(nouvelleDate);
      intervention.setDateProposeeClient(null);
    }

    intervention.setDateMiseAJour(LocalDateTime.now());
    intervention = interventions.save(intervention);

    envoyerNotificationsNouvelleDate(intervention, ticket, estClient);

    return intervention;
  }

  @Transactional
  public Intervention cloturerIntervention(Integer interventionId, String ficheIntervention,
                                           Integer consultantId) {
    Intervention intervention = interventions.findById(interventionId)
      .orElseThrow(() -> new IllegalArgumentException("Intervention introuvable"));

    Ticket ticket = tickets.findById(intervention.getTicketId()).orElse(null);

    intervention.setStatutInterventionId(4);
    intervention.setDateCloture(LocalDateTime.now());
    intervention.setClotureParUtilisateurId(consultantId);
    intervention.setFicheIntervention(ficheIntervention);
    intervention.setDateMiseAJour(LocalDateTime.now());

    intervention = interventions.save(intervention);

    if (intervention.getIdExterneCrm() != null && ticket != null) {
      synchroniserAvecCrm(intervention, ticket);
    }

    envoyerNotificationsCloture(intervention, ticket);

    return intervention;
  }

  @Transactional
  public List<Intervention> listerInterventionsTicket(Integer ticketId) {
    return interventions.findByTicketId(ticketId);
  }

  @Transactional
  public List<Intervention> listerInterventionsConsultant(Integer consultantId) {
    return interventions.findByCreeParUtilisateurId(consultantId);
  }

  private void synchroniserAvecCrm(Intervention intervention, Ticket ticket) {
    try {
      if (intervention.getIdExterneCrm() == null) {
        Integer appointmentId = crmJdbc.queryForObject(
          "INSERT INTO dbo.Appointments " +
          " (Appt_CompanyId, Appt_PersonId, Appt_OpportunityId, Appt_Subject, Appt_StartDateTime, " +
          "  Appt_Duration, Appt_Status, Appt_Type, Appt_Notes, Appt_CreatedDate, Appt_Deleted) " +
          " VALUES (?,?,?, ?,?, 60, 'Scheduled', ?,?, GETDATE(), 0); " +
          " SELECT CAST(SCOPE_IDENTITY() AS INT);",
          Integer.class,
          ticket.getCompanyId(), null, ticket.getIdExterneCrm(),
          "Intervention - " + ticket.getReference(),
          intervention.getDateIntervention(),
          intervention.getTypeIntervention(),
          intervention.getRaison()
        );

        if (appointmentId != null) {
          intervention.setIdExterneCrm(appointmentId);
          interventions.save(intervention);
        }
      } else {
        crmJdbc.update(
          "UPDATE dbo.Appointments SET Appt_StartDateTime = ?, Appt_Status = ?, Appt_Notes = ? " +
          "WHERE Appt_AppointmentId = ?",
          intervention.getDateIntervention(),
          intervention.getStatutInterventionId() == 4 ? "Completed" : "Scheduled",
          intervention.getRaison(),
          intervention.getIdExterneCrm()
        );
      }
    } catch (Exception e) {
      System.err.println("Erreur synchronisation CRM : " + e.getMessage());
    }
  }

  private void envoyerNotificationsCreation(Intervention intervention, Ticket ticket) {
    try {
      if (ticket == null) return;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      String dateFormatee = intervention.getDateIntervention().format(formatter);

      Utilisateur client = utilisateurs.findById(ticket.getCreeParUtilisateurId()).orElse(null);
      if (client != null && client.getEmail() != null) {
        emailService.envoyerNotificationInterventionCreee(
          client.getEmail(),
          ticket.getReference(),
          dateFormatee
        );

        if (client.getTelephone() != null) {
          whatsAppService.envoyerNotificationInterventionCreee(
            client.getTelephone(),
            ticket.getReference(),
            dateFormatee
          );
        }
      }
    } catch (Exception e) {
      System.err.println("Erreur envoi notifications : " + e.getMessage());
    }
  }

  private void envoyerNotificationsValidation(Intervention intervention, Ticket ticket) {
    try {
      if (ticket == null) return;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      String dateFormatee = intervention.getDateIntervention().format(formatter);

      Utilisateur consultant = utilisateurs.findById(intervention.getCreeParUtilisateurId()).orElse(null);
      if (consultant != null && consultant.getEmail() != null) {
        emailService.envoyerNotificationDateValidee(
          consultant.getEmail(),
          ticket.getReference(),
          dateFormatee
        );

        if (consultant.getTelephone() != null) {
          whatsAppService.envoyerNotificationDateValidee(
            consultant.getTelephone(),
            ticket.getReference(),
            dateFormatee
          );
        }
      }

      Utilisateur client = utilisateurs.findById(ticket.getCreeParUtilisateurId()).orElse(null);
      if (client != null && client.getEmail() != null) {
        emailService.envoyerNotificationDateValidee(
          client.getEmail(),
          ticket.getReference(),
          dateFormatee
        );
      }
    } catch (Exception e) {
      System.err.println("Erreur envoi notifications : " + e.getMessage());
    }
  }

  private void envoyerNotificationsNouvelleDate(Intervention intervention, Ticket ticket, boolean parClient) {
    try {
      if (ticket == null) return;

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      LocalDateTime dateProposee = parClient ? intervention.getDateProposeeClient() : intervention.getDateIntervention();
      String dateFormatee = dateProposee.format(formatter);

      if (parClient) {
        Utilisateur consultant = utilisateurs.findById(intervention.getCreeParUtilisateurId()).orElse(null);
        if (consultant != null && consultant.getEmail() != null) {
          emailService.envoyerNotificationInterventionCreee(
            consultant.getEmail(),
            ticket.getReference(),
            dateFormatee
          );
        }
      } else {
        Utilisateur client = utilisateurs.findById(ticket.getCreeParUtilisateurId()).orElse(null);
        if (client != null && client.getEmail() != null) {
          emailService.envoyerNotificationInterventionCreee(
            client.getEmail(),
            ticket.getReference(),
            dateFormatee
          );

          if (client.getTelephone() != null) {
            whatsAppService.envoyerNotificationInterventionCreee(
              client.getTelephone(),
              ticket.getReference(),
              dateFormatee
            );
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Erreur envoi notifications : " + e.getMessage());
    }
  }

  private void envoyerNotificationsCloture(Intervention intervention, Ticket ticket) {
    try {
      if (ticket == null) return;

      Utilisateur client = utilisateurs.findById(ticket.getCreeParUtilisateurId()).orElse(null);
      if (client != null && client.getEmail() != null) {
        emailService.envoyerNotificationInterventionCloturee(
          client.getEmail(),
          ticket.getReference()
        );
      }
    } catch (Exception e) {
      System.err.println("Erreur envoi notifications : " + e.getMessage());
    }
  }
}
