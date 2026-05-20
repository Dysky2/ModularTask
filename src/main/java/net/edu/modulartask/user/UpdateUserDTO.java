package net.edu.modulartask.user;

public record UpdateUserDTO (
        String firstName,
        String lastName,
        String email,
        UserRole role,
        boolean isActive
) { }
