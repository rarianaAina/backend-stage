package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.ChartDataDto;
import com.nrstudio.portail.dto.DashboardAdminDto;
import com.nrstudio.portail.dto.DashboardClientDto;
import com.nrstudio.portail.services.DashboardService;
import com.nrstudio.portail.depots.UtilisateurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardControleur {

  private final DashboardService dashboardService;
  private final UtilisateurRepository utilisateurRepository;

  // Injection par constructeur avec les deux dépendances
  public DashboardControleur(DashboardService dashboardService, 
                           UtilisateurRepository utilisateurRepository) {
    this.dashboardService = dashboardService;
    this.utilisateurRepository = utilisateurRepository;
  }

  @GetMapping("/client/{userId}")
  public ResponseEntity<DashboardClientDto> getDashboardClient(@PathVariable("userId") Integer userId) {
    try {
      System.out.println("=== DÉBUT APPEL DASHBOARD CLIENT ===");
      System.out.println("User ID reçu: " + userId);
      
      // Trouver l'idExterneCrm associé à l'utilisateur


      DashboardClientDto dashboard = dashboardService.getDashboardClient(userId);
      
      System.out.println("Dashboard généré avec succès");
      System.out.println("Crédits horaires: " + (dashboard.getCreditsHoraires() != null ? dashboard.getCreditsHoraires().size() : "null"));
      
      return ResponseEntity.ok(dashboard);
    } catch (Exception e) {
      System.out.println("ERREUR dans getDashboardClient: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/client/{userId}/chart")
  public ResponseEntity<ChartDataDto> getChartDataClient(@PathVariable("userId") Integer userId) {
    try {
      ChartDataDto chartData = dashboardService.getChartDataClient(userId);
      return ResponseEntity.ok(chartData);
    } catch (Exception e) {
      System.out.println("ERREUR dans getChartDataClient: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/admin")
  public ResponseEntity<DashboardAdminDto> getDashboardAdmin() {
    try {
      DashboardAdminDto dashboard = dashboardService.getDashboardAdmin();
      return ResponseEntity.ok(dashboard);
    } catch (Exception e) {
      System.out.println("ERREUR dans getDashboardAdmin: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/admin/chart")
  public ResponseEntity<ChartDataDto> getChartDataAdmin() {
    try {
      ChartDataDto chartData = dashboardService.getChartDataAdmin();
      return ResponseEntity.ok(chartData);
    } catch (Exception e) {
      System.out.println("ERREUR dans getChartDataAdmin: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.badRequest().build();
    }
  }
}