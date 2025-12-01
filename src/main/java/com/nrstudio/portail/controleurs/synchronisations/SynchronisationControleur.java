package com.nrstudio.portail.controleurs.synchronisations;

import com.nrstudio.portail.services.synchronisations.CrmCompanySyncService;
import com.nrstudio.portail.services.synchronisations.CrmCreditHoraireSyncService;
import com.nrstudio.portail.services.synchronisations.CrmPersonSyncService;
import com.nrstudio.portail.services.synchronisations.CrmProductSyncService;
import com.nrstudio.portail.services.synchronisations.CrmTicketSyncService;
import com.nrstudio.portail.services.solution.CrmSolutionsSyncService;
import com.nrstudio.portail.services.solution.CrmSolutionTicketSyncService;
import com.nrstudio.portail.services.synchronisations.SynchronisationManager;
import com.nrstudio.portail.dto.synchronisations.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/synchronisation")
@CrossOrigin
public class SynchronisationControleur {

    private final CrmCompanySyncService crmCompanySyncService;
    private final CrmCreditHoraireSyncService crmCreditHoraireSyncService;
    private final CrmPersonSyncService crmPersonSyncService;
    private final CrmProductSyncService crmProductSyncService;
    private final CrmTicketSyncService crmTicketSyncService;
    private final CrmSolutionsSyncService crmSolutionsSyncService;
    private final CrmSolutionTicketSyncService crmSolutionTicketSyncService;
    private final SynchronisationManager synchronisationManager;

    // Injection par constructeur (recommandé au lieu de @Autowired)
    public SynchronisationControleur(CrmCompanySyncService crmCompanySyncService,
                                   CrmCreditHoraireSyncService crmCreditHoraireSyncService,
                                   CrmPersonSyncService crmPersonSyncService,
                                   CrmProductSyncService crmProductSyncService,
                                   CrmTicketSyncService crmTicketSyncService,
                                   CrmSolutionsSyncService crmSolutionsSyncService,
                                   CrmSolutionTicketSyncService crmSolutionTicketSyncService,
                                   SynchronisationManager synchronisationManager) {
        this.crmCompanySyncService = crmCompanySyncService;
        this.crmCreditHoraireSyncService = crmCreditHoraireSyncService;
        this.crmPersonSyncService = crmPersonSyncService;
        this.crmProductSyncService = crmProductSyncService;
        this.crmTicketSyncService = crmTicketSyncService;
        this.crmSolutionsSyncService = crmSolutionsSyncService;
        this.crmSolutionTicketSyncService = crmSolutionTicketSyncService;
        this.synchronisationManager = synchronisationManager;
    }

    // === COMPANIES ===
    @PostMapping("/companies/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationCompanies() {
        return executerDemarrage("companies", 
            () -> crmCompanySyncService.synchroniserCompaniesManuellement(),
            "Synchronisation des companies démarrée");
    }

    @PostMapping("/companies/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationCompanies() {
        return executerArret("companies", "Synchronisation des companies");
    }

    @GetMapping("/companies/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationCompanies() {
        return getStatut("companies", "Synchronisation des companies");
    }

    // === CRÉDITS HORAIRES ===
    @PostMapping("/credits-horaires/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationCreditHoraire() {
        return executerDemarrage("credits-horaires", 
            () -> crmCreditHoraireSyncService.synchroniserCreditHoraireManuellement(),
            "Synchronisation des crédits horaires démarrée");
    }

    @PostMapping("/credits-horaires/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationCreditHoraire() {
        return executerArret("credits-horaires", "Synchronisation des crédits horaires");
    }

    @GetMapping("/credits-horaires/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationCreditHoraire() {
        return getStatut("credits-horaires", "Synchronisation des crédits horaires");
    }

    // === PERSONNES (UTILISATEURS) ===
    @PostMapping("/personnes/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationPersonnes() {
        return executerDemarrage("personnes", 
            () -> crmPersonSyncService.synchroniserPersonsManuellement(),
            "Synchronisation des personnes démarrée");
    }

    @PostMapping("/personnes/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationPersonnes() {
        return executerArret("personnes", "Synchronisation des personnes");
    }

    @GetMapping("/personnes/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationPersonnes() {
        return getStatut("personnes", "Synchronisation des personnes");
    }

    // === PRODUITS ===
    @PostMapping("/produits/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationProduits() {
        return executerDemarrage("produits", 
            () -> crmProductSyncService.synchroniserProduitsManuellement(),
            "Synchronisation des produits démarrée");
    }

    @PostMapping("/produits/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationProduits() {
        return executerArret("produits", "Synchronisation des produits");
    }

    @GetMapping("/produits/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationProduits() {
        return getStatut("produits", "Synchronisation des produits");
    }

    // === TICKETS ===
    @PostMapping("/tickets/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationTickets() {
        return executerDemarrage("tickets", 
            () -> crmTicketSyncService.importerDepuisCrmManuellement(),
            "Synchronisation des tickets démarrée");
    }

    @PostMapping("/tickets/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationTickets() {
        return executerArret("tickets", "Synchronisation des tickets");
    }

    @GetMapping("/tickets/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationTickets() {
        return getStatut("tickets", "Synchronisation des tickets");
    }

    // === SOLUTIONS ===
    @PostMapping("/solutions/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationSolutions() {
        return executerDemarrage("solutions", 
            () -> crmSolutionsSyncService.synchroniserSolutionsManuellement(),
            "Synchronisation des solutions démarrée");
    }

    @PostMapping("/solutions/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationSolutions() {
        return executerArret("solutions", "Synchronisation des solutions");
    }

    @GetMapping("/solutions/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationSolutions() {
        return getStatut("solutions", "Synchronisation des solutions");
    }

    // === LIAISONS SOLUTIONS-TICKETS ===
    @PostMapping("/liaisons-solutions-tickets/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerSynchronisationLiaisonsSolutionsTickets() {
        return executerDemarrage("liaisons-solutions-tickets", 
            () -> crmSolutionTicketSyncService.synchroniserLiaisonsSolutionsTicketsManuellement(),
            "Synchronisation des liaisons solutions-tickets démarrée");
    }

    @PostMapping("/liaisons-solutions-tickets/arreter")
    public ResponseEntity<SynchronisationResponse> arreterSynchronisationLiaisonsSolutionsTickets() {
        return executerArret("liaisons-solutions-tickets", "Synchronisation des liaisons solutions-tickets");
    }

    @GetMapping("/liaisons-solutions-tickets/statut")
    public ResponseEntity<StatutSynchronisationResponse> getStatutSynchronisationLiaisonsSolutionsTickets() {
        return getStatut("liaisons-solutions-tickets", "Synchronisation des liaisons solutions-tickets");
    }

    // === SYNCHRONISATIONS GLOBALES ===
    @PostMapping("/tout/demarrer")
    public ResponseEntity<SynchronisationResponse> demarrerToutesSynchronisations() {
        try {
            // Vérifier si au moins une synchronisation est déjà en cours
            if (estAuMoinsUneSynchronisationEnCours()) {
                return ResponseEntity.badRequest()
                    .body(new SynchronisationResponse(
                        "EN_COURS", 
                        "Une ou plusieurs synchronisations sont déjà en cours"
                    ));
            }
            
            // Démarrer toutes les synchronisations
            crmCompanySyncService.synchroniserCompaniesManuellement();
            crmCreditHoraireSyncService.synchroniserCreditHoraireManuellement();
            crmPersonSyncService.synchroniserPersonsManuellement();
            crmProductSyncService.synchroniserProduitsManuellement();
            crmTicketSyncService.importerDepuisCrmManuellement();
            crmSolutionsSyncService.synchroniserSolutionsManuellement();
            crmSolutionTicketSyncService.synchroniserLiaisonsSolutionsTicketsManuellement();
            
            return ResponseEntity.ok(new SynchronisationResponse(
                "DEMARREES", 
                "Toutes les synchronisations ont été démarrées"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new SynchronisationResponse(
                    "ERREUR", 
                    "Erreur lors du démarrage global: " + e.getMessage()
                ));
        }
    }

    @PostMapping("/tout/arreter")
    public ResponseEntity<SynchronisationResponse> arreterToutesSynchronisations() {
        try {
            synchronisationManager.arreterSynchronisation("companies");
            synchronisationManager.arreterSynchronisation("credits-horaires");
            synchronisationManager.arreterSynchronisation("personnes");
            synchronisationManager.arreterSynchronisation("produits");
            synchronisationManager.arreterSynchronisation("tickets");
            synchronisationManager.arreterSynchronisation("solutions");
            synchronisationManager.arreterSynchronisation("liaisons-solutions-tickets");
            
            return ResponseEntity.ok(new SynchronisationResponse(
                "ARRET_DEMANDE", 
                "Demande d'arrêt de toutes les synchronisations envoyée"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new SynchronisationResponse(
                    "ERREUR", 
                    "Erreur lors de l'arrêt global: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/tout/statut")
    public ResponseEntity<Map<String, Object>> getStatutToutesSynchronisations() {
        Map<String, Object> statuts = new HashMap<>();
        
        statuts.put("companies", creerStatutDetaille("companies", "Companies"));
        statuts.put("creditsHoraires", creerStatutDetaille("credits-horaires", "Crédits horaires"));
        statuts.put("personnes", creerStatutDetaille("personnes", "Personnes"));
        statuts.put("produits", creerStatutDetaille("produits", "Produits"));
        statuts.put("tickets", creerStatutDetaille("tickets", "Tickets"));
        statuts.put("solutions", creerStatutDetaille("solutions", "Solutions"));
        statuts.put("liaisonsSolutionsTickets", creerStatutDetaille("liaisons-solutions-tickets", "Liaisons solutions-tickets"));
        
        return ResponseEntity.ok(statuts);
    }

    // === MÉTHODES UTILITAIRES PRIVÉES ===
    
    private ResponseEntity<SynchronisationResponse> executerDemarrage(String type, Runnable synchronisation, String messageSucces) {
        try {
            if (synchronisationManager.estEnCours(type)) {
                return ResponseEntity.badRequest()
                    .body(new SynchronisationResponse("EN_COURS", "Une synchronisation est déjà en cours"));
            }
            
            synchronisation.run();
            return ResponseEntity.ok(new SynchronisationResponse("DEMARREE", messageSucces));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new SynchronisationResponse("ERREUR", "Erreur lors du démarrage: " + e.getMessage()));
        }
    }

    private ResponseEntity<SynchronisationResponse> executerArret(String type, String nomSynchronisation) {
        try {
            if (!synchronisationManager.estEnCours(type)) {
                return ResponseEntity.badRequest()
                    .body(new SynchronisationResponse("NON_DEMARREE", "Aucune synchronisation en cours"));
            }
            
            synchronisationManager.arreterSynchronisation(type);
            return ResponseEntity.ok(new SynchronisationResponse("ARRET_DEMANDE", 
                "Demande d'arrêt de la " + nomSynchronisation.toLowerCase() + " envoyée"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new SynchronisationResponse("ERREUR", "Erreur lors de l'arrêt: " + e.getMessage()));
        }
    }

    private ResponseEntity<StatutSynchronisationResponse> getStatut(String type, String nomSynchronisation) {
        boolean enCours = synchronisationManager.estEnCours(type);
        String statut = enCours ? "EN_COURS" : "INACTIVE";
        String message = enCours ? nomSynchronisation + " en cours" : "Aucune synchronisation en cours";
        
        return ResponseEntity.ok(new StatutSynchronisationResponse(statut, message));
    }

    private Map<String, Object> creerStatutDetaille(String type, String nom) {
        Map<String, Object> statut = new HashMap<>();
        boolean enCours = synchronisationManager.estEnCours(type);
        
        statut.put("type", type);
        statut.put("nom", nom);
        statut.put("statut", enCours ? "EN_COURS" : "INACTIVE");
        statut.put("enCours", enCours);
        statut.put("message", enCours ? nom + " en cours" : nom + " inactive");
        
        return statut;
    }

    private boolean estAuMoinsUneSynchronisationEnCours() {
        return synchronisationManager.estEnCours("companies") ||
               synchronisationManager.estEnCours("credits-horaires") ||
               synchronisationManager.estEnCours("personnes") ||
               synchronisationManager.estEnCours("produits") ||
               synchronisationManager.estEnCours("tickets") ||
               synchronisationManager.estEnCours("solutions") ||
               synchronisationManager.estEnCours("liaisons-solutions-tickets");
    }
}