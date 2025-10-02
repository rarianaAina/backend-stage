package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Integer> {
  Optional<Utilisateur> findByIdentifiant(String identifiant);
  Optional<Utilisateur> findByIdExterneCrm(String idExterneCrm);
}
