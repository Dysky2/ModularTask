package net.edu.modulartask.tasks;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponseDTO(
        UUID id,
        String content,
        LocalDateTime createdAt,
        UUID authorId,
        String authorUsername,
        String authorFirstName,
        String authorLastName,
        UUID parentCommentId,
        List<CommentResponseDTO> replies
) {}