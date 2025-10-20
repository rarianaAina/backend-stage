// package com.nrstudio.portail.services;

// import com.nrstudio.portail.depots.UtilisateurRepository;
// // import com.nrstudio.portail.config.SchedulingConfig;
// import com.nrstudio.portail.depots.CompanyRepository;
// import com.nrstudio.portail.domaine.Utilisateur;
// import com.nrstudio.portail.domaine.UtilisateurRole;

// //import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

// import com.nrstudio.portail.domaine.Company;
// import com.nrstudio.portail.domaine.Role;

// import org.mindrot.jbcrypt.BCrypt;
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
// public class CrmPersonSyncService {

//   private final JdbcTemplate crmJdbc;
//   private final UtilisateurRepository utilisateurs;
//   private final CompanyRepository companies;
//   private final UtilisateurRoleService utilisateurRoleService;
//   //private final SchedulingConfig schedulingConfig;

//   public CrmPersonSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
//                               UtilisateurRepository utilisateurs,
//                               CompanyRepository companies,
//                               UtilisateurRoleService utilisateurRoleService
//                               ) {
//     this.crmJdbc = crmJdbc;
//     this.utilisateurs = utilisateurs;
//     this.companies = companies;
//     this.utilisateurRoleService = utilisateurRoleService;
    
//   }


//   @Scheduled(cron = "${scheduling.crm-person-sync-cron:0 * * * * *}")
//   @Transactional
//   public void synchroniserPersons() {
//     final String sql =
//       "SELECT Pers_PersonId, Pers_FirstName, Pers_LastName, Pers_CompanyId, Pers_Title, " +
//       "       Pers_EmailAddress, phon_MobileFullNumber, Pers_PhoneNumber, " +
//       "       ISNULL(Pers_Deleted,0) AS Pers_Deleted " +
//       "FROM vPerson " +
//       "WHERE Pers_CompanyId IS NOT NULL AND ISNULL(Pers_Deleted,0) = 0";

//     List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

//      int compteur = 0;

//     for (Map<String,Object> r : rows) {
//       Integer personId = toInt(r.get("Pers_PersonId"));
//       if (personId == null) continue;
//       if (toInt(r.get("Pers_Deleted")) == 1) continue;

//       String idExterneCrm = personId.toString();
//       if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

//       Integer companyId = toInt(r.get("Pers_CompanyId"));
//       String companyIdCrm = String.valueOf(companyId);

//       Company company = companies.findByIdExterneCrm(companyIdCrm).orElse(null);
//       if (company == null) {
        
//         continue;
//       }

//       String prenom = Objects.toString(r.get("Pers_FirstName"), "");
//       String nom = Objects.toString(r.get("Pers_LastName"), "");
//       String email = Objects.toString(r.get("Pers_EmailAddress"), "");
//       String telephone = Objects.toString(r.get("phon_MobileFullNumber"), "");
//       if (telephone.isEmpty()) {
//         telephone = Objects.toString(r.get("Pers_PhoneNumber"), "");
//       }

//       String identifiant = genererIdentifiantUnique(prenom, nom, company);

//       String motDePasseTemporaire = genererMotDePasseTemporaire();

//       Utilisateur utilisateur = new Utilisateur();
//       utilisateur.setCompanyId(company.getId());
//       utilisateur.setIdExterneCrm(idExterneCrm);
//       utilisateur.setIdentifiant(identifiant);
//       utilisateur.setMotDePasseHash(BCrypt.hashpw(motDePasseTemporaire, BCrypt.gensalt()).getBytes());
//       utilisateur.setMotDePasseSalt(null);
//       utilisateur.setNom(nom);
//       utilisateur.setPrenom(prenom);
//       utilisateur.setEmail(email);
//       utilisateur.setTelephone(telephone);
//       utilisateur.setWhatsappNumero(null);
//       utilisateur.setActif(true);
//       utilisateur.setDateCreation(LocalDateTime.now());
//       utilisateur.setDateMiseAJour(LocalDateTime.now());

//       utilisateurs.save(utilisateur);

//       UtilisateurRole ur = new UtilisateurRole();
//       ur.setUtilisateur(utilisateur);
//       Role clientRole = new Role();
//       clientRole.setId(1); // 1 = CLIENT
//       ur.setRole(clientRole);
//       ur.setCompany(company);
//       utilisateurRoleService.enregistrerUtilisateurRole(ur);
      
//       compteur++;
//       System.out.println("Utilisateur numéro: " + compteur );
//     }
//   }

//   /**
//    * Génère un identifiant unique pour l'utilisateur
//    */
//   // private String genererIdentifiantUnique(String prenom, String nom, Company company) {
//   //   String baseIdentifiant = (prenom.charAt(0) + nom).toLowerCase().replaceAll("[^a-z0-9]", "");
    
//   //   // Si l'identifiant existe déjà, ajouter un suffixe numérique
//   //   String identifiant = baseIdentifiant;
//   //   int suffixe = 1;
//   //   while (utilisateurs.findByIdentifiant(identifiant).isPresent()) {
//   //     identifiant = baseIdentifiant + suffixe;
//   //     suffixe++;
//   //   }
    
//   //   return identifiant;
//   // }

//   private String genererIdentifiantUnique(String prenom, String nom, Company company) {
//     String baseIdentifiant;
//     if (prenom != null && !prenom.isEmpty() && nom != null && !nom.isEmpty()) {
//         baseIdentifiant = (prenom.charAt(0) + nom).toLowerCase().replaceAll("[^a-z0-9]", "");
//     } else if (nom != null && !nom.isEmpty()) {
//         baseIdentifiant = nom.toLowerCase().replaceAll("[^a-z0-9]", "");
//     } else if (prenom != null && !prenom.isEmpty()) {
//         baseIdentifiant = prenom.toLowerCase().replaceAll("[^a-z0-9]", "");
//     } else {
//         baseIdentifiant = "user";
//     }

//     // Si l'identifiant existe déjà, ajouter un suffixe numérique
//     String identifiant = baseIdentifiant;
//     int suffixe = 1;
//     while (utilisateurs.findByIdentifiant(identifiant).isPresent()) {
//         identifiant = baseIdentifiant + suffixe;
//         suffixe++;
//     }

//     return identifiant;
// }

//   private String genererMotDePasseTemporaire() {
//     // return "Optimada" + UUID.randomUUID().toString().substring(0, 8) + "!";
//     return "test123+";
//   }

//   private Integer toInt(Object o) {
//     if (o == null) return null;
//     if (o instanceof Number) return ((Number)o).intValue();
//     try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
//   }
// }