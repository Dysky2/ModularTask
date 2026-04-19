package net.edu.modulartask.organization;

import jakarta.validation.constraints.NotBlank;

public record RenameOrganizationUnitDTO(
        @NotBlank String name
) {
}

