package net.edu.modulartask.user;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivityDTO(
        UUID taskId,
        String taskTitle,
        String action,
        String details,
        LocalDateTime createdAt
) { }

