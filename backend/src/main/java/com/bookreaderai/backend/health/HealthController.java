package com.bookreaderai.backend.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public HealthController(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/api/fix-db")
    public String fixDb() {
        try {
            jdbcTemplate.execute("TRUNCATE TABLE documents");
            jdbcTemplate.execute("ALTER TABLE documents ALTER COLUMN embedding TYPE vector(1024)");
            return "Successfully updated pgvector column for Ollama (1024 dimensions)!";
        } catch (Exception e) {
            return "Failed to fix DB: " + e.getMessage();
        }
    }
}
