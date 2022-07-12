package org.openmrs.module.appointments.notification;

public enum NotificationType {
    Scheduled("Scheduled"), Reminder("Reminder");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }
}
