package com.nrstudio.portail.depots;

import com.nrstudio.portail.domaine.WorkflowNotificationMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface WorkflowNotificationMailRepository extends JpaRepository<WorkflowNotificationMail, Integer> {
    
    @Query("SELECT w FROM WorkflowNotificationMail w " +
           "JOIN FETCH w.typeNotification tn " +
           "WHERE tn.code = :typeNotificationCode " +
           "AND w.estActif = true " +
           "AND tn.estActif = true " +
           "ORDER BY w.ordre")
    List<WorkflowNotificationMail> findByTypeNotificationCodeActif(@Param("typeNotificationCode") String typeNotificationCode);
    
    @Query("SELECT w FROM WorkflowNotificationMail w " +
           "JOIN FETCH w.typeNotification tn " +
           "WHERE w.estActif = true " +
           "AND tn.estActif = true " +
           "ORDER BY tn.code, w.ordre")
    List<WorkflowNotificationMail> findAllActifs();
    
    @Query("SELECT w FROM WorkflowNotificationMail w " +
           "WHERE w.typeNotification.id = :typeNotificationId " +
           "AND w.estActif = true " +
           "ORDER BY w.ordre")
    List<WorkflowNotificationMail> findByTypeNotificationIdActif(@Param("typeNotificationId") Integer typeNotificationId);
    
    void deleteByTypeNotificationId(Integer typeNotificationId);
}
