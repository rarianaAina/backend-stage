package com.nrstudio.portail.depots.synchronisation;

import com.nrstudio.portail.domaine.synchronisation.SchedulingConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulingConfigurationRepository extends JpaRepository<SchedulingConfiguration, Integer> {
    
    Optional<SchedulingConfiguration> findByJobName(String jobName);
    
    List<SchedulingConfiguration> findAllByOrderByJobName();
    
    List<SchedulingConfiguration> findByEnabledTrue();
    
    List<SchedulingConfiguration> findByScheduleType(String scheduleType);
    
    boolean existsByJobName(String jobName);
    
    @Modifying
    @Query("UPDATE SchedulingConfiguration s SET s.enabled = :enabled, s.lastModified = CURRENT_TIMESTAMP WHERE s.jobName = :jobName")
    int updateEnabledStatus(@Param("jobName") String jobName, @Param("enabled") boolean enabled);
    
    @Modifying
    @Query("UPDATE SchedulingConfiguration s SET s.cronExpression = :cronExpression, s.displayName = :displayName, " +
           "s.scheduleType = :scheduleType, s.enabled = :enabled, s.lastModified = CURRENT_TIMESTAMP, " +
           "s.lastModifiedBy = :modifiedBy WHERE s.jobName = :jobName")
    int updateConfiguration(@Param("jobName") String jobName,
                          @Param("cronExpression") String cronExpression,
                          @Param("displayName") String displayName,
                          @Param("scheduleType") String scheduleType,
                          @Param("enabled") boolean enabled,
                          @Param("modifiedBy") String modifiedBy);
}