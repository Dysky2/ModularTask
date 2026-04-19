package net.edu.modulartask.organization;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateOrganizationUnitDTO(
        @NotBlank String name,
        UUID parentId
) {
}

