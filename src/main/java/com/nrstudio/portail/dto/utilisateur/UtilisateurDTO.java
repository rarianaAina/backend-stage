package com.nrstudio.portail.dto.utilisateur;

import com.nrstudio.portail.domaine.Utilisateur;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UtilisateurDTO {
    private Integer id;
    private Integer companyId;
    private String companyName;
    private String idExterneCrm;
    private String identifiant;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String whatsappNumero;
    private boolean actif;
    private LocalDateTime dateDerniereConnexion;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    
    public UtilisateurDTO(Utilisateur utilisateur) {
        this.id = utilisateur.getId();
        this.companyId = utilisateur.getCompanyId();
        this.idExterneCrm = utilisateur.getIdExterneCrm();
        this.identifiant = utilisateur.getIdentifiant();
        this.nom = utilisateur.getNom();
        this.prenom = utilisateur.getPrenom();
        this.email = utilisateur.getEmail();
        this.telephone = utilisateur.getTelephone();
        this.whatsappNumero = utilisateur.getWhatsappNumero();
        this.actif = utilisateur.isActif();
        this.dateDerniereConnexion = utilisateur.getDateDerniereConnexion();
        this.dateCreation = utilisateur.getDateCreation();
        this.dateMiseAJour = utilisateur.getDateMiseAJour();
        if (utilisateur.getCompany() != null) {
            this.companyName = utilisateur.getCompany().getNom();
        }
    }
}