package net.edu.modulartask.notification;

import net.edu.modulartask.email.EmailSender;
import net.edu.modulartask.tasks.Task;
import net.edu.modulartask.tasks.TaskRepository;
import net.edu.modulartask.tasks.TaskStatus;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private EmailSender emailSender;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;
    private Task task;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(UUID.randomUUID());
        recipient.setUsername("jan.kowalski");
        recipient.setEmail("jan@firma.pl");
        recipient.setRole(UserRole.USER);
        recipient.setActive(true);
        recipient.setCreatedAt(LocalDateTime.now());

    }
}
