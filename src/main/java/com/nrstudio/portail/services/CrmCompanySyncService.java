// package com.nrstudio.portail.services;

// import com.nrstudio.portail.config.SchedulingConfig;
// import com.nrstudio.portail.depots.CompanyRepository;
// import com.nrstudio.portail.domaine.Company;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;

// @Service
// public class CrmCompanySyncService {

//   private final JdbcTemplate crmJdbc;
//   private final CompanyRepository companies;
//   private final SchedulingConfig schedulingConfig;

//   public CrmCompanySyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
//                                CompanyRepository companies,
//                                SchedulingConfig schedulingConfig) {
//     this.crmJdbc = crmJdbc;
//     this.companies = companies;
//     this.schedulingConfig = schedulingConfig;
//   }

//   @Scheduled(cron = "${scheduling.crm-company-sync-cron:0 * * * * *}")
//   @Transactional
//   public void synchroniserCompanies() {
//     final String sql =
//       "SELECT Comp_CompanyId, Comp_Name, Comp_Type, " +
//       "       ISNULL(Comp_Deleted,0) AS Comp_Deleted " +
//       "FROM dbo.Company ";
      

//     List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

//     for (Map<String,Object> r : rows) {
//       Integer companyId = toInt(r.get("Comp_CompanyId"));
//       if (companyId == null) continue;
//       if (toInt(r.get("Comp_Deleted")) == 1) continue;

//       String idExterneCrm = String.valueOf(companyId);
//       Company companyExistante = companies.findByIdExterneCrm(idExterneCrm).orElse(null);

//       String nom = Objects.toString(r.get("Comp_Name"), "Société " + companyId);
//       System.out.println("Company CRM " + companyId + " : " + nom);
      

//       if (companyExistante != null) {
//         companyExistante.setNom(nom);
        
        
//         companyExistante.setDateMiseAJour(LocalDateTime.now());
//         companies.save(companyExistante);
//       } else {
//         Company nouvelleCompany = new Company();
//         nouvelleCompany.setIdExterneCrm(idExterneCrm);
//         nouvelleCompany.setCodeCompany("COMP-" + companyId);
//         nouvelleCompany.setNom(nom);
        
        
//         nouvelleCompany.setActif(true);
//         nouvelleCompany.setDateCreation(LocalDateTime.now());
//         nouvelleCompany.setDateMiseAJour(LocalDateTime.now());
//         companies.save(nouvelleCompany);
//       }
//     }
//   }

//   private Integer toInt(Object o) {
//     if (o == null) return null;
//     if (o instanceof Number) return ((Number)o).intValue();
//     try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
//   }
// }
