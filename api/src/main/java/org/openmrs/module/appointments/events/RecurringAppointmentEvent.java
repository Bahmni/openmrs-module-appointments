package org.openmrs.module.appointments.events;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;

public class RecurringAppointmentEvent extends AppointmentEvent {
    private AppointmentRecurringPattern appointmentRecurringPattern;

    public RecurringAppointmentEvent(AppointmentEventType eventType,AppointmentRecurringPattern appointmentRecurringPattern) {
        super(eventType);
        this.appointmentRecurringPattern=appointmentRecurringPattern;
        this.payloadId=appointmentRecurringPattern.getAppointments().iterator().next().getUuid();
    }
    public AppointmentRecurringPattern getAppointmentRecurringPattern() {
        return appointmentRecurringPattern;
    }

}

