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

      // Récupérer les utilisateurs internes (role_id = 2 ou 3)
    @Query("SELECT DISTINCT u FROM Utilisateur u " +
           "JOIN UtilisateurRole ur ON u.id = ur.utilisateur.id " +
           "WHERE ur.role.id IN (2, 3) " +
           "AND u.actif = true " +
           "ORDER BY u.nom, u.prenom")
    List<Utilisateur> findUtilisateursInternes();
    
    // Version DTO avec nom complet
    @Query("SELECT DISTINCT new com.nrstudio.portail.dto.UserDto(" +
           "u.id, " +
           "CONCAT(COALESCE(u.prenom, ''), ' ', u.nom), " +
           "u.email) " +
           "FROM Utilisateur u " +
           "JOIN UtilisateurRole ur ON u.id = ur.utilisateur.id " +
           "WHERE ur.role.id IN (2, 3) " +
           "AND u.actif = true " +
           "ORDER BY u.nom, u.prenom")
    List<UserDto> findUtilisateursInternesDto();
    
    // Récupérer un utilisateur avec ses rôles
    @Query("SELECT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.roles ur " +
           "LEFT JOIN FETCH ur.role " +
           "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithRoles(@Param("id") Integer id);

}
