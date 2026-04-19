package net.edu.modulartask.auth;

public record TwoFactorLoginRequest(
        int code,
        String username
) {
}
