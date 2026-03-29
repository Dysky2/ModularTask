package net.edu.modulartask.subtask;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.tasktemplate.TaskTemplate;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "subtask_templates")
public class SubtaskTemplate {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "templete_id")
    private TaskTemplate template;

    private String title;

    @JoinColumn(name = "offset_days")
    private int offsetDays;
}
