package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.CompanyPARC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyPARCRepository extends JpaRepository<CompanyPARC, Integer> {

    List<CompanyPARC> findByParcCompanyId(Integer parcCompanyId);

        // NOUVELLE : Récupérer tous les userId distincts pour un comp_companyid
    @Query("SELECT DISTINCT cp.userId FROM CompanyPARC cp WHERE cp.compCompanyId = :compCompanyId AND cp.userId IS NOT NULL")
    List<Integer> findDistinctUserIdsByCompCompanyId(@Param("compCompanyId") Integer compCompanyId);

    // NOUVELLE : Récupérer tous les CompanyPARC avec userId pour un comp_companyid
    @Query("SELECT cp FROM CompanyPARC cp WHERE cp.compCompanyId = :compCompanyId AND cp.userId IS NOT NULL")
    List<CompanyPARC> findByCompCompanyIdWithUsers(@Param("compCompanyId") Integer compCompanyId);

}
