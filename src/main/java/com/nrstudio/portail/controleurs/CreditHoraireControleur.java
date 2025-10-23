package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.dto.CreditHoraireDto;
import com.nrstudio.portail.services.CreditHoraireService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits-horaires")
public class CreditHoraireControleur {

  private final CreditHoraireService creditHoraireService;

  public CreditHoraireControleur(CreditHoraireService creditHoraireService) {
    this.creditHoraireService = creditHoraireService;
  }

  @GetMapping("/client/{userId}")
  public ResponseEntity<List<CreditHoraireDto>> getCreditsParUtilisateur(@PathVariable Integer userId) {
    try {
      // TODO: Récupérer le companyId à partir du userId
      // Pour l'instant, retourner une liste vide ou gérer autrement
      Integer companyId = 1; // À remplacer par la vraie logique
      List<CreditHoraireDto> credits = creditHoraireService.getCreditsParCompany(companyId);
      return ResponseEntity.ok(credits);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/company/{companyId}")
  public ResponseEntity<List<CreditHoraireDto>> getCreditsParCompany(@PathVariable Integer companyId) {
    try {
      List<CreditHoraireDto> credits = creditHoraireService.getCreditsParCompany(companyId);
      return ResponseEntity.ok(credits);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/company/{companyId}/actifs")
  public ResponseEntity<List<CreditHoraireDto>> getCreditsActifs(@PathVariable Integer companyId) {
    try {
      List<CreditHoraireDto> credits = creditHoraireService.getCreditsActifs(companyId);
      return ResponseEntity.ok(credits);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/company/{companyId}/restant")
  public ResponseEntity<Integer> getHeuresRestantes(@PathVariable Integer companyId) {
    try {
      Integer heuresRestantes = creditHoraireService.getHeuresRestantesTotal(companyId);
      return ResponseEntity.ok(heuresRestantes);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
