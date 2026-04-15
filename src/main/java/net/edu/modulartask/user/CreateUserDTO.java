package net.edu.modulartask.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserDTO(
    @NotBlank String username,
    String firstName,
    String lastName,
    @NotBlank String email,
    @NotBlank String password,
    UserRole role,
    boolean isActive
) { }
