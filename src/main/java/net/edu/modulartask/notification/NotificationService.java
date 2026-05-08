package net.edu.modulartask.notification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.edu.modulartask.email.EmailSender;
import net.edu.modulartask.tasks.Task;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    @Autowired
    EmailSender emailSender;

    @Autowired
    NotificationRepository repository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private UserService userService;

    public Notification findById(UUID notificationId) {
        return repository.findById(notificationId).orElseThrow(() -> new IllegalArgumentException("This notification is not found"));
    }

    public void sendNotification(String title, String message, UUID userId) {
        NotificationEvent event = new NotificationEvent(title, message, userId);

        kafkaTemplate.send("task-notifications", userId.toString(), event);
    }

    public void notifyAssignment(Task task, User user) {
        emailSender.sendMail(user.getEmail(), task.getTitle(), "system@100kcompany.com", task.getDescription());
    }

    public void notifyRemovedFromTask(Task task, User user) {
        emailSender.sendMail(user.getEmail(), "Remove from " + task.getTitle(), "system@100kcompany.com", "You are not longer working on this task");
    }

    @Transactional
    public void createNotification(String title, String message, User senderId, User targetId) {
        Notification notification = new Notification();

        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setSender(senderId);
        notification.setTarget(targetId);

        repository.save(notification);
    }

    @Scheduled(fixedDelay = 7000)
    public void sendToKafka() {
        List<Notification> list = repository.findAllByStatus(NotificationStatus.PENDING);

        list.forEach(notification -> {
            sendNotification(notification.title, notification.message, notification.target.getId());
            notification.setStatus(NotificationStatus.SENT);
            repository.save(notification);
        });
    }

    public List<Notification> getAllUnreadNotifications() {
        User user = userService.getCurrentlyLoggedUser();

        return repository.findAllByTargetAndStatus(user, NotificationStatus.SENT);
    }

    public ResponseEntity<Map<String, String>> markNotificationAsRead(UUID notificationId) {
        Notification notification = findById(notificationId);

        notification.setStatus(NotificationStatus.READ);

        repository.save(notification);

        return ResponseEntity.ok(Map.of("message", "Notification read"));
    }

    public ResponseEntity<Map<String, String>> markAllAsRead() {

        User user = userService.getCurrentlyLoggedUser();

        List<Notification> notifications = repository.findAllByTarget(user);

        for(var notification : notifications) {
            notification.setStatus(NotificationStatus.READ);

            repository.save(notification);
        }

        return ResponseEntity.ok(Map.of("message", "Notifications read"));
    }

}
