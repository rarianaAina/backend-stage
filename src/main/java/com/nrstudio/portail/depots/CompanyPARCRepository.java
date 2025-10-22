package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.CompanyPARC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyPARCRepository extends JpaRepository<CompanyPARC, Integer> {

    List<CompanyPARC> findByParcCompanyId(Integer parcCompanyId);

}
