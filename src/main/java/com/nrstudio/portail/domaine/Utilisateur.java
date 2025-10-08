package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.OffsetDateTime;

@Entity @Table(name = "utilisateur", schema = "dbo")
@Getter @Setter
public class Utilisateur {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable=false, unique=true, length=150)
  private String identifiant;

  private String nom;
  private String prenom;
  private String telephone;

  @Column(length=320) private String email;
  @Column(nullable=false) private boolean actif = true;

  @Lob
  @Column(name="mot_de_passe_hash")
  private byte[] motDePasseHash;

  @Column(name="id_externe_crm", unique=true)
  private String idExterneCrm;

  @Column(name="date_mise_a_jour")
  private OffsetDateTime dateMiseAJour;

  @Column(name="type_compte", length=50)
  private String typeCompte;

  @Column(name="company_id")
  private Integer companyId;

  @Column(name="company_nom", length=255)
  private String companyNom;

  @Column(name="role", length=50)
  private String role;
}