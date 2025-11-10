package com.nrstudio.portail.services.synchronisations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class SynchronisationManager {
    
    private final Map<String, AtomicBoolean> flagsArret = new ConcurrentHashMap<>();
    private final Map<String, Thread> threadsActifs = new ConcurrentHashMap<>();
    
    public void demarrerSynchronisation(String type) {
        flagsArret.put(type, new AtomicBoolean(false));
    }
    
    public void arreterSynchronisation(String type) {
        AtomicBoolean flag = flagsArret.get(type);
        if (flag != null) {
            flag.set(true);
        }
        
        // Arrêt forcé du thread si nécessaire
        Thread thread = threadsActifs.get(type);
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
    
    public boolean doitArreter(String type) {
        AtomicBoolean flag = flagsArret.get(type);
        return flag != null && flag.get();
    }
    
    public boolean estEnCours(String type) {
        AtomicBoolean flag = flagsArret.get(type);
        return flag != null && !flag.get() && threadsActifs.containsKey(type);
    }
    
    public void enregistrerThread(String type, Thread thread) {
        threadsActifs.put(type, thread);
    }
    
    public void supprimerThread(String type) {
        threadsActifs.remove(type);
        flagsArret.remove(type);
    }
}