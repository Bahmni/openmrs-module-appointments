package org.openmrs.module.appointments.events;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AppointmentBookingEvent extends AppointmentEvent {

    private Appointment appointment;

    public void createAppointmentEvent(AppointmentEventType eventType, Appointment appointment) {
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

