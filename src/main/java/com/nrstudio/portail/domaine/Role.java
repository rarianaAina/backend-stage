package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "role", schema = "dbo")
@Getter
@Setter
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;  // CLIENT, CONSULTANT, ADMIN

    @Column(nullable = false, length = 150)
    private String libelle;


}