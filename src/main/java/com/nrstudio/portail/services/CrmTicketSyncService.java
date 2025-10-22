// package com.nrstudio.portail.services;

// import com.nrstudio.portail.depots.CompanyRepository;
// import com.nrstudio.portail.depots.TicketRepository;
// import com.nrstudio.portail.depots.UtilisateurRepository;
// import com.nrstudio.portail.domaine.Company;
// import com.nrstudio.portail.domaine.Ticket;
// import com.nrstudio.portail.domaine.Utilisateur;

// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.sql.Timestamp;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;

// @Service
// public class CrmTicketSyncService {

//   private final JdbcTemplate crmJdbc;
//   private final TicketRepository tickets;
//   private final CompanyRepository companies;
//   private final UtilisateurRepository users;

//   public CrmTicketSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
//                               TicketRepository tickets,
//                               CompanyRepository companies,
//                               UtilisateurRepository users) {
//     this.crmJdbc = crmJdbc;
//     this.tickets = tickets;
//     this.companies = companies;
//     this.users = users;
//   }

//   @Scheduled(cron = "0 * * * * *")
//   @Transactional
//   public void importerDepuisCrm() {
//     final String sql =
//       "SELECT Case_CaseId, Case_Description, Case_PrimaryPersonId, Case_ProblemNote, Case_Priority, Case_Status, Case_CreatedBy, " +
//       "       Case_PARCId, Case_PrimaryCompanyId, Case_CreatedDate, Case_UpdatedDate, Case_Opened, Case_Closed, Case_CustomerRef, " +
//       "       ISNULL(Case_Deleted,0) AS Case_Deleted " +
//       "FROM dbo.Cases WHERE ISNULL(Case_Deleted,0) = 0";

//     List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
    
//     for (Map<String,Object> r : rows) {
//       Integer caseId = toInt(r.get("Case_CaseId"));
//       if (caseId == null) continue;
//       if (toInt(r.get("Case_Deleted")) == 1) continue; // on ignore supprimés

//       if (tickets.findByIdExterneCrm(caseId).isPresent()) continue; // déjà importé

//       String titre       = Objects.toString(r.get("Case_Description"), null);
//       String description = Objects.toString(r.get("Case_ProblemNote"), null);
//       String prioriteStr = Objects.toString(r.get("Case_Priority"), null);
//       String statutStr   = Objects.toString(r.get("Case_Status"), null);
//       Integer produitId  = toInt(r.get("Case_PARCId"));
//       Integer compId     = toInt(r.get("Case_PrimaryCompanyId"));
//       Integer personId    = toInt(r.get("Case_PrimaryPersonId"));
//       String ref         = Objects.toString(r.get("Case_CustomerRef"), null);
//       Integer creeParUtilisateurId = toInt(r.get("Case_CreatedBy"));
//       System.out.println("Utilisateur: " + creeParUtilisateurId);


//             // Recherche de l'utilisateur portail par id_externe_crm
//       Integer utilisateurIdPortail = null;
//       if (creeParUtilisateurId != null) {
//         Utilisateur user = users.findByIdExterneCrm(String.valueOf(creeParUtilisateurId)).orElse(null);
//         if (user != null) {
//           utilisateurIdPortail = user.getId();
//         }
//       }

      
//       LocalDateTime opened = toLdt(r.get("Case_Opened"));
//       LocalDateTime closed = toLdt(r.get("Case_Closed"));
//       LocalDateTime created = toLdt(r.get("Case_CreatedDate"));
//       LocalDateTime updated = toLdt(r.get("Case_UpdatedDate"));  
//       Integer companyIdPortail = mapCompanyIdToCompanyId(compId);
//       if (companyIdPortail == null) {
//         continue;
//       }

//       Ticket t = new Ticket();
//       t.setReference(ref != null && !ref.isEmpty() ? ref : "CRM-" + caseId);
//       t.setCompanyId(companyIdPortail);
//       t.setProduitId(mapProduitIdToId(produitId));
//       t.setTypeTicketId(mapTypeByHeuristique(titre, description));

//       t.setPrioriteTicketId(mapPrioriteCrmStringToId(prioriteStr));
//       t.setStatutTicketId(mapStatutCrmStringToId(statutStr));

//       t.setTitre(titre != null ? titre : "Ticket CRM " + caseId);
//       t.setDescription(description);
//       t.setRaison(null);
//       t.setPolitiqueAcceptee(true);
//       t.setClientId(personId != null ? personId : null);
//       t.setCreeParUtilisateurId(creeParUtilisateurId);
//       //t.setCreeParUtilisateurId(creeParUtilisateurId);
//       t.setAffecteAUtilisateurId(null);

//       t.setDateCreation(created != null ? created : LocalDateTime.now());
//       t.setDateMiseAJour(updated != null ? updated : LocalDateTime.now());
//       t.setDateCloture(closed);
//       t.setClotureParUtilisateurId(null);

//       t.setIdExterneCrm(caseId);

//       tickets.save(t);
//     }
//   }

//   private Integer toInt(Object o) {
//     if (o == null) return null;
//     if (o instanceof Number) return ((Number)o).intValue();
//     try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
//   }

//   private LocalDateTime toLdt(Object o) {
//     if (o == null) return null;
//     if (o instanceof Timestamp) return ((Timestamp)o).toLocalDateTime();
//     if (o instanceof java.util.Date) return new Timestamp(((java.util.Date)o).getTime()).toLocalDateTime();
//     return null;
//     }

//   // ====== MAPPINGS (à brancher sur tes tables référentielles) ======
//   // private Integer mapPrioriteCrmStringToId(String s) {
//   //   if (s == null) return 2; // ex. Normal
//   //   switch (s) {
//   //     case "Urgent": return 4;
//   //     case "High":   return 3;
//   //     case "Normal": return 2;
//   //     default:       return 1; // Low
//   //   }
//   // }

//   private Integer mapPrioriteCrmStringToId(String s) {
//     if (s == null) return 2; // ex. Normal
//     if (s.equals("Urgent")) return 3;
//     if (s.equals("High"))   return 2;
//     if (s.equals("Normal")) return 1;
//     return 0; // Low
// }


//   private Integer mapStatutCrmStringToId(String s) {
//     if (s == null) return 1; // Open
//     if (s.equals("Closed")) return 4;
//     if (s.equals("Pending")) return 3;
//     if (s.equals("In Progress")) return 2;
//     return 1;
//   }

//   private Integer mapProduitIdToId(Integer produitIdCrm) {
//     if (produitIdCrm == null) return null;
//     // TODO : lookup dans table produit par id_externe_crm
//     return null;
//   }

//   private Integer mapTypeByHeuristique(String titre, String desc) {
//     return 1;
//   }

//   private Integer mapCompanyIdToCompanyId(Integer companyId) {
//     if (companyId == null) return null;
//     try {
//       String idExterneCrm = String.valueOf(companyId);
//       Company company = companies.findByIdExterneCrm(idExterneCrm).orElse(null);
//       if (company != null) {
//         return company.getId();
//       }
//       return null;
//     } catch (Exception e) {
//       return null;
//     }
//   }
// }
