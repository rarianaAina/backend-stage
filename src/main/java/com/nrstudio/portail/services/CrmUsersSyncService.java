package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.domaine.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CrmUsersSyncService {

  private final JdbcTemplate crmJdbc;
  private final UtilisateurRepository utilisateurs;

  public CrmUsersSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                             UtilisateurRepository utilisateurs) {
    this.crmJdbc = crmJdbc;
    this.utilisateurs = utilisateurs;
  }

  @Scheduled(cron = "0 20 2 * * *")
  @Transactional
  public void synchroniserUsers() {
    final String sql =
      "SELECT User_UserId, User_FirstName, User_LastName, User_EmailAddress, " +
      "       User_Phone, ISNULL(User_Deleted,0) AS User_Deleted " +
      "FROM dbo.Users";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer userId = toInt(r.get("User_UserId"));
      if (userId == null) continue;
      if (toInt(r.get("User_Deleted")) == 1) continue;

      String idExterneCrm = "USER-" + userId;
      if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

      String prenom = Objects.toString(r.get("User_FirstName"), "");
      String nom = Objects.toString(r.get("User_LastName"), "");
      String email = Objects.toString(r.get("User_EmailAddress"), null);
      String telephone = Objects.toString(r.get("User_Phone"), null);

      String identifiant = genererIdentifiant(email, userId);

      String role = determinerRole(userId);

      Utilisateur user = new Utilisateur();
      user.setIdentifiant(identifiant);
      user.setNom(nom);
      user.setPrenom(prenom);
      user.setEmail(email);
      user.setTelephone(telephone);
      user.setActif(true);
      user.setTypeCompte("INTERNAL");
      user.setCompanyId(null);
      user.setCompanyNom("OPTIMADA");
      user.setRole(role);
      user.setIdExterneCrm(idExterneCrm);
      user.setDateMiseAJour(OffsetDateTime.now());

      String motDePasseTemporaire = genererMotDePasseTemporaire();
      user.setMotDePasseHash(BCrypt.hashpw(motDePasseTemporaire, BCrypt.gensalt()).getBytes());

      utilisateurs.save(user);

      System.out.println("Utilisateur interne créé: " + identifiant + " / Rôle: " + role + " / MDP temporaire: " + motDePasseTemporaire);
    }
  }

  private String determinerRole(Integer userId) {
    try {
      Integer count = crmJdbc.queryForObject(
        "SELECT COUNT(*) FROM dbo.UserSecurity " +
        "WHERE UsrS_UserId = ? AND UsrS_SecurityGroupId IN (1, 2)",
        Integer.class,
        userId
      );
      return (count != null && count > 0) ? "ADMIN" : "CONSULTANT";
    } catch (Exception e) {
      return "CONSULTANT";
    }
  }

  private String genererIdentifiant(String email, Integer userId) {
    if (email != null && !email.trim().isEmpty()) {
      String identifiantBase = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
      if (utilisateurs.findByIdentifiant(identifiantBase).isEmpty()) {
        return identifiantBase;
      }
    }
    return "user_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8);
  }

  private String genererMotDePasseTemporaire() {
    return "Optimada" + UUID.randomUUID().toString().substring(0, 8) + "!";
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
