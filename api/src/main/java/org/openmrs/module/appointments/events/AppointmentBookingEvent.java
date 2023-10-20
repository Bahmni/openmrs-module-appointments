package org.openmrs.module.appointments.events;

import org.openmrs.module.appointments.model.Appointment;

public class AppointmentBookingEvent extends AppointmentEvent {

    private Appointment appointment;

    public AppointmentBookingEvent(AppointmentEventType eventType, Appointment appointment) {
        super(eventType);
        this.appointment=appointment;
        this.payloadId=appointment.getUuid();
    }

    public Appointment getAppointment() {
        return appointment;
    }
}

