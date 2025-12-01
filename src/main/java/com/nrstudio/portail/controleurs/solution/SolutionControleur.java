package com.nrstudio.portail.controleurs.solution;

import com.nrstudio.portail.dto.solution.SolutionDTO;
import com.nrstudio.portail.services.solution.SolutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solutions")
@CrossOrigin(origins = "*")
public class SolutionControleur {
    
    private final SolutionService solutionService;
    
    public SolutionControleur(SolutionService solutionService) {
        this.solutionService = solutionService;
    }
    
    /**
     * GET /api/solutions/ticket/{ticketId}
     * Récupère toutes les solutions d'un ticket via la table de liaison
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<SolutionDTO>> getSolutionsByTicket(@PathVariable("ticketId") Integer ticketId) {
        System.out.println("🔍 Récupération des solutions pour le ticket id: " + ticketId);
        try {
            List<SolutionDTO> solutions = solutionService.getSolutionsByTicketId(ticketId);
            System.out.println("✅ " + solutions.size() + " solutions trouvées pour le ticket " + ticketId);
            return ResponseEntity.ok(solutions);
        } catch (RuntimeException e) {
            System.err.println("❌ Ticket non trouvé: " + ticketId + " - " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Erreur serveur pour ticket " + ticketId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * GET /api/solutions/{id}
     * Récupère une solution par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SolutionDTO> getSolutionById(@PathVariable("id") Integer id) {
        try {
            SolutionDTO solution = solutionService.getSolutionById(id);
            return ResponseEntity.ok(solution);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/solutions/test/{ticketId}
     * Endpoint de test pour vérifier la liaison
     */
    @GetMapping("/test/{ticketId}")
    public ResponseEntity<String> testLiaison(@PathVariable("ticketId") Integer ticketId) {
        try {
            List<SolutionDTO> solutions = solutionService.getSolutionsByTicketId(ticketId);
            return ResponseEntity.ok("Test réussi - " + solutions.size() + " solutions trouvées pour le ticket " + ticketId);
        } catch (Exception e) {
            return ResponseEntity.ok("Test échoué - Erreur: " + e.getMessage());
        }
    }
}