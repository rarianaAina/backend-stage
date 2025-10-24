package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
  Optional<Company> findByIdExterneCrm(String idExterneCrm);
  Integer findIdExterneCrmById(Integer id);
  //String findNomById(Integer id);
  @Query("SELECT c.nom FROM Company c WHERE c.id = :companyId")
   String findNomById(@Param("companyId") Integer companyId);
    
    // OU Solution 2: Récupérer l'objet Company et extraire le nom
    Optional<Company> findById(Integer companyId);
}
