package com.nrstudio.portail.domaine;

import jakarta.persistence.*;

@Entity
@Table(name = "type_interaction")
public class TypeInteraction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;

    // Constructeurs
    public TypeInteraction() {}

    public TypeInteraction(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return "TypeInteraction{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", libelle='" + libelle + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TypeInteraction that = (TypeInteraction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}