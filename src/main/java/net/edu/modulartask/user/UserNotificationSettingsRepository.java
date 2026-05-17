package net.edu.modulartask.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationSettingsRepository extends JpaRepository<UserNotificationSettings, UUID> {
    Optional<UserNotificationSettings> findByUserId(UUID userId);
}

