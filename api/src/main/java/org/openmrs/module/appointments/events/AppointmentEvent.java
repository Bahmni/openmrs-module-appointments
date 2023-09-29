package org.openmrs.module.appointments.events;

import org.openmrs.api.context.UserContext;
import org.openmrs.module.appointments.events.AppointmentEventType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AppointmentEvent {
    private static final long version = 1L;
    public UserContext userContext;
    public String eventId;
    public AppointmentEventType eventType;
    public String payloadId;
    public LocalDateTime publishedDateTime;

    public UserContext getUserContext() {
        return userContext;
    }
    public String getEventId() {
        return eventId;
    }
    public AppointmentEventType getEventType() {
        return eventType;
    }
    public String getPayloadId() {
        return payloadId;
    }
    public LocalDateTime getPublishedDateTime() {
        return publishedDateTime;
    }
}


