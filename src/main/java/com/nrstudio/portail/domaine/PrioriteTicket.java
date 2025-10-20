package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "priorite_ticket", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PrioriteTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;
    
    @Column(name = "niveau", nullable = false)
    private Integer niveau;
    
}
