package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nrstudio.portail.dto.workflow.UserDto;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
  Optional<Utilisateur> findByIdentifiant(String identifiant);
  Optional<Utilisateur> findByIdExterneCrm(String idExterneCrm);
  Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM Utilisateur u " +
           "JOIN u.roles ur " +
           "WHERE ur.role.id IN (2, 3) " +
           "AND u.actif = true " +
           "ORDER BY u.nom, u.prenom")
    List<Utilisateur> findUtilisateursInternes();
    
    // Version DTO avec nom complet - Version corrigée
    
    // Récupérer un utilisateur avec ses rôles
    @Query("SELECT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.roles ur " +
           "LEFT JOIN FETCH ur.role " +
           "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithRoles(@Param("id") Integer id);
    
    // Méthode alternative plus simple pour vérifier si un utilisateur a un rôle interne
    @Query("SELECT COUNT(ur) > 0 FROM UtilisateurRole ur " +
           "WHERE ur.utilisateur.id = :utilisateurId " +
           "AND ur.role.id IN (2, 3)")
    boolean isUtilisateurInterne(@Param("utilisateurId") Integer utilisateurId);

   //récupérer tous les utilisateurs
       @Query("SELECT u FROM Utilisateur u")
       List<Utilisateur> findAllUtilisateurs();

}
