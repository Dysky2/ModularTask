package net.edu.modulartask.auth;

public record TwoFactorRequest(
        int code,
        String key
) {
}
