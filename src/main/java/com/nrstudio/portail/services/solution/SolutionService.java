package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.depots.solution.SolutionTicketRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.dto.solution.SolutionDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolutionService {
    
    private final SolutionTicketRepository solutionTicketRepository;
    
    public SolutionService(SolutionTicketRepository solutionTicketRepository) {
        this.solutionTicketRepository = solutionTicketRepository;
    }
    
    /**
     * Récupère toutes les solutions rattachées à un ticket via la table de liaison
     */
    public List<SolutionDTO> getSolutionsByTicketId(Integer ticketId) {
        System.out.println("🔍 Service: Recherche des solutions pour ticket ID: " + ticketId);
        
        // Récupérer les solutions via la table de liaison
        List<Solution> solutions = solutionTicketRepository.findSolutionsByTicketId(ticketId);
        
        System.out.println("📊 Service: " + solutions.size() + " solutions trouvées en base");
        
        // Convertir en DTO
        return solutions.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Récupère une solution par son ID
     */
    public SolutionDTO getSolutionById(Integer solutionId) {
        // Implémentation à adapter selon votre repository Solution
        // Pour l'instant, retourne un DTO vide
        System.out.println("🔍 Récupération de la solution ID: " + solutionId);
        return new SolutionDTO(); // À implémenter
    }
    
    /**
     * Convertit une entité Solution en DTO
     */
    private SolutionDTO convertToDTO(Solution solution) {
        SolutionDTO dto = new SolutionDTO();
        dto.setId(solution.getId());
        dto.setTitre(solution.getTitre());
        dto.setDescription(solution.getDescription());
        dto.setZone(solution.getZone());
        dto.setStatut(solution.getStatut());
        dto.setEtape(solution.getEtape());
        dto.setReference(solution.getReference());
        dto.setDateCreation(solution.getDateCreation());
        dto.setDateMiseAJour(solution.getDateMiseAJour());
        dto.setDateCloture(solution.getDateCloture());
        dto.setCloture(solution.isCloture());
        return dto;
    }
}