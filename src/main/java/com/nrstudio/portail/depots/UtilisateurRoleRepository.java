package com.nrstudio.portail.depots;


import com.nrstudio.portail.domaine.UtilisateurRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtilisateurRoleRepository extends JpaRepository<UtilisateurRole, Integer> {
}
