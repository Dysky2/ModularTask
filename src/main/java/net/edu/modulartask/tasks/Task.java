package net.edu.modulartask.tasks;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.user.User;

import java.time.LocalDateTime;
import java.util.*;

@Setter
@Getter
@Entity
@Table(name = "Tasks")
public class Task {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    @JoinColumn(name = "parent_task_id")
    @JsonIgnore
    private Task parentTask;

    @ManyToMany
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignees = new HashSet<>();

    private LocalDateTime deadline;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> subtasks = new ArrayList<>();
}
