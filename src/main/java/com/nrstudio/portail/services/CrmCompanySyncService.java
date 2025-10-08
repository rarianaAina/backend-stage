package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.domaine.Utilisateur;
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
public class CrmCompanySyncService {

  private final JdbcTemplate crmJdbc;
  private final UtilisateurRepository utilisateurs;

  public CrmCompanySyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                               UtilisateurRepository utilisateurs) {
    this.crmJdbc = crmJdbc;
    this.utilisateurs = utilisateurs;
  }

  @Scheduled(cron = "0 0 2 * * *")
  @Transactional
  public void synchroniserCompanies() {
    final String sql =
      "SELECT Comp_CompanyId, Comp_Name, Comp_Type, ISNULL(Comp_Deleted,0) AS Comp_Deleted " +
      "FROM dbo.Company " +
      "WHERE Comp_Type = 'Customer'";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer companyId = toInt(r.get("Comp_CompanyId"));
      if (companyId == null) continue;
      if (toInt(r.get("Comp_Deleted")) == 1) continue;

      String idExterneCrm = "COMPANY-" + companyId;
      if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

      String companyName = Objects.toString(r.get("Comp_Name"), "Société " + companyId);

      Utilisateur companyUser = new Utilisateur();
      companyUser.setIdentifiant("company_" + companyId + "_" + UUID.randomUUID().toString().substring(0, 8));
      companyUser.setNom(companyName);
      companyUser.setPrenom("");
      companyUser.setEmail(null);
      companyUser.setTelephone(null);
      companyUser.setActif(true);
      companyUser.setTypeCompte("COMPANY");
      companyUser.setCompanyId(companyId);
      companyUser.setCompanyNom(companyName);
      companyUser.setRole("CLIENT");
      companyUser.setIdExterneCrm(idExterneCrm);
      companyUser.setDateMiseAJour(OffsetDateTime.now());

      utilisateurs.save(companyUser);
    }
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
