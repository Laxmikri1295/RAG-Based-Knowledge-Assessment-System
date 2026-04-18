package com.bookreaderai.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BackendApplication {

    // Use env var HF_API_KEY or application.properties to avoid hardcoding.
    private static final String HF_TOKEN = System.getenv().getOrDefault("HF_API_KEY",
            "************************************");

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
