package org.openmrs.module.appointments.event;

import org.openmrs.module.appointments.model.Appointment;
import org.springframework.context.ApplicationEvent;

public class TeleconsultationAppointmentSavedEvent extends ApplicationEvent {
    private Appointment appointment;

    public TeleconsultationAppointmentSavedEvent(Appointment appointment) {
        super(appointment);
        this.appointment = appointment;
    }

    public Appointment getAppointment() {
        return appointment;
    }
}
