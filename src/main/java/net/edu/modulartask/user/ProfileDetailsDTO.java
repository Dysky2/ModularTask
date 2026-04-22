package net.edu.modulartask.user;

import jakarta.validation.constraints.NotBlank;
import net.edu.modulartask.organization.OrganizationUnit;

import java.util.List;

public record ProfileDetailsDTO(
        String username,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        String description,
        OrganizationUnit organizationUnit,
        boolean isActive,
        List<UserActivityDTO> recentActivity
) { }
