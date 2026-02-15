package com.example.demo.dto;

import com.example.demo.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
    @NotBlank @Size(max = 150) String name,
    @NotBlank @Email @Size(max = 190) String email,
    @NotBlank @Size(min = 6, max = 120) String password,
    @NotNull Role role,
    Long departmentId
) {
}
