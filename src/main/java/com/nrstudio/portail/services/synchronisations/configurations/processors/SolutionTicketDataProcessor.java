package com.nrstudio.portail.services.synchronisations.configurations.processors;


import java.util.Objects;

public class SolutionTicketDataProcessor {

    public Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}