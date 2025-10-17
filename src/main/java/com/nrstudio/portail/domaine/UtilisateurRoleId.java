package com.nrstudio.portail.domaine;

import java.io.Serializable;
import java.util.Objects;

public class UtilisateurRoleId implements Serializable {
    private Integer utilisateur;
    private Integer role;

    public UtilisateurRoleId() {}

    public UtilisateurRoleId(Integer utilisateur, Integer role) {
        this.utilisateur = utilisateur;
        this.role = role;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UtilisateurRoleId)) return false;
        UtilisateurRoleId that = (UtilisateurRoleId) o;
        return Objects.equals(utilisateur, that.utilisateur) &&
               Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utilisateur, role);
    }

    // getters & setters
    public Integer getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Integer utilisateur) { this.utilisateur = utilisateur; }
    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }
}