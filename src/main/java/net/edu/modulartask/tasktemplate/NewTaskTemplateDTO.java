package net.edu.modulartask.tasktemplate;

import net.edu.modulartask.subtask.SubTaskDTO;

public record NewTaskTemplateDTO(
        String title,
        String description,
        SubTaskDTO[] subtasks
) {}
