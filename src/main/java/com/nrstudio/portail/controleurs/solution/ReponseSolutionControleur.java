package com.nrstudio.portail.controleurs.solution;

import com.nrstudio.portail.dto.solution.*;
import com.nrstudio.portail.services.solution.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solutions")
@CrossOrigin(origins = "*")
public class ReponseSolutionControleur {
    
    @Autowired
    private ReponseSolutionService reponseSolutionService;
    
    @PostMapping("/{solutionId}/reponses")
    public ResponseEntity<?> creerReponse(
            @PathVariable Integer solutionId,
            @RequestBody CreateReponseSolutionRequest request
            ) {
        
        try {
            
            request.setSolutionId(solutionId);
            
            ReponseSolutionDTO reponse = reponseSolutionService.creerReponse(request);
            return ResponseEntity.ok(reponse);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur interne du serveur");
        }
    }
    
    @GetMapping("/{solutionId}/reponses")
    public ResponseEntity<?> getReponsesParSolution(@PathVariable Integer solutionId) {
        try {
            List<ReponseSolutionDTO> reponses = reponseSolutionService.getReponsesParSolution(solutionId);
            return ResponseEntity.ok(reponses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la récupération des réponses");
        }
    }
    
    @GetMapping("/{solutionId}/reponses/statistiques")
    public ResponseEntity<?> getStatistiques(@PathVariable Integer solutionId) {
        try {
            StatistiquesReponseDTO statistiques = reponseSolutionService.getStatistiquesSolution(solutionId);
            return ResponseEntity.ok(statistiques);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la récupération des statistiques");
        }
    }
    
    @GetMapping("/{solutionId}/reponses/derniere")
    public ResponseEntity<?> getDerniereReponse(@PathVariable Integer solutionId) {
        try {
            return reponseSolutionService.getDerniereReponse(solutionId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erreur lors de la récupération de la dernière réponse");
        }
    }
}