package net.edu.modulartask.user;

public record NotificationSettingsDTO(
        boolean notifyOnAssignment,
        boolean notifyOnMention,
        boolean notifyOnStatusChange,
        boolean dailyDigestEnabled
) {
}

