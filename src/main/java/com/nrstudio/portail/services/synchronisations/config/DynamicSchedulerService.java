package com.nrstudio.portail.services.synchronisations.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DynamicSchedulerService {

    private final JdbcTemplate jdbcTemplate;

    public DynamicSchedulerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCronExpression(String jobName) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT cron_expression FROM scheduling_configuration WHERE job_name = ? AND enabled = 1",
                    String.class,
                    jobName
            );
        } catch (Exception e) {
            return "0 * * * * *"; // fallback
        }
    }

    public boolean isEnabled(String jobName) {
        try {
            return Boolean.TRUE.equals(
                    jdbcTemplate.queryForObject(
                            "SELECT enabled FROM scheduling_configuration WHERE job_name = ?",
                            Boolean.class,
                            jobName
                    )
            );
        } catch (Exception e) {
            return false;
        }
    }
}
