package net.edu.modulartask.tasktemplate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.subtask.SubtaskTemplate;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "task_templates")
public class TaskTemplate {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER)
    private List<SubtaskTemplate> subtasks;
}
