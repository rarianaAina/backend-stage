package com.nrstudio.portail.services.synchronisations.config;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SchedulingWatcherService {

    private final JdbcTemplate jdbcTemplate;
    private Map<String, String> lastCronValues = new HashMap<>();

    public SchedulingWatcherService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, String> loadCronValues() {
        return jdbcTemplate.query(
            "SELECT job_name, cron_expression, enabled FROM scheduling_configuration",
            rs -> {
                Map<String, String> map = new HashMap<>();
                while (rs.next()) {
                    String job = rs.getString("job_name");
                    boolean enabled = rs.getBoolean("enabled");
                    String cron = enabled ? rs.getString("cron_expression") : null;
                    map.put(job, cron);
                }
                return map;
            }
        );
    }

    public boolean hasChanged() {
        Map<String, String> newValues = loadCronValues();
        boolean changed = !newValues.equals(lastCronValues);
        lastCronValues = newValues;
        return changed;
    }

    public String getCron(String jobName) {
        return lastCronValues.get(jobName);
    }

    @PostConstruct
    public void debug() {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            System.out.println("👉 Scheduler connecté à la base : " + conn.getCatalog());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
