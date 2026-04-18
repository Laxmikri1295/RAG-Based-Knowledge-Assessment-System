package com.bookreaderai.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming chat request body. Contains the user’s prompt that will be forwarded
 * to the Ollama model.
 */
public class ChatRequest {

    @NotBlank
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
