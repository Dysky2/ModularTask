package net.edu.modulartask.tasks;

import net.edu.modulartask.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findById(UUID id);

    List<Task> findALlByCreatorIdAndStatus(UUID id, TaskStatus status);

    List<Task> findByParentTask(Task parentTask);

    List<Task> findAllByAssignees(Set<User> assignees);

    List<Task> findByAssigneesContaining(User user);

    @Query(value = """
    WITH RECURSIVE TasksRecursive AS (
        SELECT id, title, description, status, creator_id, parent_task_id, deadline, created_at, task_limit
        FROM tasks
        WHERE creator_id = :creatorId

        UNION ALL

        SELECT t.id, t.title, t.description, t.status, t.creator_id, t.parent_task_id, t.deadline, t.created_at, t.task_limit
        FROM tasks t
        JOIN TasksRecursive tr ON t.parent_task_id = tr.id
    )
    SELECT DISTINCT id, title, description, status, creator_id, parent_task_id, deadline, created_at, task_limit
    FROM TasksRecursive
    WHERE status = CAST(:status AS text)
    """, nativeQuery = true)
    List<Task> findAllTasksByCreatorIdAndStatus(
            @Param("creatorId") UUID creatorID,
            @Param("status") String status
    );

}
