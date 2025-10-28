package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.InteractionRepository;
import com.nrstudio.portail.depots.TypeInteractionRepository;
import com.nrstudio.portail.depots.CanalInteractionRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.domaine.Interaction;
import com.nrstudio.portail.domaine.TypeInteraction;
import com.nrstudio.portail.domaine.CanalInteraction;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.dto.InteractionCreateDTO;
import com.nrstudio.portail.dto.InteractionDTO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin
public class InteractionControleur {

    private final InteractionRepository interactionRepo;
    private final TypeInteractionRepository typeInteractionRepo;
    private final CanalInteractionRepository canalInteractionRepo;
    private final UtilisateurRepository utilisateurRepo;

    public InteractionControleur(InteractionRepository interactionRepo,
                                TypeInteractionRepository typeInteractionRepo,
                                CanalInteractionRepository canalInteractionRepo,
                                UtilisateurRepository utilisateurRepo) {
        this.interactionRepo = interactionRepo;
        this.typeInteractionRepo = typeInteractionRepo;
        this.canalInteractionRepo = canalInteractionRepo;
        this.utilisateurRepo = utilisateurRepo;
    }

    @GetMapping
    public List<InteractionDTO> lister() {
        return interactionRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public InteractionDTO obtenir(@PathVariable("id") Integer id) {
        Interaction interaction = interactionRepo.findById(id).orElseThrow();
        return convertToDTO(interaction);
    }

    @GetMapping("/ticket/{ticketId}")
    public List<InteractionDTO> listerParTicket(@PathVariable("ticketId") Integer ticketId) {
        List<Interaction> interactions = interactionRepo.findByTicketIdOrderByDateCreationDesc(ticketId);
        System.out.println("Nombre d'interactions pour le ticket ID " + ticketId + ": " + interactions.size());
        
        List<InteractionDTO> dtos = interactions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        System.out.println("DTOs créés: " + dtos.size());
        return dtos;
    }

    @GetMapping("/intervention/{interventionId}")
    public List<InteractionDTO> listerParIntervention(@PathVariable("interventionId") Integer interventionId) {
        return interactionRepo.findByInterventionIdOrderByDateCreationDesc(interventionId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public InteractionDTO creer(@RequestBody InteractionCreateDTO dto) {
        System.out.println("Création d'une interaction avec les données: " + dto);
        // Validation des références
        TypeInteraction typeInteraction = typeInteractionRepo.findById(dto.getTypeInteractionId())
                .orElseThrow(() -> new RuntimeException("Type d'interaction non trouvé"));
        
        CanalInteraction canalInteraction = canalInteractionRepo.findById(dto.getCanalInteractionId())
                .orElseThrow(() -> new RuntimeException("Canal d'interaction non trouvé"));
        
        Utilisateur auteur = utilisateurRepo.findById(dto.getAuteurUtilisateurId())
                .orElseThrow(() -> new RuntimeException("Utilisateur auteur non trouvé"));

        // Création de l'interaction
        Interaction interaction = new Interaction();
        interaction.setTicketId(dto.getTicketId());
        interaction.setInterventionId(dto.getInterventionId());
        interaction.setMessage(dto.getMessage());
        interaction.setTypeInteractionId(dto.getTypeInteractionId());
        interaction.setCanalInteractionId(1);
        interaction.setAuteurUtilisateurId(dto.getAuteurUtilisateurId());
        interaction.setDateCreation(LocalDateTime.now());
        

        Interaction savedInteraction = interactionRepo.save(interaction);
        return convertToDTO(savedInteraction);
    }

    // Méthode utilitaire pour convertir Interaction en InteractionDTO avec jointures
    private InteractionDTO convertToDTO(Interaction interaction) {
        InteractionDTO dto = new InteractionDTO();
        dto.setId(interaction.getId());
        dto.setTicketId(interaction.getTicketId());
        dto.setInterventionId(interaction.getInterventionId());
        dto.setMessage(interaction.getMessage());
        dto.setDateCreation(interaction.getDateCreation());
        // dto.setVisibleClient(interaction.getVisibleClient());
        // Récupération du type d'interaction
        if (interaction.getTypeInteractionId() != null) {
            typeInteractionRepo.findById(interaction.getTypeInteractionId()).ifPresent(type -> {
                dto.setTypeInteractionId(type.getId());
                dto.setTypeInteractionLibelle(type.getLibelle());
                dto.setTypeInteractionCode(type.getCode());
            });
        }

        // Récupération du canal d'interaction
        if (interaction.getCanalInteractionId() != null) {
            canalInteractionRepo.findById(interaction.getCanalInteractionId()).ifPresent(canal -> {
                dto.setCanalInteractionId(canal.getId());
                dto.setCanalInteractionLibelle(canal.getLibelle());
                dto.setCanalInteractionCode(canal.getCode());
            });
        }

        // Récupération des informations de l'auteur
        if (interaction.getAuteurUtilisateurId() != null) {
            utilisateurRepo.findById(interaction.getAuteurUtilisateurId()).ifPresent(utilisateur -> {
                dto.setAuteurUtilisateurId(utilisateur.getId());
                dto.setAuteurNom(utilisateur.getNom());
                dto.setAuteurPrenom(utilisateur.getPrenom());
                dto.setAuteurEmail(utilisateur.getEmail());
            });
        }

        return dto;
    }
}