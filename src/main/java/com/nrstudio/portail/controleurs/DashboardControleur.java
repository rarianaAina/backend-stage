package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.ChartDataDto;
import com.nrstudio.portail.dto.DashboardAdminDto;
import com.nrstudio.portail.dto.DashboardClientDto;
import com.nrstudio.portail.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardControleur {

  private final DashboardService dashboardService;

  public DashboardControleur(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/client/{userId}")
  public ResponseEntity<DashboardClientDto> getDashboardClient(@PathVariable Integer userId) {
    try {
      DashboardClientDto dashboard = dashboardService.getDashboardClient(userId);
      return ResponseEntity.ok(dashboard);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/client/{userId}/chart")
  public ResponseEntity<ChartDataDto> getChartDataClient(@PathVariable Integer userId) {
    try {
      ChartDataDto chartData = dashboardService.getChartDataClient(userId);
      return ResponseEntity.ok(chartData);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/admin")
  public ResponseEntity<DashboardAdminDto> getDashboardAdmin() {
    try {
      DashboardAdminDto dashboard = dashboardService.getDashboardAdmin();
      return ResponseEntity.ok(dashboard);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/admin/chart")
  public ResponseEntity<ChartDataDto> getChartDataAdmin() {
    try {
      ChartDataDto chartData = dashboardService.getChartDataAdmin();
      return ResponseEntity.ok(chartData);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
