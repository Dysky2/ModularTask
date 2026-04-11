package net.edu.modulartask.notification;

import java.util.UUID;

public record NotificationEvent(String title, String message, UUID userId) {}