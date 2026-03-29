package net.edu.modulartask.subtask;

import net.edu.modulartask.tasktemplate.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubtaskTemplateRepository extends JpaRepository<SubtaskTemplate, UUID> {
    Optional<SubtaskTemplate> findById(UUID id);

    List<SubtaskTemplate> findByTemplate(TaskTemplate template);

}
