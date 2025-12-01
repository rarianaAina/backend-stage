package com.nrstudio.portail.services.synchronisations.configurations.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SyncThreadManager {
    private static final Logger logger = LoggerFactory.getLogger(SyncThreadManager.class);
    private final Map<String, Thread> threadsSync = new ConcurrentHashMap<>();
    private final Map<String, Boolean> arretDemande = new ConcurrentHashMap<>();
    
    public boolean estEnCours(String typeSync) {
        Thread thread = threadsSync.get(typeSync);
        return thread != null && thread.isAlive();
    }
    
    public void demarrerSynchronisation(String typeSync) {
        arretDemande.put(typeSync, false);
        logger.debug("Démarrage synchronisation: {}", typeSync);
    }
    
    public void enregistrerThread(String typeSync, Thread thread) {
        threadsSync.put(typeSync, thread);
        logger.debug("Thread enregistré pour {}: {}", typeSync, thread.getName());
    }
    
    public void supprimerThread(String typeSync) {
        threadsSync.remove(typeSync);
        arretDemande.remove(typeSync);
        logger.debug("Thread supprimé pour: {}", typeSync);
    }
    
    public boolean doitArreter(String typeSync) {
        return arretDemande.getOrDefault(typeSync, false);
    }
    
    public void demanderArret(String typeSync) {
        arretDemande.put(typeSync, true);
        logger.info("Arrêt demandé pour: {}", typeSync);
    }
    
    public void arreterSynchronisation(String typeSync) {
        demanderArret(typeSync);
        Thread thread = threadsSync.get(typeSync);
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            logger.info("Interruption envoyée pour: {}", typeSync);
        }
    }
    
    public Map<String, Thread> getThreadsSync() {
        return new ConcurrentHashMap<>(threadsSync);
    }
    
    public void nettoyer(String typeSync) {
        threadsSync.remove(typeSync);
        arretDemande.remove(typeSync);
        logger.debug("Nettoyage effectué pour: {}", typeSync);
    }
}