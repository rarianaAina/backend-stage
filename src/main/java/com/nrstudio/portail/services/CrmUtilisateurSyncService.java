package com.nrstudio.portail.services;

import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.depots.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmUtilisateurSyncService {

  private final JdbcTemplate crmJdbc;
  private final UtilisateurRepository repo;

  public CrmUtilisateurSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                   UtilisateurRepository repo) {
    this.crmJdbc = crmJdbc;
    this.repo = repo;
  }

  @Scheduled(cron = "0 * * * * *") // toutes les 30 min
  @Transactional
  public void synchroniser() {

    final String sql =
        "SELECT " +
        "  User_UserId, " +
        "  User_Logon, " +
        "  User_EmailAddress, " +
        "  ISNULL(User_Deleted, 0)  AS User_Deleted, " +
        "  ISNULL(User_Disabled, 0) AS User_Disabled, " +
        "  User_FirstName, " +
        "  User_LastName " +
        "FROM dbo.Users " +
        "WHERE ISNULL(User_Deleted, 0) = 0"; // on ignore les supprimés

    List<Map<String, Object>> lignes = crmJdbc.queryForList(sql);

    for (Map<String, Object> r : lignes) {
      String idCrm   = Objects.toString(r.get("User_UserId"), null);
      String logon   = Objects.toString(r.get("User_Logon"), null);
      String email   = Objects.toString(r.get("User_EmailAddress"), null);
      String prenom  = Objects.toString(r.get("User_FirstName"), null);
      String nom     = Objects.toString(r.get("User_LastName"), null);

      int deleted  = toInt(r.get("User_Deleted"));
      int disabled = toInt(r.get("User_Disabled"));
      boolean actif = (deleted == 0) && (disabled == 0);

      // On exige un id CRM + un identifiant (logon)
      if (idCrm == null || idCrm.isEmpty() || logon == null || logon.isEmpty()) {
        continue;
      }

      Utilisateur u = repo.findByIdExterneCrm(idCrm).orElse(null);
      if (u == null) u = new Utilisateur();

      u.setIdExterneCrm(idCrm);
      u.setIdentifiant(logon);
      u.setEmail(email);
      u.setActif(actif);

      // si le nom/prénom sont vides, on met au moins le logon
      if (u.getNom() == null || u.getNom().isEmpty()) {
        u.setNom(nom != null && !nom.isEmpty() ? nom : logon);
      } else if (nom != null && !nom.isEmpty()) {
        u.setNom(nom);
      }

      if (u.getPrenom() == null || u.getPrenom().isEmpty()) {
        if (prenom != null && !prenom.isEmpty()) u.setPrenom(prenom);
      } else if (prenom != null && !prenom.isEmpty()) {
        u.setPrenom(prenom);
      }

      repo.save(u);
    }
  }

  private int toInt(Object o) {
    if (o == null) return 0;
    if (o instanceof Number) return ((Number) o).intValue();
    if (o instanceof Boolean) return ((Boolean) o) ? 1 : 0;
    try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
  }
}
