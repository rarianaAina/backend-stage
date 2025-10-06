package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmTicketSyncService {

  private final JdbcTemplate crmJdbc;
  private final TicketRepository tickets;

  public CrmTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              TicketRepository tickets) {
    this.crmJdbc = crmJdbc;
    this.tickets = tickets;
  }

  @Scheduled(cron = "0 */30 * * * *")
  @Transactional
  public void importerDepuisCrm() {
    final String sql =
      "SELECT Case_CaseId, Case_Description, Case_ProblemNote, Case_Priority, Case_Status, " +
      "       Case_Product, Case_PrimaryCompanyId, Case_Opened, Case_Closed, Case_CustomerRef, " +
      "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
      "FROM dbo.Cases";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer caseId = toInt(r.get("Case_CaseId"));
      if (caseId == null) continue;
      if (toInt(r.get("Case_Deleted")) == 1) continue; // on ignore supprimés

      if (tickets.findByIdExterneCrm(caseId).isPresent()) continue; // déjà importé

      String titre       = Objects.toString(r.get("Case_Description"), null);
      String description = Objects.toString(r.get("Case_ProblemNote"), null);
      String prioriteStr = Objects.toString(r.get("Case_Priority"), null);
      String statutStr   = Objects.toString(r.get("Case_Status"), null);
      String produitStr  = Objects.toString(r.get("Case_Product"), null);
      Integer compId     = toInt(r.get("Case_PrimaryCompanyId"));
      String ref         = Objects.toString(r.get("Case_CustomerRef"), null);


      LocalDateTime opened = toLdt(r.get("Case_Opened"));
      LocalDateTime closed = toLdt(r.get("Case_Closed"));

      Ticket t = new Ticket();
      t.setReference(ref != null && !ref.isEmpty() ? ref : "CRM-" + caseId);
      t.setClientId(compId != null ? compId : 0); // à mapper si besoin
      t.setProduitId(mapProduitCrmStringToId(produitStr)); // TODO
      t.setTypeTicketId(mapTypeByHeuristique(titre, description)); // TODO

      t.setPrioriteTicketId(mapPrioriteCrmStringToId(prioriteStr)); // TODO
      t.setStatutTicketId(mapStatutCrmStringToId(statutStr));       // TODO

      t.setTitre(titre != null ? titre : "Ticket CRM " + caseId);
      t.setDescription(description);
      t.setRaison(null);
      t.setPolitiqueAcceptee(true); // import CRM → on considère ok

      // à défaut d’auteur, on met 0 (à mapper si tu as un user technique)
      t.setCreeParUtilisateurId(1);
      t.setAffecteAUtilisateurId(null);

      t.setDateCreation(opened != null ? opened : LocalDateTime.now());
      t.setDateMiseAJour(LocalDateTime.now());
      t.setDateCloture(closed);
      t.setClotureParUtilisateurId(null);

      t.setIdExterneCrm(caseId);

      tickets.save(t);
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

  // ====== MAPPINGS (à brancher sur tes tables référentielles) ======
  // private Integer mapPrioriteCrmStringToId(String s) {
  //   if (s == null) return 2; // ex. Normal
  //   switch (s) {
  //     case "Urgent": return 4;
  //     case "High":   return 3;
  //     case "Normal": return 2;
  //     default:       return 1; // Low
  //   }
  // }

  private Integer mapPrioriteCrmStringToId(String s) {
    if (s == null) return 2; // ex. Normal
    if (s.equals("Urgent")) return 4;
    if (s.equals("High"))   return 3;
    if (s.equals("Normal")) return 2;
    return 1; // Low
}


  private Integer mapStatutCrmStringToId(String s) {
    if (s == null) return 1; // Open
    if (s.equals("Closed")) return 4;
    if (s.equals("Pending")) return 3;
    if (s.equals("In Progress")) return 2;
    return 1;
  }

  private Integer mapProduitCrmStringToId(String s) {
    return null; // TODO : lookup via table produit
  }

  private Integer mapTypeByHeuristique(String titre, String desc) {
    return 1; // TODO : par défaut, ou applique des règles simples
  }
}
