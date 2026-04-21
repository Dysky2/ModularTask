package net.edu.modulartask.tasks;

import net.edu.modulartask.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {
    List<TaskHistory> findTop10ByUserOrderByCreatedAtDesc(User user);

    List<TaskHistory> findAllByTaskOrderByCreatedAtDesc(Task task);
}
