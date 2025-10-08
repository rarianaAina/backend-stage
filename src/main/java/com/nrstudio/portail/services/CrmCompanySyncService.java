package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.ClientRepository;
import com.nrstudio.portail.domaine.Client;
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
public class CrmCompanySyncService {

  private final JdbcTemplate crmJdbc;
  private final ClientRepository clients;

  public CrmCompanySyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                               ClientRepository clients) {
    this.crmJdbc = crmJdbc;
    this.clients = clients;
  }

  @Scheduled(cron = "0 0 2 * * *")
  @Transactional
  public void synchroniserCompanies() {
    final String sql =
      "SELECT Comp_CompanyId, Comp_Name, Comp_Type, Comp_PhoneNumber, Comp_EmailAddress, " +
      "       ISNULL(Comp_Deleted,0) AS Comp_Deleted " +
      "FROM dbo.Company " +
      "WHERE Comp_Type = 'Customer'";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);

    for (Map<String,Object> r : rows) {
      Integer companyId = toInt(r.get("Comp_CompanyId"));
      if (companyId == null) continue;
      if (toInt(r.get("Comp_Deleted")) == 1) continue;

      String idExterneCrm = String.valueOf(companyId);
      Client clientExistant = clients.findByIdExterneCrm(idExterneCrm).orElse(null);

      String raisonSociale = Objects.toString(r.get("Comp_Name"), "Société " + companyId);
      String telephone = Objects.toString(r.get("Comp_PhoneNumber"), null);
      String email = Objects.toString(r.get("Comp_EmailAddress"), null);

      if (clientExistant != null) {
        clientExistant.setRaisonSociale(raisonSociale);
        clientExistant.setTelephone(telephone);
        clientExistant.setEmail(email);
        clientExistant.setDateMiseAJour(LocalDateTime.now());
        clients.save(clientExistant);
      } else {
        Client nouveauClient = new Client();
        nouveauClient.setIdExterneCrm(idExterneCrm);
        nouveauClient.setCodeClient("CLI-" + companyId);
        nouveauClient.setRaisonSociale(raisonSociale);
        nouveauClient.setTelephone(telephone);
        nouveauClient.setEmail(email);
        nouveauClient.setActif(true);
        nouveauClient.setDateCreation(LocalDateTime.now());
        nouveauClient.setDateMiseAJour(LocalDateTime.now());
        clients.save(nouveauClient);
      }
    }
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}
