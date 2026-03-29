package net.edu.modulartask.tasktemplate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "task_templates")
public class TaskTemplate {

    @Id
    private UUID id;

    private String title;

    private String description;
}
