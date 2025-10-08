package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "produit", schema = "dbo")
@Getter
@Setter
public class Produit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name="id_externe_crm", length=100)
  private String idExterneCrm;

  @Column(name="code_produit", length=100)
  private String codeProduit;

  @Column(nullable=false, length=250)
  private String libelle;

  @Lob
  private String description;

  @Column(nullable=false)
  private boolean actif = true;

  @Column(name="date_creation", nullable=false)
  private LocalDateTime dateCreation;

  @Column(name="date_mise_a_jour", nullable=false)
  private LocalDateTime dateMiseAJour;
}
