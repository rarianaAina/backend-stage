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
public class CrmPersonSyncService {

  private final JdbcTemplate crmJdbc;
  private final UtilisateurRepository utilisateurs;

  public CrmPersonSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              UtilisateurRepository utilisateurs) {
    this.crmJdbc = crmJdbc;
    this.utilisateurs = utilisateurs;
  }

  @Scheduled(cron = "0 10 2 * * *")
  @Transactional
  public void synchroniserPersons() {
    final String sql =
      "SELECT Pers_PersonId, Pers_FirstName, Pers_LastName, Pers_CompanyId, " +
      "       Pers_EmailAddress, Pers_PhoneNumber, ISNULL(Pers_Deleted,0) AS Pers_Deleted " +
      "FROM dbo.Person " +
      "WHERE Pers_CompanyId IS NOT NULL";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer personId = toInt(r.get("Pers_PersonId"));
      if (personId == null) continue;
      if (toInt(r.get("Pers_Deleted")) == 1) continue;

      String idExterneCrm = "PERSON-" + personId;
      if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

      Integer companyId = toInt(r.get("Pers_CompanyId"));
      String prenom = Objects.toString(r.get("Pers_FirstName"), "");
      String nom = Objects.toString(r.get("Pers_LastName"), "");
      String email = Objects.toString(r.get("Pers_EmailAddress"), null);
      String telephone = Objects.toString(r.get("Pers_PhoneNumber"), null);

      String companyNom = recupererNomCompany(companyId);

      String identifiant = genererIdentifiant(email, personId);

      Utilisateur user = new Utilisateur();
      user.setIdentifiant(identifiant);
      user.setNom(nom);
      user.setPrenom(prenom);
      user.setEmail(email);
      user.setTelephone(telephone);
      user.setActif(true);
      user.setTypeCompte("PERSON");
      user.setCompanyId(companyId);
      user.setCompanyNom(companyNom);
      user.setRole("CLIENT");
      user.setIdExterneCrm(idExterneCrm);
      user.setDateMiseAJour(OffsetDateTime.now());

      String motDePasseTemporaire = genererMotDePasseTemporaire();
      user.setMotDePasseHash(BCrypt.hashpw(motDePasseTemporaire, BCrypt.gensalt()).getBytes());

      utilisateurs.save(user);

      System.out.println("Utilisateur client créé: " + identifiant + " / MDP temporaire: " + motDePasseTemporaire);
    }
  }

  private String recupererNomCompany(Integer companyId) {
    if (companyId == null) return null;
    try {
      String nom = crmJdbc.queryForObject(
        "SELECT Comp_Name FROM dbo.Company WHERE Comp_CompanyId = ?",
        String.class,
        companyId
      );
      return nom;
    } catch (Exception e) {
      return "Société " + companyId;
    }
  }

  private String genererIdentifiant(String email, Integer personId) {
    if (email != null && !email.trim().isEmpty()) {
      String identifiantBase = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
      if (utilisateurs.findByIdentifiant(identifiantBase).isEmpty()) {
        return identifiantBase;
      }
    }
    return "person_" + personId + "_" + UUID.randomUUID().toString().substring(0, 8);
  }

  private String genererMotDePasseTemporaire() {
    return "Welcome" + UUID.randomUUID().toString().substring(0, 8) + "!";
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
