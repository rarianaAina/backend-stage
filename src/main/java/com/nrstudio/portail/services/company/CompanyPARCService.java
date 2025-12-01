package com.nrstudio.portail.services.company;

import com.nrstudio.portail.depots.CompanyPARCRepository;
import com.nrstudio.portail.dto.companyparc.InterlocuteurDto;
import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.CompanyPARC;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.dto.companyparc.CompanyPARCDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyPARCService {

    private final CompanyPARCRepository companyPARCRepository;
    private final CompanyRepository companyRepository;
    private final UtilisateurInterneRepository utilisateurInterneRepository;

    @Autowired
    public CompanyPARCService(CompanyPARCRepository companyPARCRepository, 
                            CompanyRepository companyRepository,
                            UtilisateurInterneRepository utilisateurInterneRepository) {
        this.companyPARCRepository = companyPARCRepository;
        this.companyRepository = companyRepository;
        this.utilisateurInterneRepository = utilisateurInterneRepository;
    }

    // Méthode existante retournant les entités
    public List<CompanyPARC> getParcsByCompanyId(Integer companyId) {
        Optional<Company> company = companyRepository.findById(companyId);
        
        if (company.isPresent()) {
            String idExterneCrm = company.get().getIdExterneCrm();
            int idExterneCrmInt = Integer.parseInt(idExterneCrm);
            System.out.println("Recherche des PARCs pour la company avec idExterneCrm: " + idExterneCrmInt);
            return companyPARCRepository.findByParcCompanyId(idExterneCrmInt);
        } else {
            throw new RuntimeException("Company non trouvée avec l'ID: " + companyId);
        }
    }

    // NOUVELLE MÉTHODE : Retourne les DTOs avec le nom complet de l'utilisateur
    public List<CompanyPARCDto> getParcsDtosByCompanyId(Integer companyId) {
        List<CompanyPARC> parcs = getParcsByCompanyId(companyId);
        
        return parcs.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Méthode pour convertir CompanyPARC en CompanyPARCDto
    private CompanyPARCDto convertToDto(CompanyPARC parc) {
        String userFullName = null;
        if (parc.getUserId() != null) {
            // Option 1: Récupérer seulement le nom complet (plus efficace)
            userFullName = utilisateurInterneRepository.findUserFullNameByIdExterneCrm(parc.getUserId())
                .orElse("Utilisateur inconnu");
            
            // Option 2: Récupérer l'utilisateur complet si besoin d'autres informations
            /*
            Optional<UtilisateurInterne> user = utilisateurInterneRepository.findById(parc.getUserId());
            userFullName = user.map(u -> 
                (u.getPrenom() != null ? u.getPrenom() + " " : "") + u.getNom()
            ).orElse("Utilisateur inconnu");
            */
        }
        
        return new CompanyPARCDto(
            parc.getParcId(),
            parc.getParcName(),
            parc.getParcCompanyId(),
            parc.getCompCompanyId(),
            parc.getCompName(),
            parc.getDateObtention(),
            parc.getUserId(),
            userFullName
        );
    }

    public Integer getUserIdByParcId(Integer parcId) {
        Optional<CompanyPARC> companyPARC = companyPARCRepository.findById(parcId);
        return companyPARC.map(CompanyPARC::getUserId).orElse(null);
    }

    // NOUVELLE MÉTHODE : Récupérer le nom complet de l'utilisateur par parcId
    public String getUserFullNameByParcId(Integer parcId) {
        Optional<CompanyPARC> companyPARC = companyPARCRepository.findById(parcId);
        if (companyPARC.isPresent() && companyPARC.get().getUserId() != null) {
            return utilisateurInterneRepository.findUserFullNameByIdExterneCrm(companyPARC.get().getUserId())
                .orElse("Utilisateur inconnu");
        }
        return null;
    }

    // NOUVELLE MÉTHODE : Récupérer les détails complets de l'utilisateur par parcId
    public UtilisateurInterne getUtilisateurInterneByParcId(Integer parcId) {
        Optional<CompanyPARC> companyPARC = companyPARCRepository.findById(parcId);
        if (companyPARC.isPresent() && companyPARC.get().getUserId() != null) {
            return utilisateurInterneRepository.findById(companyPARC.get().getUserId()).orElse(null);
        }
        return null;
    }

    // NOUVELLE MÉTHODE : Récupérer tous les interlocuteurs distincts pour une company
    public List<InterlocuteurDto> getInterlocuteursByCompanyId(String companyId) {
        // Convertir companyId en idExterneCrm si nécessaire
        Optional<Company> company = companyRepository.findById(Integer.parseInt(companyId));
        
        if (company.isPresent()) {
            String idExterneCrm = company.get().getIdExterneCrm();
            int compCompanyId = Integer.parseInt(idExterneCrm);
            System.out.println("Comp idexterne " + compCompanyId);
            
            // Récupérer tous les userId distincts pour ce comp_companyid
            List<Integer> userIds = companyPARCRepository.findDistinctUserIdsByCompCompanyId(compCompanyId);
            
            // Convertir en DTOs avec les informations complètes
            return userIds.stream()
                    .map(userId -> {
                        // Récupérer l'utilisateur complet pour avoir l'email
                        Optional<UtilisateurInterne> utilisateur = utilisateurInterneRepository.findByIdExterneCrm(userId.toString());
                        
                        if (utilisateur.isPresent()) {
                            UtilisateurInterne user = utilisateur.get();
                            String fullName = (user.getPrenom() != null ? user.getPrenom() + " " : "") + user.getNom();
                            System.out.println("Interlocuteur trouvé - ID: " + userId + ", Nom: " + fullName + ", Email: " + user.getEmail());
                            
                            return new InterlocuteurDto(
                                userId, 
                                fullName, 
                                user.getEmail(),
                                user.getTelephone(),
                                user.getWhatsappNumero()
                            );
                        } else {
                            System.out.println("Utilisateur non trouvé pour l'ID: " + userId);
                            return new InterlocuteurDto(userId, "Utilisateur inconnu", null, null, null);
                        }
                    })
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Company non trouvée avec l'ID: " + companyId);
        }
    }
}