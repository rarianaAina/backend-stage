package com.nrstudio.portail.domaine;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "piece_jointe", schema = "dbo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nom_fichier", length = 255, nullable = false)
    private String nomFichier;

    @Column(name = "url_contenu", length = 500)
    private String urlContenu;

    @Column(name = "chemin_fichier", length = 500)
    private String cheminFichier;

    @Column(name = "type_mime", length = 100)
    private String typeMime;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    @Column(name = "ajoute_par_utilisateur_id", nullable = false)
    private Integer ajouteParUtilisateurId;

    @Column(name = "ticket_id")
    private Integer ticketId;

    @Column(name = "intervention_id")
    private Integer interventionId;

    @Column(name = "interaction_id")
    private Integer interactionId;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout;

    // Constructeurs
    public PieceJointe() {}

    public PieceJointe(String nomFichier, String urlContenu, String typeMime, Long tailleOctets, 
                      Integer ajouteParUtilisateurId, Integer ticketId) {
        this.nomFichier = nomFichier;
        this.urlContenu = urlContenu;
        this.typeMime = typeMime;
        this.tailleOctets = tailleOctets;
        this.ajouteParUtilisateurId = ajouteParUtilisateurId;
        this.ticketId = ticketId;
        this.dateAjout = LocalDateTime.now();
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getUrlContenu() {
        return urlContenu;
    }

    public void setUrlContenu(String urlContenu) {
        this.urlContenu = urlContenu;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getTypeMime() {
        return typeMime;
    }

    public void setTypeMime(String typeMime) {
        this.typeMime = typeMime;
    }

    public Long getTailleOctets() {
        return tailleOctets;
    }

    public void setTailleOctets(Long tailleOctets) {
        this.tailleOctets = tailleOctets;
    }

    public Integer getAjouteParUtilisateurId() {
        return ajouteParUtilisateurId;
    }

    public void setAjouteParUtilisateurId(Integer ajouteParUtilisateurId) {
        this.ajouteParUtilisateurId = ajouteParUtilisateurId;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getInterventionId() {
        return interventionId;
    }

    public void setInterventionId(Integer interventionId) {
        this.interventionId = interventionId;
    }

    public Integer getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Integer interactionId) {
        this.interactionId = interactionId;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    // Méthode utilitaire pour obtenir l'extension du fichier
    public String getExtension() {
        if (nomFichier == null || !nomFichier.contains(".")) {
            return "";
        }
        return nomFichier.substring(nomFichier.lastIndexOf(".") + 1).toLowerCase();
    }

    // Méthode utilitaire pour formater la taille
    public String getTailleFormatee() {
        if (tailleOctets == null) return "0 octets";
        
        if (tailleOctets < 1024) {
            return tailleOctets + " octets";
        } else if (tailleOctets < 1024 * 1024) {
            return String.format("%.1f Ko", tailleOctets / 1024.0);
        } else {
            return String.format("%.1f Mo", tailleOctets / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "PieceJointe{" +
                "id=" + id +
                ", nomFichier='" + nomFichier + '\'' +
                ", typeMime='" + typeMime + '\'' +
                ", tailleOctets=" + tailleOctets +
                ", ticketId=" + ticketId +
                ", dateAjout=" + dateAjout +
                '}';
    }
}