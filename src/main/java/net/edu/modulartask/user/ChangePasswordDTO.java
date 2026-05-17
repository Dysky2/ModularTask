package net.edu.modulartask.user;

public record ChangePasswordDTO(
        String currentPassword,
        String newPassword
) {
}

