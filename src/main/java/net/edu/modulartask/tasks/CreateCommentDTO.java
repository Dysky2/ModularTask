package net.edu.modulartask.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentDTO(
        @NotBlank(message = "Comment content cannot be blank")
        @Size(max = 5000, message = "Comment content is too long")
        String content,
        UUID parentCommentId
) {}