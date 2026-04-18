package com.bookreaderai.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "metadata")
public class FileMetadata {
    @Id
    @NotBlank
    @Column(name ="bookName", nullable = false, unique = true)
    private String bookName;

    @NotBlank
    @Column(nullable = false)
    private String authorName;
}
