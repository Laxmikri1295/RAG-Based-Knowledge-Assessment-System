package com.bookreaderai.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request payload for creating a user profile.
 */
@Data
public class CreateUserRequest {

    @Email
    @NotBlank
    private String emailId;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password; // NOTE: hash before persisting in production
}
