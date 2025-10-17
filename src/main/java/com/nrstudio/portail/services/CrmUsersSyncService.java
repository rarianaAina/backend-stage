// package com.nrstudio.portail.services;

// import com.nrstudio.portail.depots.UtilisateurRepository;
// import com.nrstudio.portail.domaine.Utilisateur;
// import com.nrstudio.portail.domaine.UtilisateurRole;
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
// import java.util.UUID;

// @Service
// public class CrmUsersSyncService {

//   private final JdbcTemplate crmJdbc;
//   private final UtilisateurRepository utilisateurs;
//   private final UtilisateurRoleService utilisateurRoleService;

//   public CrmUsersSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
//                              UtilisateurRepository utilisateurs,
//                              UtilisateurRoleService utilisateurRoleService) {
//     this.crmJdbc = crmJdbc;
//     this.utilisateurs = utilisateurs;
//     this.utilisateurRoleService = utilisateurRoleService;
//   }

//   @Scheduled(cron = "0 * * * * *")
//   @Transactional
//   public void synchroniserUsers() {
//     final String sql =
//       "SELECT User_UserId, User_FirstName, User_LastName, User_EmailAddress, " +
//       "       User_Phone, ISNULL(User_Deleted,0) AS User_Deleted " +
//       "FROM dbo.Users";

//     List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

//     for (Map<String,Object> r : rows) {
//       Integer userId = toInt(r.get("User_UserId"));
//       if (userId == null) continue;
//       if (toInt(r.get("User_Deleted")) == 1) continue;

//       String idExterneCrm = userId.toString();
//       if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

//       String prenom = Objects.toString(r.get("User_FirstName"), "");
//       String nom = Objects.toString(r.get("User_LastName"), "");
//       String email = Objects.toString(r.get("User_EmailAddress"), null);
//       String telephone = Objects.toString(r.get("User_Phone"), null);

//       String identifiant = genererIdentifiant(email, userId);


//       Utilisateur user = new Utilisateur();
//       user.setIdentifiant(identifiant);
//       user.setNom(nom);
//       user.setPrenom(prenom);
//       user.setEmail(email);
//       user.setTelephone(telephone);
//       user.setActif(true);
//       user.setIdExterneCrm(idExterneCrm);
//       user.setDateCreation(LocalDateTime.now());
//       user.setDateMiseAJour(LocalDateTime.now());

//       String motDePasseTemporaire = genererMotDePasseTemporaire();
//       user.setMotDePasseHash(BCrypt.hashpw(motDePasseTemporaire, BCrypt.gensalt()).getBytes());

//       utilisateurs.save(user);

//       Role role = new Role();
//       role.setId(2); // 2 = Consultant
//       UtilisateurRole ur = new UtilisateurRole();
//       ur.setUtilisateur(user);
//       ur.setRole(role);
//       ur.setCompany(null); // ou renseigne la company si besoin
//       utilisateurRoleService.enregistrerUtilisateurRole(ur);
//       System.out.println("Utilisateur interne créé: " + identifiant + " / Rôle: " + " / MDP temporaire: " + motDePasseTemporaire);
//     }
//   }


//   private String genererIdentifiant(String email, Integer userId) {
//     if (email != null && !email.trim().isEmpty()) {
//       String identifiantBase = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
//       if (utilisateurs.findByIdentifiant(identifiantBase).isEmpty()) {
//         return identifiantBase;
//       }
//     }
//     return "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
//   }

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
