package net.edu.modulartask.tasktemplate;

import net.edu.modulartask.subtask.SubtaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, UUID> {
        Optional<TaskTemplate> findById(UUID id);
        
        List<TaskTemplate> findBySubtasks(List<SubtaskTemplate> subtasks);

}
