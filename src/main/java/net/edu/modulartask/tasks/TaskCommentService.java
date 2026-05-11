package net.edu.modulartask.tasks;

import jakarta.transaction.Transactional;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.*;

@Service
public class TaskCommentService {

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;


    @Transactional
    public CommentResponseDTO addComment(UUID taskId, CreateCommentDTO dto) {
        if (dto == null || dto.content() == null || dto.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content is empty");
        }

        var task = taskService.findById(taskId);
        var currentUser = userService.getCurrentlyLoggedUser();
        validateLoggedUser(currentUser);
        validateCommentAccess(task, currentUser);

        TaskComment parent = null;
        if (dto.parentCommentId() != null) {
            UUID parentId = dto.parentCommentId();
            parent = taskCommentRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment not found"));

            if (!parent.getTask().getId().equals(task.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment belongs to different task");
            }
        }

        TaskComment comment = new TaskComment(task, currentUser, dto.content().trim(), parent);
        TaskComment saved = taskCommentRepository.save(comment);
        logTaskHistory(task, currentUser, "COMMENT_ADDED", buildCommentLog(saved));

        return mapToDtoWithChildren(saved, new HashMap<>());
    }


    public List<CommentResponseDTO> getCommentsTree(UUID taskId) {
        var task = taskService.findById(taskId);
        List<TaskComment> comments = taskCommentRepository.findAllByTaskOrderByCreatedAtAsc(task);

        Map<UUID, List<TaskComment>> parentToChildren = new HashMap<>();
        for (TaskComment c : comments) {
            if (c.getParentComment() == null) continue;
            UUID parentId = c.getParentComment().getId();
            parentToChildren.computeIfAbsent(parentId, k -> new ArrayList<>()).add(c);
        }

        List<CommentResponseDTO> result = new ArrayList<>();
        for (TaskComment root : comments) {
            if (root.getParentComment() == null) {
                result.add(buildDtoRecursively(root, parentToChildren));
            }
        }

        return result;
    }


    @Transactional
    public CommentResponseDTO updateComment(UUID taskId, UUID commentId, CreateCommentDTO dto) {
        if (dto == null || dto.content() == null || dto.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment content is empty");
        }

        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        validateCommentBelongsToTask(comment, taskId);

        var currentUser = userService.getCurrentlyLoggedUser();
        validateLoggedUser(currentUser);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own comments");
        }

        comment.setContent(dto.content().trim());
        TaskComment updated = taskCommentRepository.save(comment);
        logTaskHistory(updated.getTask(), currentUser, "COMMENT_UPDATED", buildCommentLog(updated));

        return mapToDtoWithChildren(updated, new HashMap<>());
    }


    @Transactional
    public void deleteComment(UUID taskId, UUID commentId) {
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        validateCommentBelongsToTask(comment, taskId);

        var currentUser = userService.getCurrentlyLoggedUser();
        validateLoggedUser(currentUser);

        if (!comment.getAuthor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own comments");
        }

        Task task = comment.getTask();
        String details = buildCommentLog(comment);
        taskCommentRepository.deleteById(commentId);
        logTaskHistory(task, currentUser, "COMMENT_DELETED", details);
    }


    private void validateCommentBelongsToTask(TaskComment comment, UUID taskId) {
        if (!comment.getTask().getId().equals(taskId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment does not belong to this task");
        }
    }

    private void validateLoggedUser(net.edu.modulartask.user.User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not logged");
        }
    }

    private void validateCommentAccess(Task task, net.edu.modulartask.user.User user) {
        boolean isCreator = task.getCreator() != null && task.getCreator().getId().equals(user.getId());
        boolean isAssignee = task.getAssignees().stream().anyMatch(assignee -> assignee.getId().equals(user.getId()));

        if (!isCreator && !isAssignee) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only creator or assignee can comment this task");
        }
    }

    private void logTaskHistory(Task task, net.edu.modulartask.user.User user, String action, String details) {
        taskHistoryRepository.save(new TaskHistory(task, user, action, details));
    }

    private String buildCommentLog(TaskComment comment) {
        String trimmed = comment.getContent() == null ? "" : comment.getContent().trim();
        String snippet = trimmed.length() > 120 ? trimmed.substring(0, 120) + "..." : trimmed;
        return "CommentId=" + comment.getId() + ", parentId=" +
                (comment.getParentComment() == null ? "null" : comment.getParentComment().getId()) +
                ", content=\"" + snippet + "\"";
    }


    private CommentResponseDTO buildDtoRecursively(
            TaskComment comment,
            Map<UUID, List<TaskComment>> parentToChildren) {

        List<TaskComment> children = parentToChildren.getOrDefault(comment.getId(), Collections.emptyList());
        List<CommentResponseDTO> childDtos = new ArrayList<>();

        for (TaskComment child : children) {
            childDtos.add(buildDtoRecursively(child, parentToChildren));
        }

        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getFirstName(),
                comment.getAuthor().getLastName(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                childDtos
        );
    }


    private CommentResponseDTO mapToDtoWithChildren(
            TaskComment comment,
            Map<UUID, List<TaskComment>> parentToChildren) {

        List<TaskComment> children = parentToChildren.getOrDefault(comment.getId(), Collections.emptyList());
        List<CommentResponseDTO> childDtos = children.stream()
                .map(child -> buildDtoRecursively(child, parentToChildren))
                .toList();

        return new CommentResponseDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getAuthor().getId(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getFirstName(),
                comment.getAuthor().getLastName(),
                comment.getParentComment() == null ? null : comment.getParentComment().getId(),
                childDtos
        );
    }
}