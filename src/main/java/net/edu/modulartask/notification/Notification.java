package net.edu.modulartask.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.user.User;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    public UUID id;

    public String title;

    public String message;

    @Enumerated(EnumType.STRING)
    public NotificationStatus status;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    public User sender;

    @ManyToOne
    @JoinColumn(name = "target_id")
    public User target;

}
