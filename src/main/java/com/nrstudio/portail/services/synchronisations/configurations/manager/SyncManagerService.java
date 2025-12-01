package com.nrstudio.portail.services.synchronisations.configurations.manager;

import com.nrstudio.portail.dto.synchronisations.SyncStats;
import com.nrstudio.portail.dto.synchronisations.Synchronisable;
import com.nrstudio.portail.services.synchronisations.configurations.manager.SyncExecutionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SyncManagerService.class);
    private final Map<String, Synchronisable> synchronisateurs;
    private final SyncExecutionManager executionManager;
    private final SyncThreadManager syncThreadManager; // AJOUT
    
    public SyncManagerService(List<Synchronisable> synchronisateurs,
                            SyncExecutionManager executionManager,
                            SyncThreadManager syncThreadManager) { // AJOUT
        this.synchronisateurs = synchronisateurs.stream()
            .collect(Collectors.toMap(Synchronisable::getEntite, s -> s));
        this.executionManager = executionManager;
        this.syncThreadManager = syncThreadManager; // AJOUT
    }
    
    public void synchroniserManuellement(String entite) {
        // Vérifier si on peut démarrer
        if (!executionManager.peutDemarrerSyncManuelle(entite)) {
            Map<String, Object> statut = executionManager.getStatutDetaille(entite);
            String message = "Impossible de démarrer la synchronisation manuelle : ";
            
            if ((Boolean) statut.get("manuelleEnCours")) {
                message += "une synchronisation manuelle est déjà en cours";
            } else if ((Boolean) statut.get("planifieeEnCours")) {
                message += "une synchronisation planifiée est en cours. Veuillez réessayer dans quelques instants";
            } else {
                message += "raison inconnue";
            }
            
            throw new IllegalStateException(message);
        }
        
        Synchronisable synchronisateur = synchronisateurs.get(entite);
        if (synchronisateur != null) {
            synchronisateur.synchroniserManuellement();
        } else {
            throw new IllegalArgumentException("Synchronisateur inconnu pour: " + entite);
        }
    }
    
    public boolean peutDemarrerSyncManuelle(String entite) {
        return executionManager.peutDemarrerSyncManuelle(entite);
    }
    
    public Map<String, Object> getStatutDetaille(String entite) {
        return executionManager.getStatutDetaille(entite);
    }
    
    // CORRECTION : Utiliser syncThreadManager au lieu de executionManager
    public void arreterSynchronisation(String entite) {
        syncThreadManager.arreterSynchronisation(entite); // CORRIGÉ
        logger.info("Arrêt demandé pour: {}", entite);
    }
    
    public SyncStats getStats(String entite) {
        Synchronisable synchronisateur = synchronisateurs.get(entite);
        return synchronisateur != null ? synchronisateur.getStats() : null;
    }
    
    public Map<String, SyncStats> getToutesStats() {
        return synchronisateurs.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getStats()
            ));
    }
    
    public boolean estSyncEnCours(String entite) {
        return executionManager.estSyncPlanifieeEnCours(entite) || 
               syncThreadManager.estEnCours(entite); // CORRIGÉ
    }
    
    public Map<String, Map<String, Object>> getTousStatutsDetaillees() {
        return synchronisateurs.keySet().stream()
            .collect(Collectors.toMap(
                entite -> entite,
                this::getStatutDetaille
            ));
    }
    
    public List<String> getEntitesDisponibles() {
        return List.copyOf(synchronisateurs.keySet());
    }
    
    public Map<String, Boolean> getStatutSynchronisations() {
        return synchronisateurs.keySet().stream()
            .collect(Collectors.toMap(
                entite -> entite,
                this::estSyncEnCours
            ));
    }
    
    // CORRECTION : Utiliser syncThreadManager au lieu de executionManager
    public void nettoyer(String entite) {
        syncThreadManager.nettoyer(entite); // CORRIGÉ
        logger.info("Nettoyage effectué pour: {}", entite);
    }
}