package net.edu.modulartask.admin;

public record SystemConfigDTO(
        String configKey,
        String configValue,
        String description
) {
}

