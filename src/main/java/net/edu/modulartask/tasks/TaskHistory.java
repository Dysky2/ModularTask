package net.edu.modulartask.tasks;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public TaskHistory() {
    }

    public TaskHistory(Task task, User user, String action, String details) {
        this.task = task;
        this.user = user;
        this.action = action;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
}
