package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "type_ticket", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TypeTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", length = 100, nullable = false)
    private String code;

    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;

  
    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }


    public void setId(Integer id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }



    @Override
    public String toString() {
        return "PrioriteTicket{" +
               "id=" + id +
               ", code='" + code + '\'' +
               ", libelle='" + libelle + '\'' +
               '}';
    }
}

