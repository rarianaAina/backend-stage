package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {
  Optional<Produit> findByIdExterneCrm(String idExterneCrm);
  List<Produit> findByActif(boolean actif);
  //Integer findIdByIdExterneCrm(String idExterneCrm);
}


