package org.openmrs.module.appointments.model;

import org.bahmni.module.bahmnicommons.api.model.BahmniEventType;
import org.bahmni.module.bahmnicommons.api.model.Event;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AppointmentEvent extends Event {

    private Appointment appointment;

    public void createAppointmentEvent(BahmniEventType eventType, Appointment appointment) {
        this.eventType = eventType;
        this.appointment = appointment;
        this.eventId = UUID.randomUUID().toString();
        this.payloadId = appointment.getUuid();
        this.publishedDateTime = LocalDateTime.now();
        this.userContext= Context.getUserContext();
    }

    public Appointment getAppointment() {
        return appointment;
    }
}

