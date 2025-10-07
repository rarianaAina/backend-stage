package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.InterventionRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Intervention;
import com.nrstudio.portail.domaine.Ticket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmInterventionSyncService {

  private final JdbcTemplate crmJdbc;
  private final InterventionRepository interventions;
  private final TicketRepository tickets;

  public CrmInterventionSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                    InterventionRepository interventions,
                                    TicketRepository tickets) {
    this.crmJdbc = crmJdbc;
    this.interventions = interventions;
    this.tickets = tickets;
  }

  @Scheduled(cron = "0 */15 * * * *")
  @Transactional
  public void importerDepuisCrm() {
    final String sql =
      "SELECT Appt_AppointmentId, Appt_CompanyId, Appt_OpportunityId, Appt_Subject, " +
      "       Appt_StartDateTime, Appt_Duration, Appt_Status, Appt_Type, Appt_Notes, " +
      "       Appt_CreatedDate, ISNULL(Appt_Deleted,0) AS Appt_Deleted " +
      "FROM dbo.Appointments " +
      "WHERE Appt_OpportunityId IS NOT NULL";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer apptId = toInt(r.get("Appt_AppointmentId"));
      if (apptId == null) continue;
      if (toInt(r.get("Appt_Deleted")) == 1) continue;

      if (interventions.findByIdExterneCrm(apptId).isPresent()) continue;

      Integer crmCaseId = toInt(r.get("Appt_OpportunityId"));
      Ticket ticket = tickets.findByIdExterneCrm(crmCaseId).orElse(null);
      if (ticket == null) continue;

      String sujet = Objects.toString(r.get("Appt_Subject"), null);
      LocalDateTime dateDebut = toLdt(r.get("Appt_StartDateTime"));
      String statut = Objects.toString(r.get("Appt_Status"), null);
      String type = Objects.toString(r.get("Appt_Type"), null);
      String notes = Objects.toString(r.get("Appt_Notes"), null);
      LocalDateTime dateCrea = toLdt(r.get("Appt_CreatedDate"));

      Intervention intervention = new Intervention();
      intervention.setTicketId(ticket.getId());
      intervention.setReference("CRM-INT-" + apptId);
      intervention.setRaison(notes);
      intervention.setDateIntervention(dateDebut != null ? dateDebut : LocalDateTime.now());
      intervention.setTypeIntervention(type);
      intervention.setStatutInterventionId(mapStatutCrmToId(statut));
      intervention.setCreeParUtilisateurId(ticket.getAffecteAUtilisateurId() != null ? ticket.getAffecteAUtilisateurId() : 1);
      intervention.setDateCreation(dateCrea != null ? dateCrea : LocalDateTime.now());
      intervention.setDateMiseAJour(LocalDateTime.now());
      intervention.setValideeParClient(false);
      intervention.setIdExterneCrm(apptId);

      if ("Completed".equalsIgnoreCase(statut) || "Cancelled".equalsIgnoreCase(statut)) {
        intervention.setDateCloture(LocalDateTime.now());
      }

      interventions.save(intervention);
    }
  }

  @Scheduled(cron = "0 */20 * * * *")
  @Transactional
  public void exporterVersCrm() {
    List<Intervention> interventionsSansIdCrm = interventions.findAll().stream()
      .filter(i -> i.getIdExterneCrm() == null)
      .toList();

    for (Intervention intervention : interventionsSansIdCrm) {
      try {
        Ticket ticket = tickets.findById(intervention.getTicketId()).orElse(null);
        if (ticket == null || ticket.getIdExterneCrm() == null) continue;

        Integer appointmentId = crmJdbc.queryForObject(
          "INSERT INTO dbo.Appointments " +
          " (Appt_CompanyId, Appt_OpportunityId, Appt_Subject, Appt_StartDateTime, " +
          "  Appt_Duration, Appt_Status, Appt_Type, Appt_Notes, Appt_CreatedDate, Appt_Deleted) " +
          " VALUES (?,?, ?,?, 60, ?, ?,?, GETDATE(), 0); " +
          " SELECT CAST(SCOPE_IDENTITY() AS INT);",
          Integer.class,
          ticket.getClientId(),
          ticket.getIdExterneCrm(),
          "Intervention - " + ticket.getReference(),
          intervention.getDateIntervention(),
          mapStatutIdToCrmString(intervention.getStatutInterventionId()),
          intervention.getTypeIntervention(),
          intervention.getRaison(),
          intervention.getDateCreation()
        );

        if (appointmentId != null) {
          intervention.setIdExterneCrm(appointmentId);
          interventions.save(intervention);
        }
      } catch (Exception e) {
        System.err.println("Erreur export intervention vers CRM : " + e.getMessage());
      }
    }
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }

  private LocalDateTime toLdt(Object o) {
    if (o == null) return null;
    if (o instanceof Timestamp) return ((Timestamp)o).toLocalDateTime();
    if (o instanceof java.util.Date) return new Timestamp(((java.util.Date)o).getTime()).toLocalDateTime();
    return null;
  }

  private Integer mapStatutCrmToId(String statut) {
    if (statut == null) return 1;
    if ("Completed".equalsIgnoreCase(statut)) return 4;
    if ("Cancelled".equalsIgnoreCase(statut)) return 5;
    if ("Confirmed".equalsIgnoreCase(statut)) return 2;
    return 1;
  }

  private String mapStatutIdToCrmString(Integer statutId) {
    if (statutId == null) return "Scheduled";
    switch (statutId) {
      case 4: return "Completedd";
      case 5: return "Cancelled";
      case 2: return "Confirmed";
      default: return "Scheduled";
    }
  }
}
