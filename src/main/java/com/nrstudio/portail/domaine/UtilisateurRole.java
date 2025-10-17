package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@IdClass(UtilisateurRoleId.class)
@Table(name = "utilisateur_role", schema = "dbo")
public class UtilisateurRole {
    @Id
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}