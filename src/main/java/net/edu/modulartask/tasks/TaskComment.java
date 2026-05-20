package net.edu.modulartask.tasks;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task_comments")
public class TaskComment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private TaskComment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> replies = new ArrayList<>();

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TaskComment() {}

    public TaskComment(Task task, User author, String content, TaskComment parent) {
        this.task = task;
        this.author = author;
        this.content = content;
        this.parentComment = parent;
        this.createdAt = LocalDateTime.now();
    }
}