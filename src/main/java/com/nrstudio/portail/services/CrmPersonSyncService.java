package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.ClientRepository;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Client;
import com.nrstudio.portail.domaine.Company;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CrmPersonSyncService {

  private final JdbcTemplate crmJdbc;
  private final ClientRepository clients;
  private final CompanyRepository companies;

  public CrmPersonSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              ClientRepository clients,
                              CompanyRepository companies) {
    this.crmJdbc = crmJdbc;
    this.clients = clients;
    this.companies = companies;
  }

  @Scheduled(cron = "0 * * * * *")
  @Transactional
  public void synchroniserPersons() {
    final String sql =
      "SELECT Pers_PersonId, Pers_FirstName, Pers_LastName, Pers_CompanyId, Pers_Title, " +
      "       ISNULL(Pers_Deleted,0) AS Pers_Deleted " +
      "FROM dbo.Person " +
      "WHERE Pers_CompanyId IS NOT NULL AND ISNULL(Pers_Deleted,0) = 0";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer personId = toInt(r.get("Pers_PersonId"));
      if (personId == null) continue;
      if (toInt(r.get("Pers_Deleted")) == 1) continue;

      String idExterneCrm = "PERSON-" + personId;
      if (clients.findByIdExterneCrm(idExterneCrm).isPresent()) continue;

      Integer companyId = toInt(r.get("Pers_CompanyId"));
      String companyIdCrm = String.valueOf(companyId);

      Company company = companies.findByIdExterneCrm(companyIdCrm).orElse(null);
      if (company == null) {
        System.out.println("Company CRM " + companyId + " non trouvée pour Person " + personId);
        continue;
      }

      String prenom = Objects.toString(r.get("Pers_FirstName"), "");
      String nom = Objects.toString(r.get("Pers_LastName"), "");
      String fonction = Objects.toString(r.get("Pers_Title"), null);
      String email = null;
      String telephone = null;

      Client client = new Client();
      client.setCompanyId(company.getId());
      client.setIdExterneCrm(idExterneCrm);
      client.setNom(nom);
      client.setPrenom(prenom);
      client.setFonction(fonction);
      client.setEmail(email);
      client.setTelephone(telephone);
      client.setWhatsappNumero(null);
      client.setPrincipal(false);
      client.setActif(true);
      client.setDateCreation(LocalDateTime.now());
      client.setDateMiseAJour(LocalDateTime.now());

      clients.save(client);

      System.out.println("Client créé: " + prenom + " " + nom + " (Company: " + company.getNom() + ")");
    }
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
