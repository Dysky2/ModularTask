package net.edu.modulartask.user;

public record UpdateProfileDTO(
        String firstName,
        String lastName,
        String position,
        String avatarUrl,
        String description
) {
}

