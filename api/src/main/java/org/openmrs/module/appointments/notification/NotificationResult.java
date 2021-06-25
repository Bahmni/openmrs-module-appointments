package org.openmrs.module.appointments.notification;

public class NotificationResult {
    public static int SUCCESS_STATUS = 0;
    private String uuid;
    private int status = SUCCESS_STATUS;
    private String message;

    public NotificationResult(String uuid, int status, String message) {
        this.uuid = uuid;
        this.status = status;
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
