package net.edu.modulartask.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    private String theme = "light";

    private String language = "pl";

    @PrePersist
    @PreUpdate
    private void ensureUserId() {
        if (userId == null && user != null) {
            userId = user.getId();
        }
    }
}
