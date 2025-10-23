package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_horaire", schema = "dbo")
@Getter
@Setter
public class CreditHoraire {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "company_id", nullable = false)
  private Integer companyId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", insertable = false, updatable = false)
  private Company company;

  @Column(name = "produit_id")
  private Integer produitId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "produit_id", insertable = false, updatable = false)
  private Produit produit;

  @Column(name = "periode_debut", nullable = false)
  private LocalDate periodeDebut;

  @Column(name = "periode_fin", nullable = false)
  private LocalDate periodeFin;

  @Column(name = "heures_allouees", nullable = false)
  private Integer heuresAllouees;

  @Column(name = "heures_consommees", nullable = false)
  private Integer heuresConsommees = 0;

  @Column(name = "heures_restantes", nullable = false)
  private Integer heuresRestantes;

  @Column(nullable = false)
  private boolean actif = true;

  @Column(name = "renouvelable")
  private Boolean renouvelable = false;

  @Column(length = 1000)
  private String remarques;

  @Column(name = "date_creation", nullable = false)
  private LocalDateTime dateCreation;

  @Column(name = "date_mise_a_jour", nullable = false)
  private LocalDateTime dateMiseAJour;

  @PrePersist
  protected void onCreate() {
    dateCreation = LocalDateTime.now();
    dateMiseAJour = LocalDateTime.now();
    if (heuresRestantes == null) {
      heuresRestantes = heuresAllouees - heuresConsommees;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    dateMiseAJour = LocalDateTime.now();
    heuresRestantes = heuresAllouees - heuresConsommees;
  }
}
