package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.CreditHoraire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditHoraireRepository extends JpaRepository<CreditHoraire, Integer> {

  List<CreditHoraire> findByCompanyIdAndActifTrue(Integer companyId);

  List<CreditHoraire> findByCompanyIdAndActifTrueOrderByPeriodeFinDesc(Integer companyId);

  @Query("SELECT ch FROM CreditHoraire ch WHERE ch.companyId = :companyId " +
         "AND ch.actif = true " +
         "AND :date BETWEEN ch.periodeDebut AND ch.periodeFin")
  List<CreditHoraire> findCreditsActifsADate(@Param("companyId") Integer companyId, 
                                               @Param("date") LocalDate date);

  @Query("SELECT ch FROM CreditHoraire ch WHERE ch.companyId = :companyId " +
         "AND ch.produitId = :produitId " +
         "AND ch.actif = true " +
         "AND :date BETWEEN ch.periodeDebut AND ch.periodeFin")
  Optional<CreditHoraire> findCreditActifPourProduit(@Param("companyId") Integer companyId,
                                                       @Param("produitId") Integer produitId,
                                                       @Param("date") LocalDate date);

  @Query("SELECT SUM(ch.heuresRestantes) FROM CreditHoraire ch " +
         "WHERE ch.companyId = :companyId AND ch.actif = true " +
         "AND ch.periodeFin >= :date")
  Integer sumHeuresRestantesActives(@Param("companyId") Integer companyId, 
                                     @Param("date") LocalDate date);

  @Query("SELECT ch FROM CreditHoraire ch WHERE ch.actif = true " +
         "AND ch.periodeFin < :date")
  List<CreditHoraire> findCreditsExpires(@Param("date") LocalDate date);

  List<CreditHoraire> findByActifTrue();
}
