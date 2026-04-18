package com.bookreaderai.backend.dto;

import lombok.Data;

/**
 * DTO returned from user-related endpoints.
 */
@Data
public class UserResponse {
    private Long id;
    private String emailId;
    private String name;
}
