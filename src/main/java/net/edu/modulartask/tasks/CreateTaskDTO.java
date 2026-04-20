package net.edu.modulartask.tasks;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.edu.modulartask.subtask.SubTaskDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateTaskDTO (
    @NotBlank String title,
    String description,
    @NotNull LocalDateTime deadline,
    @Min(1) Integer limit,
    List<UUID> assigneeIds,
    List<SubTaskDTO> subtasks
) { }
