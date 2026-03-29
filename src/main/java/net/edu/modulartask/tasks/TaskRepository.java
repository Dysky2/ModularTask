package net.edu.modulartask.tasks;

import net.edu.modulartask.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findById(UUID id);

    List<Task> findByParentTask(Task parentTask);

    List<Task> findAllByAssignees(Set<User> assignees);
}
