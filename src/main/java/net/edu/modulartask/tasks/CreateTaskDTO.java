package net.edu.modulartask.tasks;

import jakarta.validation.constraints.NotBlank;
import net.edu.modulartask.subtask.SubTaskDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateTaskDTO (
    @NotBlank String title,
    String description,
    @NotBlank LocalDateTime deadline,
    List<UUID> assigneeIds,
    List<SubTaskDTO> subtasks
) { }
