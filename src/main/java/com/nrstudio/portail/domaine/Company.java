package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "company", schema = "dbo")
@Getter
@Setter
public class Company {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name="id_externe_crm", length=100)
  private String idExterneCrm;

  @Column(name="code_company", length=100)
  private String codeCompany;

  @Column(nullable=false, length=250)
  private String nom;

  @Column(length=100)
  private String nif;

  @Column(length=100)
  private String stat;

  @Column(length=500)
  private String adresse;

  @Column(length=50)
  private String telephone;

  @Column(name="whatsapp_numero", length=50)
  private String whatsappNumero;

  @Column(length=320)
  private String email;

  @Column(nullable=false)
  private boolean actif = true;

  @Column(name="date_creation", nullable=false)
  private LocalDateTime dateCreation;

  @Column(name="date_mise_a_jour", nullable=false)
  private LocalDateTime dateMiseAJour;
}
