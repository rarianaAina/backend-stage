package com.nrstudio.portail.services.solution;

import com.nrstudio.portail.dto.solution.*;
import com.nrstudio.portail.domaine.solution.ReponseSolution;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.domaine.solution.SolutionTicket;
import com.nrstudio.portail.depots.solution.SolutionTicketRepository;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.depots.solution.SolutionReponseRepository;
import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.services.NotificationWorkflowService;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReponseSolutionService {
    
    @Autowired
    private SolutionReponseRepository reponseSolutionRepository;
    
    @Autowired
    private SolutionRepository solutionRepository;
    
    @Autowired
    private SolutionTicketRepository solutionTicketRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    NotificationWorkflowService notificationWorkflowService;

    @Autowired
    TicketRepository ticketRepository;
    
    @Transactional
    public ReponseSolutionDTO creerReponse(CreateReponseSolutionRequest request) {

        Integer utilisateurId = request.getUtilisateurId();

        // Vérifier que la solution existe
        Solution solution = solutionRepository.findById(request.getSolutionId())
                .orElseThrow(() -> new RuntimeException("Solution non trouvée avec l'ID: " + request.getSolutionId()));

        // Vérifier que l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + utilisateurId));

        // Validation simple
        if (request.getEstValide() == null) {
            throw new RuntimeException("Le champ estValide est obligatoire");
        }

        if (!request.getEstValide() &&
            (request.getCommentaire() == null || request.getCommentaire().trim().isEmpty())) {
            throw new RuntimeException("Un commentaire est obligatoire pour une solution rejetée");
        }

        // Création de la réponse
        ReponseSolution reponse = new ReponseSolution();
        reponse.setSolution(solution);
        reponse.setEstValide(request.getEstValide());
        reponse.setCommentaire(request.getCommentaire());
        reponse.setCreePar(utilisateur);
        reponse.setDateReponse(LocalDateTime.now());

        ReponseSolution savedReponse = reponseSolutionRepository.save(reponse);

        // MAJ du statut
        mettreAJourStatutSolution(solution, request.getEstValide());

        // 🔥 Récupérer le ticket lié via la table solution_ticket
        SolutionTicket liaison = solutionTicketRepository.findBySolution(solution)
                .orElseThrow(() -> new RuntimeException(
                    "Aucune liaison solution-ticket trouvée pour la solution ID: " + solution.getId()
                ));

        Ticket ticket = liaison.getTicket();

        // 🔥 Appel du workflow de notification
        notificationWorkflowService.executerWorkflowNotification(
                "CREATION_REPONSE_SOLUTION",
                ticket
        );

        return convertirEnDTO(savedReponse);
    }

    
    private void mettreAJourStatutSolution(Solution solution, Boolean estValide) {
        if (estValide) {
            // Si solution validée, on peut la marquer comme approuvée
            solution.setStatut("Approved");
            solution.setCloture(true);
            solution.setDateCloture(LocalDateTime.now());
        } else {
            // Si solution rejetée, on peut la remettre en draft ou autre statut
            solution.setStatut("Rejected");
        }
        solution.setDateMiseAJour(LocalDateTime.now());
        solutionRepository.save(solution);
    }
    
    public List<ReponseSolutionDTO> getReponsesParSolution(Integer solutionId) {
        List<ReponseSolution> reponses = reponseSolutionRepository.findBySolutionIdOrderByDateReponseDesc(solutionId);
        return reponses.stream()
                .map(this::convertirEnDTO)
                .collect(Collectors.toList());
    }
    
    public StatistiquesReponseDTO getStatistiquesSolution(Integer solutionId) {
        // Vérifier que la solution existe
        if (!solutionRepository.existsById(solutionId)) {
            throw new RuntimeException("Solution non trouvée avec l'ID: " + solutionId);
        }
        
        Long totalReponses = reponseSolutionRepository.countBySolutionIdAndEstValide(solutionId, true) +
                           reponseSolutionRepository.countBySolutionIdAndEstValide(solutionId, false);
        
        Long reponsesValides = reponseSolutionRepository.countBySolutionIdAndEstValide(solutionId, true);
        Long reponsesRejetees = reponseSolutionRepository.countBySolutionIdAndEstValide(solutionId, false);
        
        Double tauxValidation = totalReponses > 0 ? (reponsesValides.doubleValue() / totalReponses.doubleValue()) * 100 : 0.0;
        
        StatistiquesReponseDTO stats = new StatistiquesReponseDTO();
        stats.setSolutionId(solutionId);
        stats.setTotalReponses(totalReponses);
        stats.setReponsesValides(reponsesValides);
        stats.setReponsesRejetees(reponsesRejetees);
        stats.setTauxValidation(tauxValidation);
        
        return stats;
    }
    
    public Optional<ReponseSolutionDTO> getDerniereReponse(Integer solutionId) {
        return reponseSolutionRepository.findLatestBySolutionId(solutionId)
                .map(this::convertirEnDTO);
    }
    
    private ReponseSolutionDTO convertirEnDTO(ReponseSolution reponse) {
        ReponseSolutionDTO dto = new ReponseSolutionDTO();
        dto.setId(reponse.getId());
        dto.setSolutionId(reponse.getSolution().getId());
        dto.setEstValide(reponse.getEstValide());
        dto.setCommentaire(reponse.getCommentaire());
        dto.setDateReponse(reponse.getDateReponse());
        dto.setCreeParId(reponse.getCreePar().getId());
        dto.setCreeParNom(reponse.getCreePar().getNom() + " " + reponse.getCreePar().getPrenom());
        return dto;
    }

    
}