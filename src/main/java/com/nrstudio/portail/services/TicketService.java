package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.dto.TicketCreationRequete;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TicketService {

  private final TicketRepository tickets;
  private final JdbcTemplate crmJdbc;

  public TicketService(TicketRepository tickets,
                       @Qualifier("crmJdbc") JdbcTemplate crmJdbc) {
    this.tickets = tickets;
    this.crmJdbc = crmJdbc;
  }

  @Transactional
  public Ticket creerEtSynchroniser(TicketCreationRequete r) {

    if (!r.isPolitiqueAcceptee()) {
      throw new IllegalArgumentException("Les politiques doivent être acceptées.");
    }

    // 1) Créer dans PORTAIL_CLIENT
    Ticket t = new Ticket();
    t.setClientId(r.getClientId());
    t.setProduitId(r.getProduitId());
    t.setTypeTicketId(r.getTypeTicketId());
    t.setPrioriteTicketId(r.getPrioriteTicketId());
    t.setStatutTicketId(r.getStatutTicketId());
    t.setTitre(r.getTitre());
    t.setDescription(r.getDescription());
    t.setRaison(r.getRaison());
    t.setPolitiqueAcceptee(true);
    t.setCreeParUtilisateurId(r.getCreeParUtilisateurId());
    t.setAffecteAUtilisateurId(r.getAffecteAUtilisateurId());
    t.setDateCreation(LocalDateTime.now());
    t.setDateMiseAJour(LocalDateTime.now());

    // référence lisible côté portail
    t.setReference("TCK-" + System.currentTimeMillis());
    t = tickets.save(t);

    // 2) Créer le Case dans le CRM (dbo.Cases)
    String caseDescription = truncate(t.getTitre(), 40);
    String caseProblemNote = t.getDescription() != null ? t.getDescription() : "";
    String casePriority = mapPrioriteIdToCrmString(t.getPrioriteTicketId()); // TODO: adapter
    String caseStatus   = mapStatutIdToCrmString(t.getStatutTicketId());     // TODO: adapter
    String caseProduct  = mapProduitIdToCrmString(t.getProduitId());         // TODO: adapter

    // Company côté CRM si tu as un mapping clientId -> CompanyId, mets-le ici
    Integer crmCompanyId = mapClientIdToCrmCompanyId(t.getClientId());

    Integer caseId = crmJdbc.queryForObject(
      "INSERT INTO dbo.Cases " +
      " (Case_PrimaryCompanyId, Case_Description, Case_ProblemNote, Case_Priority, Case_Status, " +
      "  Case_Product, Case_Opened, Case_Deleted, Case_Source, Case_CustomerRef) " +
      " VALUES (?,?,?,?,?,?, GETDATE(), 0, 'Portail', ?) ; " +
      " SELECT CAST(SCOPE_IDENTITY() AS INT);",
      Integer.class,
      crmCompanyId, caseDescription, caseProblemNote, casePriority, caseStatus,
      caseProduct, t.getReference()
    );

    if (caseId != null) {
      t.setIdExterneCrm(caseId);
      t.setDateMiseAJour(LocalDateTime.now());
      t = tickets.save(t);
    }

    return t;
  }

  private String truncate(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  // ====== MAPPINGS À ADAPTER à tes tables référentielles ======
  private String mapPrioriteIdToCrmString(Integer prioriteId) {
    if (prioriteId == null) return null;
    // exemple : 1=Low, 2=Normal, 3=High, 4=Urgent
    switch (prioriteId) {
      case 4: return "Urgent";
      case 3: return "High";
      case 2: return "Normal";
      default: return "Low";
    }
  }

  private String mapStatutIdToCrmString(Integer statutId) {
    if (statutId == null) return "Open";
    // exemple : 1=Open, 2=In Progress, 3=Pending, 4=Closed
    switch (statutId) {
      case 4: return "Closed";
      case 3: return "Pending";
      case 2: return "In Progress";
      default: return "Open";
    }
  }

  private String mapProduitIdToCrmString(Integer produitId) {
    // si tu as un catalogue CRM en texte, retourne le libellé attendu
    return null; // facultatif
  }

  private Integer mapClientIdToCrmCompanyId(Integer clientId) {
    // si tu as une table de correspondance Portail<->CRM, mappe ici
    return null; // facultatif
  }
}
