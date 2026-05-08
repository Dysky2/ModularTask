package net.edu.modulartask.notification;

import net.edu.modulartask.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByTarget(User target);

    List<Notification> findAllByStatus(NotificationStatus status);

    List<Notification> findAllByTargetAndStatus(User target, NotificationStatus status);
}
