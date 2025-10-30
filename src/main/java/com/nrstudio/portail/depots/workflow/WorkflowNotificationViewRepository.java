package com.nrstudio.portail.depots.workflow;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

@Repository
public class WorkflowNotificationViewRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @SuppressWarnings("unchecked")
    public List<Object[]> findCompleteActiveWorkflows() {
        String sql = """
            SELECT 
                tn.code as typeNotificationCode, 
                wn.id as id, 
                wn.ordre as ordre, 
                wn.utilisateur_id as utilisateurId,
                ui.nom as utilisateurNom,
                ui.prenom as utilisateurPrenom,
                ui.email as utilisateurEmail,
                tn.id as typeNotificationId,
                tn.libelle as typeNotificationLibelle
            FROM workflow_notification_mail wn
            INNER JOIN type_notification tn ON wn.type_notification_id = tn.id
            LEFT JOIN utilisateur_interne ui ON wn.utilisateur_id = ui.id
            WHERE wn.est_actif = 1 AND tn.est_actif = 1
            ORDER BY tn.code, wn.ordre
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public List<Object[]> findActiveByTypeNotificationCode(String typeCode) {
        String sql = """
            SELECT 
                tn.code as typeNotificationCode, 
                wn.id as id, 
                wn.ordre as ordre, 
                wn.utilisateur_id as utilisateurId,
                ui.nom as utilisateurNom,
                ui.prenom as utilisateurPrenom,
                ui.email as utilisateurEmail,
                tn.id as typeNotificationId,
                tn.libelle as typeNotificationLibelle
            FROM workflow_notification_mail wn
            INNER JOIN type_notification tn ON wn.type_notification_id = tn.id
            LEFT JOIN utilisateur_interne ui ON wn.utilisateur_id = ui.id
            WHERE wn.est_actif = 1 AND tn.est_actif = 1 AND tn.code = :typeCode
            ORDER BY wn.ordre
            """;
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("typeCode", typeCode);
        return query.getResultList();
    }
}