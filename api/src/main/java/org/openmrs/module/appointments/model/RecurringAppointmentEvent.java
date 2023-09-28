package org.openmrs.module.appointments.model;

import org.bahmni.module.bahmnicommons.api.model.BahmniEventType;
import org.bahmni.module.bahmnicommons.api.model.Event;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class RecurringAppointmentEvent extends Event {

    private AppointmentRecurringPattern appointmentRecurringPattern;

    public void createRecurringAppointmentEvent(BahmniEventType eventType, AppointmentRecurringPattern appointmentRecurringPattern) {
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

