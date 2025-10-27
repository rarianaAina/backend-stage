package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
  Optional<Utilisateur> findByIdentifiant(String identifiant);
  Optional<Utilisateur> findByIdExterneCrm(String idExterneCrm);
  Optional<Utilisateur> findByEmail(String email);

}
