package com.nrstudio.portail.controleurs.configurationsadmin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/configurations")
@CrossOrigin(origins = "*")
public class ConfigControleur {
    
    // Stockage temporaire - à remplacer par une base de données
    private final Map<String, Object> configStore = new HashMap<>();
    
    // @GetMapping("/general")
    // public ResponseEntity<Map<String, String>> getGeneralSettings() {
    //     try {
    //         Map<String, String> settings = (Map<String, String>) configStore.getOrDefault("general", getDefaultGeneralSettings());
    //         return ResponseEntity.ok(settings);
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }
    
    @PostMapping("/general")
    public ResponseEntity<Void> saveGeneralSettings(@RequestBody Map<String, String> settings) {
        try {
            configStore.put("general", settings);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // @GetMapping("/email")
    // public ResponseEntity<Map<String, Object>> getEmailSettings() {
    //     try {
    //         Map<String, Object> settings = (Map<String, Object>) configStore.getOrDefault("email", getDefaultEmailSettings());
    //         return ResponseEntity.ok(settings);
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }
    
    // @PostMapping("/email")
    // public ResponseEntity<Void> saveEmailSettings(@RequestBody Map<String, Object> settings) {
    //     try {
    //         configStore.put("email", settings);
    //         return ResponseEntity.ok().build();
    //     } catch (Exception e) {
    //         return ResponseEntity.internalServerError().build();
    //     }
    // }
    
    // Méthodes similaires pour SLA, Credits, Backup...
    
    // private Map<String, String> getDefaultGeneralSettings() {
    //     Map<String, String> defaults = new HashMap<>();
    //     defaults.put("companyName", "OPTIMADA");
    //     defaults.put("defaultLanguage", "fr");
    //     defaults.put("timezone", "UTC+3");
    //     return defaults;
    // }
    
    // private Map<String, Object> getDefaultEmailSettings() {
    //     Map<String, Object> defaults = new HashMap<>();
    //     defaults.put("smtpServer", "");
    //     defaults.put("port", 587);
    //     defaults.put("senderEmail", "");
    //     return defaults;
    // }
}