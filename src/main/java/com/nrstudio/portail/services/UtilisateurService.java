package com.nrstudio.portail.services;

import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UtilisateurService {
    private final UtilisateurRepository repo;
    
    public UtilisateurService(UtilisateurRepository repo) { 
        this.repo = repo; 
    }

    // Ancienne méthode (gardée pour compatibilité)
    public List<Utilisateur> lister() { 
        return repo.findAll(); 
    }

    // NOUVELLE méthode paginée avec filtres
    public UtilisateurPageReponse listerUtilisateursAvecPaginationEtFiltres(
        int page,
        int size,
        String recherche,
        String actifStr,
        String dateDebut,
        String dateFin) {

        Stream<Utilisateur> utilisateurStream = repo.findAll().stream();

        // Filtre par recherche (nom, prénom, email, identifiant)
        if (recherche != null && !recherche.isEmpty()) {
            String rechercheLower = recherche.toLowerCase();
            utilisateurStream = utilisateurStream.filter(utilisateur ->
                (utilisateur.getNom() != null && utilisateur.getNom().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getPrenom() != null && utilisateur.getPrenom().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getEmail() != null && utilisateur.getEmail().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getIdentifiant() != null && utilisateur.getIdentifiant().toLowerCase().contains(rechercheLower))
            );
        }

        // Filtre par statut actif/inactif
        if (actifStr != null && !actifStr.isEmpty()) {
            Boolean actif = Boolean.valueOf(actifStr);
            utilisateurStream = utilisateurStream.filter(utilisateur -> 
                utilisateur.isActif() == actif
            );
        }

        // Filtre par date de création (début)
        if (dateDebut != null && !dateDebut.isEmpty()) {
            LocalDate debut = LocalDate.parse(dateDebut);
            utilisateurStream = utilisateurStream.filter(utilisateur -> {
                if (utilisateur.getDateCreation() == null) return false;
                return !utilisateur.getDateCreation().toLocalDate().isBefore(debut);
            });
        }

        // Filtre par date de création (fin)
        if (dateFin != null && !dateFin.isEmpty()) {
            LocalDate fin = LocalDate.parse(dateFin);
            utilisateurStream = utilisateurStream.filter(utilisateur -> {
                if (utilisateur.getDateCreation() == null) return false;
                return !utilisateur.getDateCreation().toLocalDate().isAfter(fin);
            });
        }

        // Appliquer la pagination
        List<Utilisateur> utilisateurs = utilisateurStream
            .skip(page * size)
            .limit(size)
            .toList();

        // Compter le total des éléments (pour la pagination)
        long totalElements = compterUtilisateursAvecFiltres(recherche, actifStr, dateDebut, dateFin);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new UtilisateurPageReponse(utilisateurs, page, totalPages, totalElements, size);
    }

    // Méthode pour compter le total avec les mêmes filtres
    private long compterUtilisateursAvecFiltres(
        String recherche,
        String actifStr,
        String dateDebut,
        String dateFin) {

        Stream<Utilisateur> utilisateurStream = repo.findAll().stream();

        // Appliquer les mêmes filtres que dans la méthode principale
        if (recherche != null && !recherche.isEmpty()) {
            String rechercheLower = recherche.toLowerCase();
            utilisateurStream = utilisateurStream.filter(utilisateur ->
                (utilisateur.getNom() != null && utilisateur.getNom().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getPrenom() != null && utilisateur.getPrenom().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getEmail() != null && utilisateur.getEmail().toLowerCase().contains(rechercheLower)) ||
                (utilisateur.getIdentifiant() != null && utilisateur.getIdentifiant().toLowerCase().contains(rechercheLower))
            );
        }

        if (actifStr != null && !actifStr.isEmpty()) {
            Boolean actif = Boolean.valueOf(actifStr);
            utilisateurStream = utilisateurStream.filter(utilisateur -> 
                utilisateur.isActif() == actif
            );
        }

        if (dateDebut != null && !dateDebut.isEmpty()) {
            LocalDate debut = LocalDate.parse(dateDebut);
            utilisateurStream = utilisateurStream.filter(utilisateur -> {
                if (utilisateur.getDateCreation() == null) return false;
                return !utilisateur.getDateCreation().toLocalDate().isBefore(debut);
            });
        }

        if (dateFin != null && !dateFin.isEmpty()) {
            LocalDate fin = LocalDate.parse(dateFin);
            utilisateurStream = utilisateurStream.filter(utilisateur -> {
                if (utilisateur.getDateCreation() == null) return false;
                return !utilisateur.getDateCreation().toLocalDate().isAfter(fin);
            });
        }

        return utilisateurStream.count();
    }

    public Utilisateur obtenir(Integer id) { 
        return repo.findById(id).orElseThrow(); 
    }

    @Transactional
    public Utilisateur creer(UtilisateurCreationRequete r) {
        Utilisateur u = new Utilisateur();
        u.setIdentifiant(r.getIdentifiant());
        u.setNom(r.getNom());
        u.setPrenom(r.getPrenom());
        u.setEmail(r.getEmail());
        u.setActif(Boolean.TRUE.equals(r.getActif()));
        u.setDateCreation(r.getDateCreation());
        u.setDateMiseAJour(r.getDateMiseAJour());
        
        if (r.getMotDePasse() != null && !r.getMotDePasse().isBlank()) {
            String hash = BCrypt.hashpw(r.getMotDePasse(), BCrypt.gensalt());
            u.setMotDePasseHash(hash.getBytes(StandardCharsets.UTF_8));
        }
        
        return repo.save(u);
    }

    @Transactional
    public Utilisateur mettreAJour(Integer id, UtilisateurMiseAJourRequete r) {
        Utilisateur u = obtenir(id);
        if (r.getNom() != null) u.setNom(r.getNom());
        if (r.getPrenom() != null) u.setPrenom(r.getPrenom());
        if (r.getEmail() != null) u.setEmail(r.getEmail());
        if (r.getActif() != null) u.setActif(r.getActif().booleanValue());
        u.setDateMiseAJour(java.time.LocalDateTime.now());
        return repo.save(u);
    }

    @Transactional
    public void definirMotDePasse(Integer id, MotDePasseRequete r) {
        Utilisateur u = obtenir(id);
        String hash = BCrypt.hashpw(r.getMotDePasse(), BCrypt.gensalt());
        u.setMotDePasseHash(hash.getBytes(StandardCharsets.UTF_8));
        u.setDateMiseAJour(java.time.LocalDateTime.now());
        repo.save(u);
    }

    public Optional<Utilisateur> trouverParIdentifiant(String identifiant) {
        return repo.findByIdentifiant(identifiant);
    }

    public Optional<Utilisateur> trouverParEmail(String email) {
        return repo.findByEmail(email);
    }
}