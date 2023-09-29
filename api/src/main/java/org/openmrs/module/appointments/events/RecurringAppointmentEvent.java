package org.openmrs.module.appointments.events;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;


@Component
public class RecurringAppointmentEvent extends AppointmentEvent {
    private AppointmentRecurringPattern appointmentRecurringPattern;

    public void createRecurringAppointmentEvent(AppointmentEventType eventType, AppointmentRecurringPattern appointmentRecurringPattern) {
        this.eventType = eventType;
        this.appointmentRecurringPattern = appointmentRecurringPattern;
        this.eventId = UUID.randomUUID().toString();
        this.payloadId = appointmentRecurringPattern.getAppointments().iterator().next().getUuid();
        this.publishedDateTime = LocalDateTime.now();
        this.userContext= Context.getUserContext();
    }

    public AppointmentRecurringPattern getAppointmentRecurringPattern() {
        return appointmentRecurringPattern;
    }
}

