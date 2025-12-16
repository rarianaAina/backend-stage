package com.nrstudio.portail.services.synchronisations.configurations.processors;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

public class TicketDataProcessor {

    public Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.valueOf(o.toString()); } catch(Exception e){ return null; }
    }

    public String toString(Object o, String defaultValue) {
        if (o == null) return defaultValue;
        return Objects.toString(o, defaultValue);
    }

    public LocalDateTime toLdt(Object o) {
        if (o == null) return null;
        if (o instanceof Timestamp) return ((Timestamp)o).toLocalDateTime();
        if (o instanceof java.util.Date) return new Timestamp(((java.util.Date)o).getTime()).toLocalDateTime();
        return null;
    }

    public Integer mapPrioriteCrmStringToId(String s) {
        if (s == null) return 3;
        if (s.equals("Urgent")) return 1;
        if (s.equals("High"))   return 2;
        if (s.equals("Normal")) return 3;
        return 3;
    }

    public Integer mapStageCrmStringToId(String stage, String statut) {
        if ("Closed".equals(statut)) {
            return 7;
        }
        
        if (stage == null) {
            return mapStatutCrmStringToId(statut);
        }
        
        switch (stage.toLowerCase()) {
            case "logged":
                return 1;
            case "confirmed":
                return 2;
            case "waiting":
                return 3;
            case "solved":
                return 6;
            default:
                return mapStatutCrmStringToId(statut);
        }
    }

    private Integer mapStatutCrmStringToId(String s) {
        if (s == null) return 1;
        if (s.equals("Closed")) return 7;
        if (s.equals("Pending")) return 3;
        if (s.equals("In Progress")) return 2;
        return 1;
    }
}