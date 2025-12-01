package com.nrstudio.portail.services.synchronisations.configurations.manager;

import com.nrstudio.portail.services.synchronisations.configurations.constants.SyncConstants;
import com.nrstudio.portail.services.synchronisations.configurations.constants.SyncType;
import com.nrstudio.portail.dto.synchronisations.SyncStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class SyncExecutionManager {
    private static final Logger logger = LoggerFactory.getLogger(SyncExecutionManager.class);
    private final SyncThreadManager syncThreadManager;
    private final Map<String, AtomicBoolean> syncsPlanifieesEnCours = new ConcurrentHashMap<>();
    private final Map<String, SyncStats> statsActuelles = new ConcurrentHashMap<>();
    
    public SyncExecutionManager(SyncThreadManager syncThreadManager) {
        this.syncThreadManager = syncThreadManager;
    }
    
    public boolean peutDemarrerSyncPlanifiee(String entite) {
        if (syncThreadManager.estEnCours(entite)) {
            logger.debug("Sync planifiée {} ignorée - Sync manuelle en cours", entite);
            return false;
        }
        
        AtomicBoolean syncEnCours = syncsPlanifieesEnCours.computeIfAbsent(entite, k -> new AtomicBoolean(false));
        
        if (syncEnCours.get()) {
            logger.debug("Sync planifiée {} ignorée - Déjà en cours", entite);
            return false;
        }
        
        syncEnCours.set(true);
        statsActuelles.put(entite, new SyncStats(entite, SyncType.PLANIFIEE));
        return true;
    }
    
    public void terminerSyncPlanifiee(String entite) {
        AtomicBoolean syncEnCours = syncsPlanifieesEnCours.get(entite);
        if (syncEnCours != null) {
            syncEnCours.set(false);
        }
        completerStats(entite);
    }
    
    // CORRECTION : Meilleure gestion des conflits
    public void validerDemandeSyncManuelle(String entite) {
        // Vérifier si une sync manuelle est déjà en cours
        if (syncThreadManager.estEnCours(entite)) {
            throw new IllegalStateException("Une synchronisation manuelle " + entite + " est déjà en cours");
        }
        
        // Vérifier si une sync planifiée est en cours
        AtomicBoolean syncPlanifieeEnCours = syncsPlanifieesEnCours.get(entite);
        if (syncPlanifieeEnCours != null && syncPlanifieeEnCours.get()) {
            // Au lieu de bloquer, on attend que la sync planifiée se termine
            logger.info("Sync planifiée en cours pour {}, attente avant démarrage manuel", entite);
            attendreFinSyncPlanifiee(entite);
        }
        
        statsActuelles.put(entite, new SyncStats(entite, SyncType.MANUELLE));
    }
    
    // NOUVEAU : Attendre la fin d'une sync planifiée
    private void attendreFinSyncPlanifiee(String entite) {
        AtomicBoolean syncPlanifieeEnCours = syncsPlanifieesEnCours.get(entite);
        if (syncPlanifieeEnCours != null) {
            int tentatives = 0;
            int maxTentatives = 30; // 30 tentatives de 1 seconde = 30 secondes max
            
            while (syncPlanifieeEnCours.get() && tentatives < maxTentatives) {
                try {
                    logger.info("Attente fin sync planifiée {} (tentative {}/{})", entite, tentatives + 1, maxTentatives);
                    Thread.sleep(1000); // Attendre 1 seconde
                    tentatives++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Attente interrompue pour " + entite);
                }
            }
            
            if (syncPlanifieeEnCours.get()) {
                throw new IllegalStateException("Impossible de démarrer la sync manuelle : sync planifiée " + entite + " toujours en cours après " + maxTentatives + " secondes");
            }
        }
    }
    
    // NOUVEAU : Vérifier si on peut démarrer une sync manuelle (pour le frontend)
    public boolean peutDemarrerSyncManuelle(String entite) {
        // Pas de sync manuelle en cours
        if (syncThreadManager.estEnCours(entite)) {
            return false;
        }
        
        // Vérifier si une sync planifiée est en cours
        AtomicBoolean syncPlanifieeEnCours = syncsPlanifieesEnCours.get(entite);
        return syncPlanifieeEnCours == null || !syncPlanifieeEnCours.get();
    }
    
    // NOUVEAU : Obtenir le statut détaillé
    public Map<String, Object> getStatutDetaille(String entite) {
        AtomicBoolean syncPlanifieeEnCours = syncsPlanifieesEnCours.get(entite);
        boolean manuelleEnCours = syncThreadManager.estEnCours(entite);
        boolean arretDemande = syncThreadManager.doitArreter(entite);
        
        Map<String, Object> statut = new java.util.HashMap<>();
        statut.put("entite", entite);
        statut.put("planifieeEnCours", syncPlanifieeEnCours != null && syncPlanifieeEnCours.get());
        statut.put("manuelleEnCours", manuelleEnCours);
        statut.put("arretDemande", arretDemande);
        statut.put("peutDemarrerManuelle", this.peutDemarrerSyncManuelle(entite));
        
        // Déterminer le statut global
        if (manuelleEnCours) {
            statut.put("statutGlobal", arretDemande ? "ARRET_DEMANDE" : "EN_COURS");
        } else if (syncPlanifieeEnCours != null && syncPlanifieeEnCours.get()) {
            statut.put("statutGlobal", "PLANIFIEE_EN_COURS");
        } else {
            statut.put("statutGlobal", "INACTIVE");
        }
        
        return statut;
    }
    
    public void demarrerSynchronisationManuelle(String entite) {
        syncThreadManager.demarrerSynchronisation(entite);
        syncThreadManager.enregistrerThread(entite, Thread.currentThread());
    }
    
    public void terminerSynchronisationManuelle(String entite) {
        syncThreadManager.supprimerThread(entite);
        completerStats(entite);
    }
    
    public boolean doitArreter(String entite) {
        return syncThreadManager.doitArreter(entite);
    }
    
    public void pauseCourte(String entite) {
        if (doitArreter(entite)) {
            throw new RuntimeException("Sync " + entite + " arrêtée à la demande");
        }
        try {
            Thread.sleep(SyncConstants.DELAY_MS_DEFAULT);
        } catch (InterruptedException e) {
            logger.info("Sync {} interrompue", entite);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sync " + entite + " interrompue", e);
        }
    }
    
    public void incrementerTraites(String entite) {
        SyncStats stats = statsActuelles.get(entite);
        if (stats != null) stats.incrementerTraites();
    }
    
    public void incrementerErrones(String entite) {
        SyncStats stats = statsActuelles.get(entite);
        if (stats != null) stats.incrementerErrones();
    }
    
    public SyncStats getStats(String entite) {
        return statsActuelles.get(entite);
    }
    
    public boolean estSyncPlanifieeEnCours(String entite) {
        AtomicBoolean syncEnCours = syncsPlanifieesEnCours.get(entite);
        return syncEnCours != null && syncEnCours.get();
    }
    
    private void completerStats(String entite) {
        SyncStats stats = statsActuelles.get(entite);
        if (stats != null) stats.setFin(LocalDateTime.now());
    }
}