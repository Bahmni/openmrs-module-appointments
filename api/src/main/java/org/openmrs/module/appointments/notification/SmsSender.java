package org.openmrs.module.appointments.notification;

public interface SmsSender {
    void send(String message, String[] to);
}
