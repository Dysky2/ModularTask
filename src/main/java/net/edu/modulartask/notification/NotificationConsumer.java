package net.edu.modulartask.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "task-notifications", groupId = "modularTask-group", concurrency = "3")
    public void consume(NotificationEvent event) {
        String personalTopic = "/topic/notifications/" + event.userId();

        messagingTemplate.convertAndSend(personalTopic, event);
    }

}