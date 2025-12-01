package com.nrstudio.portail.services.synchronisations.config;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nrstudio.portail.dto.synchronisations.SimplifiedSchedulingRequest;
import com.nrstudio.portail.services.synchronisations.config.CronConversionService;

@Service
public class CronConversionService {

    /**
     * Convertit une configuration simplifiée en expression Cron
     */
    public String convertToCronExpression(SimplifiedSchedulingRequest request) {
        if ("CUSTOM".equals(request.getFrequency()) && request.getCustomCron() != null) {
            return request.getCustomCron();
        }
        
        return switch (request.getFrequency()) {
            case "HOURLY" -> "0 0 * * * *";
            case "DAILY" -> buildDailyCron(request.getHour(), request.getMinute());
            case "WEEKLY" -> buildWeeklyCron(request.getHour(), request.getMinute(), request.getDayOfWeek());
            case "MONTHLY" -> buildMonthlyCron(request.getHour(), request.getMinute(), request.getDayOfMonth());
            case "EVERY_30_MIN" -> "0 */30 * * * *";
            case "EVERY_2_HOURS" -> "0 0 */2 * * *";
            case "EVERY_6_HOURS" -> "0 0 */6 * * *";
            default -> "0 0 3 * * *"; // Défaut: tous les jours à 3h
        };
    }

    /**
     * Génère un nom d'affichage lisible
     */
    public String generateDisplayName(SimplifiedSchedulingRequest request) {
        return switch (request.getFrequency()) {
            case "HOURLY" -> "Toutes les heures";
            case "DAILY" -> String.format("Tous les jours à %s:%s", 
                formatTimeValue(request.getHour(), "03"),
                formatTimeValue(request.getMinute(), "00"));
            case "WEEKLY" -> {
                String dayName = getDayName(request.getDayOfWeek());
                yield String.format("Tous les %s à %s:%s", dayName,
                    formatTimeValue(request.getHour(), "03"),
                    formatTimeValue(request.getMinute(), "00"));
            }
            case "MONTHLY" -> String.format("Le %s du mois à %s:%s",
                formatTimeValue(request.getDayOfMonth(), "1"),
                formatTimeValue(request.getHour(), "03"),
                formatTimeValue(request.getMinute(), "00"));
            case "EVERY_30_MIN" -> "Toutes les 30 minutes";
            case "EVERY_2_HOURS" -> "Toutes les 2 heures";
            case "EVERY_6_HOURS" -> "Toutes les 6 heures";
            case "CUSTOM" -> "Planification personnalisée";
            default -> "Planification";
        };
    }

    /**
     * Parse une expression Cron en paramètres simplifiés
     */
    public Map<String, String> parseCronExpression(String cronExpression) {
        Map<String, String> params = new HashMap<>();
        if (cronExpression == null) return params;
        
        String[] parts = cronExpression.split("\\s+");
        if (parts.length == 6) {
            params.put("minute", parts[1]);
            params.put("hour", parts[2]);
            params.put("dayOfMonth", parts[3]);
            params.put("month", parts[4]);
            params.put("dayOfWeek", parts[5]);
            
            // Déterminer le type de fréquence
            params.put("frequency", determineFrequency(cronExpression, parts));
            
            if ("CUSTOM".equals(params.get("frequency"))) {
                params.put("customCron", cronExpression);
            }
        }
        
        return params;
    }

    /**
     * Récupère les options disponibles pour la configuration
     */
    public Map<String, Object> getConfigurationOptions() {
        Map<String, Object> options = new HashMap<>();
        
        // Fréquences prédéfinies
        options.put("frequencies", getFrequencies());
        options.put("hours", getHours());
        options.put("minutes", getMinutes());
        options.put("daysOfWeek", getDaysOfWeek());
        options.put("daysOfMonth", getDaysOfMonth());
        
        return options;
    }

    // Méthodes privées
    private String buildDailyCron(String hour, String minute) {
        return String.format("0 %s %s * * *", 
            formatTimeValue(minute, "0"),
            formatTimeValue(hour, "3"));
    }

    private String buildWeeklyCron(String hour, String minute, String dayOfWeek) {
        return String.format("0 %s %s * * %s", 
            formatTimeValue(minute, "0"),
            formatTimeValue(hour, "3"),
            formatTimeValue(dayOfWeek, "1"));
    }

    private String buildMonthlyCron(String hour, String minute, String dayOfMonth) {
        return String.format("0 %s %s %s * *", 
            formatTimeValue(minute, "0"),
            formatTimeValue(hour, "3"),
            formatTimeValue(dayOfMonth, "1"));
    }

    private String determineFrequency(String cronExpression, String[] parts) {
        if ("0 */30 * * * *".equals(cronExpression)) return "EVERY_30_MIN";
        if ("0 0 */2 * * *".equals(cronExpression)) return "EVERY_2_HOURS";
        if ("0 0 */6 * * *".equals(cronExpression)) return "EVERY_6_HOURS";
        if ("0 0 * * * *".equals(cronExpression)) return "HOURLY";
        if (parts[2].equals("*") && parts[3].equals("*") && parts[4].equals("*") && !parts[5].equals("*")) return "WEEKLY";
        if (!parts[3].equals("*") && parts[4].equals("*") && parts[5].equals("*")) return "MONTHLY";
        if (parts[2].equals("*") && parts[3].equals("*") && parts[4].equals("*") && parts[5].equals("*")) return "DAILY";
        return "CUSTOM";
    }

    private String formatTimeValue(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String getDayName(String dayOfWeek) {
        if (dayOfWeek == null) return "lundi";
        return switch (dayOfWeek) {
            case "1" -> "lundi";
            case "2" -> "mardi";
            case "3" -> "mercredi";
            case "4" -> "jeudi";
            case "5" -> "vendredi";
            case "6" -> "samedi";
            case "7" -> "dimanche";
            default -> "lundi";
        };
    }

    private List<Map<String, String>> getFrequencies() {
        return List.of(
            Map.of("value", "HOURLY", "label", "Toutes les heures", "cron", "0 0 * * * *"),
            Map.of("value", "DAILY", "label", "Tous les jours", "cron", "0 0 3 * * *"),
            Map.of("value", "WEEKLY", "label", "Toutes les semaines", "cron", "0 0 3 * * 1"),
            Map.of("value", "MONTHLY", "label", "Tous les mois", "cron", "0 0 3 1 * *"),
            Map.of("value", "EVERY_30_MIN", "label", "Toutes les 30 minutes", "cron", "0 */30 * * * *"),
            Map.of("value", "EVERY_2_HOURS", "label", "Toutes les 2 heures", "cron", "0 0 */2 * * *"),
            Map.of("value", "EVERY_6_HOURS", "label", "Toutes les 6 heures", "cron", "0 0 */6 * * *"),
            Map.of("value", "CUSTOM", "label", "Personnalisé", "cron", "")
        );
    }

    private List<Map<String, String>> getHours() {
        List<Map<String, String>> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(Map.of(
                "value", String.valueOf(i),
                "label", String.format("%02dh", i)
            ));
        }
        return hours;
    }

    private List<Map<String, String>> getMinutes() {
        return List.of(
            Map.of("value", "0", "label", "00"),
            Map.of("value", "15", "label", "15"),
            Map.of("value", "30", "label", "30"),
            Map.of("value", "45", "label", "45")
        );
    }

    private List<Map<String, String>> getDaysOfWeek() {
        return List.of(
            Map.of("value", "1", "label", "Lundi"),
            Map.of("value", "2", "label", "Mardi"),
            Map.of("value", "3", "label", "Mercredi"),
            Map.of("value", "4", "label", "Jeudi"),
            Map.of("value", "5", "label", "Vendredi"),
            Map.of("value", "6", "label", "Samedi"),
            Map.of("value", "7", "label", "Dimanche")
        );
    }

    private List<Map<String, String>> getDaysOfMonth() {
        List<Map<String, String>> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(Map.of(
                "value", String.valueOf(i),
                "label", String.valueOf(i)
            ));
        }
        return days;
    }
}