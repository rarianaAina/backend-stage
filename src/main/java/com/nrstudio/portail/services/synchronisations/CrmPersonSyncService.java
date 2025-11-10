package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.domaine.UtilisateurRole;
import com.nrstudio.portail.services.UtilisateurRoleService;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.Role;

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
  private final UtilisateurRepository utilisateurs;
  private final CompanyRepository companies;
  private final UtilisateurRoleService utilisateurRoleService;
  private final SynchronisationManager synchronisationManager;

  public CrmPersonSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                              UtilisateurRepository utilisateurs,
                              CompanyRepository companies,
                              UtilisateurRoleService utilisateurRoleService,
                              SynchronisationManager synchronisationManager) {
    this.crmJdbc = crmJdbc;
    this.utilisateurs = utilisateurs;
    this.companies = companies;
    this.utilisateurRoleService = utilisateurRoleService;
    this.synchronisationManager = synchronisationManager;
  }

  // Synchronisation planifiée - non interruptible
  @Scheduled(cron = "${scheduling.crm-person-sync-cron:0 * * * * *}")
  @Transactional
  public void synchroniserPersons() {
    System.out.println("🚀 Début de la synchronisation planifiée des personnes - " + LocalDateTime.now());
    executerSynchronisationPlanifiee();
  }

  // Synchronisation manuelle - interruptible
  @Transactional
  public void synchroniserPersonsManuellement() {
    System.out.println("🚀 Début de la synchronisation manuelle des personnes - " + LocalDateTime.now());
    executerSynchronisationManuelle();
  }

  private void executerSynchronisationPlanifiee() {
    final String sql =
      "SELECT Pers_PersonId, Pers_FirstName, Pers_LastName, Pers_CompanyId, Pers_Title, " +
      "       Pers_EmailAddress, phon_MobileFullNumber, Pers_PhoneNumber, " +
      "       ISNULL(Pers_Deleted,0) AS Pers_Deleted " +
      "FROM vPerson " +
      "WHERE Pers_CompanyId IS NOT NULL AND ISNULL(Pers_Deleted,0) = 0";

    List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
    int compteur = 0;

    for (Map<String,Object> r : rows) {
      try {
        traiterPersonne(r);
        compteur++;
        System.out.println("Utilisateur numéro: " + compteur);
      } catch (Exception e) {
        System.err.println("❌ Erreur lors du traitement de la personne: " + e.getMessage());
      }
    }
    
    System.out.println("✅ Synchronisation planifiée terminée - " + compteur + " utilisateurs traités");
  }

  private void executerSynchronisationManuelle() {
    final String typeSync = "personnes";
    
    // Vérifier si une synchronisation est déjà en cours
    if (synchronisationManager.estEnCours(typeSync)) {
      throw new IllegalStateException("Une synchronisation des personnes est déjà en cours");
    }

    // Démarrer la synchronisation
    synchronisationManager.demarrerSynchronisation(typeSync);
    
    // Exécuter dans un thread séparé pour permettre l'interruption
    Thread syncThread = new Thread(() -> {
      try {
        synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
        
        final String sql =
          "SELECT Pers_PersonId, Pers_FirstName, Pers_LastName, Pers_CompanyId, Pers_Title, " +
          "       Pers_EmailAddress, phon_MobileFullNumber, Pers_PhoneNumber, " +
          "       ISNULL(Pers_Deleted,0) AS Pers_Deleted " +
          "FROM vPerson " +
          "WHERE Pers_CompanyId IS NOT NULL AND ISNULL(Pers_Deleted,0) = 0";

        List<Map<String,Object>> rows = crmJdbc.queryForList(sql);
        int compteur = 0;

        for (Map<String,Object> r : rows) {
          // Vérifier si l'arrêt a été demandé
          if (synchronisationManager.doitArreter(typeSync)) {
            System.out.println("🛑 Synchronisation manuelle des personnes arrêtée à la demande");
            return;
          }

          try {
            traiterPersonne(r);
            compteur++;
            System.out.println("Utilisateur numéro: " + compteur);
          } catch (Exception e) {
            System.err.println("❌ Erreur lors du traitement de la personne: " + e.getMessage());
          }
          
          // Petit délai pour permettre une interruption plus réactive
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            System.out.println("🛑 Synchronisation manuelle interrompue");
            Thread.currentThread().interrupt();
            return;
          }
        }
        
        System.out.println("✅ Synchronisation manuelle terminée - " + compteur + " utilisateurs traités");
        
      } catch (Exception e) {
        System.err.println("❌ Erreur lors de la synchronisation manuelle: " + e.getMessage());
      } finally {
        synchronisationManager.supprimerThread(typeSync);
      }
    });
    
    syncThread.start();
  }

  private void traiterPersonne(Map<String, Object> r) {
    Integer personId = toInt(r.get("Pers_PersonId"));
    if (personId == null) return;
    if (toInt(r.get("Pers_Deleted")) == 1) return;

    String idExterneCrm = personId.toString();
    if (utilisateurs.findByIdExterneCrm(idExterneCrm).isPresent()) return;

    Integer companyId = toInt(r.get("Pers_CompanyId"));
    String companyIdCrm = String.valueOf(companyId);

    Company company = companies.findByIdExterneCrm(companyIdCrm).orElse(null);
    if (company == null) {
      return;
    }

    String prenom = Objects.toString(r.get("Pers_FirstName"), "");
    String nom = Objects.toString(r.get("Pers_LastName"), "");
    String email = Objects.toString(r.get("Pers_EmailAddress"), "");
    String telephone = Objects.toString(r.get("phon_MobileFullNumber"), "");
    if (telephone.isEmpty()) {
      telephone = Objects.toString(r.get("Pers_PhoneNumber"), "");
    }

    String identifiant = genererIdentifiantUnique(prenom, nom, company);
    String motDePasseTemporaire = genererMotDePasseTemporaire();

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setCompanyId(company.getId());
    utilisateur.setIdExterneCrm(idExterneCrm);
    utilisateur.setIdentifiant(identifiant);
    utilisateur.setMotDePasseHash(BCrypt.hashpw(motDePasseTemporaire, BCrypt.gensalt()).getBytes());
    utilisateur.setMotDePasseSalt(null);
    utilisateur.setNom(nom);
    utilisateur.setPrenom(prenom);
    utilisateur.setEmail(email);
    utilisateur.setTelephone(telephone);
    utilisateur.setWhatsappNumero(null);
    utilisateur.setActif(true);
    utilisateur.setDateCreation(LocalDateTime.now());
    utilisateur.setDateMiseAJour(LocalDateTime.now());

    utilisateurs.save(utilisateur);

    UtilisateurRole ur = new UtilisateurRole();
    ur.setUtilisateur(utilisateur);
    Role clientRole = new Role();
    clientRole.setId(1); // 1 = CLIENT
    ur.setRole(clientRole);
    ur.setCompany(company);
    utilisateurRoleService.enregistrerUtilisateurRole(ur);
  }

  private String genererIdentifiantUnique(String prenom, String nom, Company company) {
    String baseIdentifiant;
    if (prenom != null && !prenom.isEmpty() && nom != null && !nom.isEmpty()) {
        baseIdentifiant = (prenom.charAt(0) + nom).toLowerCase().replaceAll("[^a-z0-9]", "");
    } else if (nom != null && !nom.isEmpty()) {
        baseIdentifiant = nom.toLowerCase().replaceAll("[^a-z0-9]", "");
    } else if (prenom != null && !prenom.isEmpty()) {
        baseIdentifiant = prenom.toLowerCase().replaceAll("[^a-z0-9]", "");
    } else {
        baseIdentifiant = "user";
    }

    String identifiant = baseIdentifiant;
    int suffixe = 1;
    while (utilisateurs.findByIdentifiant(identifiant).isPresent()) {
        identifiant = baseIdentifiant + suffixe;
        suffixe++;
    }

    return identifiant;
  }

  private String genererMotDePasseTemporaire() {
    return "test123+";
  }

  private Integer toInt(Object o) {
    if (o == null) return null;
    if (o instanceof Number) return ((Number)o).intValue();
    try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
  }
}