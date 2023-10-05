package org.openmrs.module.appointments.events;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.time.LocalDateTime;
import java.util.UUID;

public class AppointmentEvent {
    private static final long version = 1L;
    public UserContext userContext;
    public final String eventId;
    public final AppointmentEventType eventType;
    public String payloadId;
    public LocalDateTime publishedDateTime;
	public AppointmentEvent(AppointmentEventType eventType) {
		this.eventType = eventType;
		this.eventId = UUID.randomUUID().toString();
		this.publishedDateTime = LocalDateTime.now();
		this.userContext= Context.getUserContext();
        this.payloadId="";
	}
}


