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
  Optional<Utilisateur> findById(Integer id);

    
    // Récupérer un utilisateur avec ses rôles
    @Query("SELECT u FROM Utilisateur u " +
           "LEFT JOIN FETCH u.roles ur " +
           "LEFT JOIN FETCH ur.role " +
           "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithRoles(@Param("id") Integer id);

}
