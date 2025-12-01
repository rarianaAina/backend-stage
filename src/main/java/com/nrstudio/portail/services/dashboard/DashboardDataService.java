package com.nrstudio.portail.services.dashboard;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.domaine.*;
import com.nrstudio.portail.dto.*;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardDataService {

    private final TicketRepository ticketRepository;
    private final InterventionRepository interventionRepository;
    private final CompanyRepository companyRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;

    public List<TicketRecentDto> getTicketsRecents(List<Ticket> tickets, int limit) {
        return tickets.stream()
            .sorted((t1, t2) -> t2.getDateCreation().compareTo(t1.getDateCreation()))
            .limit(limit)
            .map(this::toTicketRecentDto)
            .collect(Collectors.toList());
    }

    public List<InterventionProchaine> getInterventionsProchaines(Integer companyId, int limit) {
        // Optimisation: récupérer toutes les interventions en une seule requête
        List<Intervention> interventions = interventionRepository.findByDateInterventionAfter(LocalDateTime.now());
        
        // Précharger les tickets concernés
        Set<Integer> ticketIds = interventions.stream()
            .map(Intervention::getTicketId)
            .collect(Collectors.toSet());
            
        Map<Integer, Ticket> ticketsMap = ticketRepository.findAllById(ticketIds).stream()
            .collect(Collectors.toMap(Ticket::getId, t -> t));

        return interventions.stream()
            .filter(i -> {
                Ticket t = ticketsMap.get(i.getTicketId());
                return t != null && companyId.equals(t.getCompanyId());
            })
            .sorted((i1, i2) -> i1.getDateIntervention().compareTo(i2.getDateIntervention()))
            .limit(limit)
            .map(i -> toInterventionProchaine(i, ticketsMap.get(i.getTicketId())))
            .collect(Collectors.toList());
    }

    public Integer getCompanyIdForUser(String idExterneCrm) {
        return utilisateurRepository.findByIdExterneCrm(idExterneCrm)
            .map(Utilisateur::getCompanyId)
            .orElse(null);
    }

    private TicketRecentDto toTicketRecentDto(Ticket ticket) {
        TicketRecentDto dto = new TicketRecentDto();
        dto.setId(ticket.getId());
        dto.setReference(ticket.getReference());
        dto.setTitre(ticket.getTitre());
        dto.setStatut(getStatutLabel(ticket.getStatutTicketId()));
        dto.setPriorite(getPrioriteLabel(ticket.getPrioriteTicketId()));
        dto.setNomCompany(getCompanyNom(ticket.getCompanyId()));
        dto.setNomProduit(getProduitLibelle(ticket.getProduitId()));
        dto.setConsultantNom(getConsultantNom(ticket.getAffecteAUtilisateurId()));
        dto.setDateCreation(ticket.getDateCreation());
        return dto;
    }

    private InterventionProchaine toInterventionProchaine(Intervention intervention, Ticket ticket) {
        InterventionProchaine dto = new InterventionProchaine();
        dto.setId(intervention.getId());
        dto.setReference(intervention.getReference());
        
        if (ticket != null) {
            dto.setTicketReference(ticket.getReference());
            dto.setTicketTitre(ticket.getTitre());
        }
        
        dto.setDateIntervention(intervention.getDateIntervention());
        dto.setTypeIntervention(intervention.getTypeIntervention());
        dto.setStatut(getStatutInterventionLabel(intervention.getStatutInterventionId()));
        dto.setConsultantNom(getConsultantNom(intervention.getCreeParUtilisateurId()));
        
        return dto;
    }

    // Cache simple pour éviter les appels répétitifs
    private final Map<Integer, String> companyCache = new HashMap<>();
    private final Map<Integer, String> produitCache = new HashMap<>();
    private final Map<Integer, String> consultantCache = new HashMap<>();

    private String getCompanyNom(Integer companyId) {
        if (companyId == null) return "";
        return companyCache.computeIfAbsent(companyId, id -> 
            companyRepository.findById(id)
                .map(Company::getNom)
                .orElse("")
        );
    }

    private String getProduitLibelle(Integer produitId) {
        if (produitId == null) return "";
        return produitCache.computeIfAbsent(produitId, id -> 
            produitRepository.findById(id)
                .map(Produit::getLibelle)
                .orElse("")
        );
    }

    private String getConsultantNom(Integer consultantId) {
        if (consultantId == null) return "";
        return consultantCache.computeIfAbsent(consultantId, id -> 
            utilisateurRepository.findById(id)
                .map(u -> u.getNom() + " " + u.getPrenom())
                .orElse("")
        );
    }

    private String getStatutLabel(Integer statutId) {
        if (statutId == null) return "";
        switch (statutId) {
            case 1: return "Ouvert";
            case 2: return "En cours";
            case 3: return "En attente";
            case 4: return "En attente client";
            case 5: return "Planifié";
            case 6: return "Résolu";
            case 7: return "Cloturé";
            default: return "Inconnu";
        }
    }

    private String getPrioriteLabel(Integer prioriteId) {
        if (prioriteId == null) return "";
        switch (prioriteId) {
            case 1: return "Basse";
            case 2: return "Normale";
            case 3: return "Haute";
            case 4: return "Urgente";
            default: return "Inconnu";
        }
    }

    private String getStatutInterventionLabel(Integer statutId) {
        if (statutId == null) return "";
        switch (statutId) {
            case 1: return "Proposée";
            case 2: return "Planifiée";
            case 3: return "En cours";
            case 4: return "À valider";
            case 5: return "Refusée";
            case 6: return "Clôturée";
            default: return "Inconnu";
        }
    }
}