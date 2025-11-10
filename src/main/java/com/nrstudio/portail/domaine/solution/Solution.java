package com.nrstudio.portail.domaine.solution;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solutions")
public class Solution {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "id_externe_crm", unique = true)
    private String idExterneCrm;
    
    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String zone;
    private String statut;
    private String etape;
    private String reference;
    private String secteur;
    
    private boolean cloture;
    private boolean supprime;
    
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
    
    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;
    
    @Column(name = "date_externalisation")
    private LocalDateTime dateExternalisation;
    
    @Column(name = "date_synchronisation")
    private LocalDateTime dateSynchronisation;
    
    @Column(name = "cree_par")
    private Integer creePar;
    
    @Column(name = "mis_a_jour_par")
    private Integer misAJourPar;
    
    @Column(name = "utilisateur_attribue")
    private Integer utilisateurAttribue;
    
    @Column(name = "workflow_id")
    private Integer workflowId;
    
    @Column(name = "canal_id")
    private Integer canalId;
    
    @Column(name = "cle_externe_talend")
    private String cleExterneTalend;
    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;
    
    // Getters et Setters pour le nouveau champ
    public LocalDateTime getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDateTime dateCloture) {
        this.dateCloture = dateCloture;
    }
    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdExterneCrm() {
        return idExterneCrm;
    }

    public void setIdExterneCrm(String idExterneCrm) {
        this.idExterneCrm = idExterneCrm;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getEtape() {
        return etape;
    }

    public void setEtape(String etape) {
        this.etape = etape;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSecteur() {
        return secteur;
    }

    public void setSecteur(String secteur) {
        this.secteur = secteur;
    }

    public boolean isCloture() {
        return cloture;
    }

    public void setCloture(boolean cloture) {
        this.cloture = cloture;
    }

    public boolean isSupprime() {
        return supprime;
    }

    public void setSupprime(boolean supprime) {
        this.supprime = supprime;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }

    public LocalDateTime getDateExternalisation() {
        return dateExternalisation;
    }

    public void setDateExternalisation(LocalDateTime dateExternalisation) {
        this.dateExternalisation = dateExternalisation;
    }

    public LocalDateTime getDateSynchronisation() {
        return dateSynchronisation;
    }

    public void setDateSynchronisation(LocalDateTime dateSynchronisation) {
        this.dateSynchronisation = dateSynchronisation;
    }

    public Integer getCreePar() {
        return creePar;
    }

    public void setCreePar(Integer creePar) {
        this.creePar = creePar;
    }

    public Integer getMisAJourPar() {
        return misAJourPar;
    }

    public void setMisAJourPar(Integer misAJourPar) {
        this.misAJourPar = misAJourPar;
    }

    public Integer getUtilisateurAttribue() {
        return utilisateurAttribue;
    }

    public void setUtilisateurAttribue(Integer utilisateurAttribue) {
        this.utilisateurAttribue = utilisateurAttribue;
    }

    public Integer getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Integer workflowId) {
        this.workflowId = workflowId;
    }

    public Integer getCanalId() {
        return canalId;
    }

    public void setCanalId(Integer canalId) {
        this.canalId = canalId;
    }

    public String getCleExterneTalend() {
        return cleExterneTalend;
    }

    public void setCleExterneTalend(String cleExterneTalend) {
        this.cleExterneTalend = cleExterneTalend;
    }

    // Méthodes utilitaires
    @Override
    public String toString() {
        return "Solution{" +
                "id=" + id +
                ", idExterneCrm='" + idExterneCrm + '\'' +
                ", titre='" + titre + '\'' +
                ", zone='" + zone + '\'' +
                ", statut='" + statut + '\'' +
                ", cloture=" + cloture +
                ", supprime=" + supprime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return id != null && id.equals(solution.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}