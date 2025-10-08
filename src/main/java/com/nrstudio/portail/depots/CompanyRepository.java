package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
  Optional<Company> findByIdExterneCrm(String idExterneCrm);
}
