package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "utilisateur", schema = "dbo")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Champs de Company (ajoutés depuis Client)
    @Column(name = "company_id")
    private Integer companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    // Champs CRM
    @Column(name = "id_externe_crm", length = 100)
    private String idExterneCrm;

    // Champs d'authentification
    @Column(nullable = false, unique = true, length = 150)
    private String identifiant;

    @Column(name = "mot_de_passe_hash")
    private byte[] motDePasseHash;

    @Column(name = "mot_de_passe_salt")
    private byte[] motDePasseSalt;

    // Champs personnels
    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 150)
    private String prenom;

    @Column(length = 320)
    private String email;

    @Column(length = 50)
    private String telephone;

    @Column(name = "whatsapp_numero", length = 50)
    private String whatsappNumero;

    // Statut
    @Column(nullable = false)
    private boolean actif = true;

    // Dates
    @Column(name = "date_derniere_connexion")
    private LocalDateTime dateDerniereConnexion;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_mise_a_jour", nullable = false)
    private LocalDateTime dateMiseAJour;

    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private Set<UtilisateurRole> roles;
}