package net.edu.modulartask.tasks;

import net.edu.modulartask.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record TaskDetailsResponseDTO(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskStatus userStatus,
        Set<User> assignees,
        int limit,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        User creator,
        UUID parentTaskId,
        List<TaskResponseDTO> subtasks
) {
}
