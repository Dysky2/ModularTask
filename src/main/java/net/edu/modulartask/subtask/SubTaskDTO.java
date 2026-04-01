package net.edu.modulartask.subtask;

import java.util.UUID;

public record SubTaskDTO(
        String title,
        int offsetDays,
        UUID assigneeId
) { }
