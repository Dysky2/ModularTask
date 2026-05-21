package net.edu.modulartask.notification;

import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping("/unread")
    public List<Notification> getAllUnreadNotifications() {
        User user = userService.getCurrentlyLoggedUser();
        return notificationService.getAllUnreadNotifications(user);
    }

    @PostMapping("/{notificationId}/markAsRead")
    public ResponseEntity<Map<String, String>> markNotificationAsRead(@PathVariable(name = "notificationId") UUID notificationId) {
        return notificationService.markNotificationAsRead(notificationId);
    }

    @PostMapping("/markAllAsRead")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        User user = userService.getCurrentlyLoggedUser();
        return notificationService.markAllAsRead(user);
    }

}
