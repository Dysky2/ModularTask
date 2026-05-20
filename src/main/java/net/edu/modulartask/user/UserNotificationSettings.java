package net.edu.modulartask.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_notification_settings")
public class UserNotificationSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    @PreUpdate
    private void ensureUserId() {
        if (userId == null && user != null) {
            userId = user.getId();
        }
    }

    @Column(name = "notify_on_assignment")
    private boolean notifyOnAssignment = true;

    @Column(name = "notify_on_mention")
    private boolean notifyOnMention = true;

    @Column(name = "notify_on_status_change")
    private boolean notifyOnStatusChange = true;

    @Column(name = "daily_digest_enabled")
    private boolean dailyDigestEnabled;
}
