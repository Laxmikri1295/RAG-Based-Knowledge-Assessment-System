package com.bookreaderai.backend.controller;

import com.bookreaderai.backend.dto.CreateUserRequest;
import com.bookreaderai.backend.dto.UserResponse;
import com.bookreaderai.backend.entity.User;
import com.bookreaderai.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link User}.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserResponse> createProfile(@Valid @RequestBody CreateUserRequest request) {
        String normalizedEmail = request.getEmailId().trim().toLowerCase();
        if (userRepository.existsByEmailId(normalizedEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setEmailId(normalizedEmail);
        user.setName(request.getName().trim());
        // NOTE: In production hash the password (e.g., BCrypt)
        user.setPassword(request.getPassword());

        User saved = userRepository.save(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{emailId}")
                .buildAndExpand(saved.getEmailId())
                .toUri();

        return ResponseEntity.created(location).body(toResponse(saved));
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String emailId) {
        String normalizedEmail = emailId.trim().toLowerCase();
        Optional<User> userOptional = userRepository.findByEmailId(normalizedEmail);
        if (userOptional.isEmpty()) {
            log.info("User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User user = userOptional.get();
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{emailId}")
    public ResponseEntity<UserResponse> getUserDetails(@PathVariable String emailId) {
        String normalizedEmail = emailId.trim().toLowerCase();
        return userRepository.findByEmailId(normalizedEmail)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/authenticate/{emailId}/{password}")
    public ResponseEntity<String> authenticate(@PathVariable String emailId,
                                               @PathVariable String password) {
        return userRepository.findByEmailId(emailId)
                .map(user -> user.getPassword().equals(password)
                        ? ResponseEntity.ok("user is valid")
                        : ResponseEntity.status(HttpStatus.CONFLICT).body("user password invalid"))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid userId"));
    }

    @GetMapping("/listAllUsers")
    public ResponseEntity<List<User>> listUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    /* ------------------------------------------------------------------ */
    private UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setEmailId(user.getEmailId());
        dto.setName(user.getName());
        return dto;
    }
}
