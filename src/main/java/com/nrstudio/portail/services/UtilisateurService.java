package com.nrstudio.portail.services;

import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {
  private final UtilisateurRepository repo;
  public UtilisateurService(UtilisateurRepository repo) { this.repo = repo; }

  public List<Utilisateur> lister() { return repo.findAll(); }

  public Utilisateur obtenir(Integer id) { return repo.findById(id).orElseThrow(); }

  @Transactional
  public Utilisateur creer(UtilisateurCreationRequete r) {
    Utilisateur u = new Utilisateur();
    u.setIdentifiant(r.getIdentifiant());
    u.setNom(r.getNom());
    u.setPrenom(r.getPrenom());
    u.setEmail(r.getEmail());
    u.setActif(Boolean.TRUE.equals(r.getActif()));
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
    return repo.save(u);
  }

  @Transactional
  public void definirMotDePasse(Integer id, MotDePasseRequete r) {
    Utilisateur u = obtenir(id);
    String hash = BCrypt.hashpw(r.getMotDePasse(), BCrypt.gensalt());
    u.setMotDePasseHash(hash.getBytes(StandardCharsets.UTF_8));
    repo.save(u);
  }

  public Optional<Utilisateur> trouverParIdentifiant(String identifiant) {
    return repo.findByIdentifiant(identifiant);
  }

  public Optional<Utilisateur> trouverParEmail(String email) {
    return repo.findByEmail(email);
  }
}
