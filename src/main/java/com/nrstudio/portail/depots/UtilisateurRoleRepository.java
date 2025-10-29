package com.nrstudio.portail.depots;


import com.nrstudio.portail.domaine.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Integer> {
    @Query("SELECT ur FROM UtilisateurRole ur WHERE ur.utilisateur.id = :utilisateurId")
    List<UtilisateurRole> findByUtilisateurId(@Param("utilisateurId") Integer utilisateurId);
    
    @Query("SELECT ur FROM UtilisateurRole ur WHERE ur.role.id IN :roleIds")
    List<UtilisateurRole> findByRoleIdIn(@Param("roleIds") List<Integer> roleIds);
    
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END " +
           "FROM UtilisateurRole ur " +
           "WHERE ur.utilisateur.id = :utilisateurId " +
           "AND ur.role.id IN :roleIds")
    boolean existsByUtilisateurIdAndRoleIdIn(@Param("utilisateurId") Integer utilisateurId, 
                                           @Param("roleIds") List<Integer> roleIds);

}
