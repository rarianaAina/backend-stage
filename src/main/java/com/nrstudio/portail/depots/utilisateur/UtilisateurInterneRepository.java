package com.nrstudio.portail.depots.utilisateur;

import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.dto.utilisateur.UtilisateurInterneDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurInterneRepository extends JpaRepository<UtilisateurInterne, Integer> {
    
    @Query("SELECT new com.nrstudio.portail.dto.utilisateur.UtilisateurInterneDto(ui.id, ui.nom, ui.prenom, ui.email) " +
           "FROM UtilisateurInterne ui ORDER BY ui.nom, ui.prenom")
    List<UtilisateurInterneDto> findAllUtilisateurs();
    
    boolean existsById(Integer id);
    
    @Query("SELECT COUNT(ui) FROM UtilisateurInterne ui WHERE ui.id IN :userIds")
    long countByIdIn(@Param("userIds") List<Integer> userIds);

    Optional<UtilisateurInterne> findByIdExterneCrm(String idExterneCrm);
    //find by email
    Optional<UtilisateurInterne> findByEmail(String email);
    //find by identifiant
    Optional<UtilisateurInterne> findByIdentifiant(String identifiant);

    @Query("SELECT CONCAT(u.prenom, ' ', u.nom) FROM UtilisateurInterne u WHERE u.idExterneCrm = :userId")
    Optional<String> findUserFullNameByIdExterneCrm(@Param("userId") Integer userId);
}