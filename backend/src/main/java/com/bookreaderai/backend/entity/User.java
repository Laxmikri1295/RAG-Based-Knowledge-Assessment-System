package com.bookreaderai.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * User entity persisted to PostgreSQL.
 */
@Entity
@Table(name = "users") // avoid reserved word "user" in PostgreSQL
@Data
@NoArgsConstructor
@Slf4j
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(name = "email_id", unique = true, nullable = false)
    private String emailId;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // NOTE: In production, store a hash (e.g., BCrypt), not plaintext
    @NotBlank
    @Size(min = 6, max = 100)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;
}
