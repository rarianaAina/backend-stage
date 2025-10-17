package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "client", schema = "dbo")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Client {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name="company_id", nullable=false)
  private Integer companyId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="company_id", insertable=false, updatable=false)
  private Company company;

  @Column(name="id_externe_crm", length=100)
  private String idExterneCrm;

  @Column(nullable=false, length=150)
  private String nom;

  @Column(length=150)
  private String prenom;

  @Column(length=320)
  private String email;

  @Column(length=50)
  private String telephone;

  @Column(name="whatsapp_numero", length=50)
  private String whatsappNumero;

  @Column(length=150)
  private String fonction;

  @Column(nullable=false)
  private boolean principal = false;

  @Column(nullable=false)
  private boolean actif = true;

  @Column(name="date_creation", nullable=false)
  private LocalDateTime dateCreation;

  @Column(name="date_mise_a_jour", nullable=false)
  private LocalDateTime dateMiseAJour;
}
