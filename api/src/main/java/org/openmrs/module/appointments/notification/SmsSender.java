package org.openmrs.module.appointments.notification;

import java.util.List;

public interface SmsSender {
    void send(String message, List<String> to);
}
