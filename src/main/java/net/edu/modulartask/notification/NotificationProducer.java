package net.edu.modulartask.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotification(String title, String message, UUID userId) {
        NotificationEvent event = new NotificationEvent(title, message, userId);

        kafkaTemplate.send("task-notifications", userId.toString(), event);
    }
}