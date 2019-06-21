package org.openmrs.module.appointments.web.util;

import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.Date;

public class AppointmentBuilder {

    private Appointment appointment;

    public AppointmentBuilder() {
        this.appointment = new Appointment();
    }

    public AppointmentBuilder withUuid(String uuid) {
        appointment.setUuid(uuid);
        return this;
    }

    public AppointmentBuilder withStartDateTime(Date startDateTime) {
        appointment.setStartDateTime(startDateTime);
        return this;
    }

    public AppointmentBuilder withEndDateTime(Date endDateTime) {
        appointment.setEndDateTime(endDateTime);
        return this;
    }

    public AppointmentBuilder withStatus(AppointmentStatus status) {
        appointment.setStatus(status);
        return this;
    }

    public AppointmentBuilder withAppointmentRecurringPattern(AppointmentRecurringPattern appointmentRecurringPattern) {
        appointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        return this;
    }

    public Appointment build() {
        return appointment;
    }

    public AppointmentBuilder withPatient(Patient patient) {
        appointment.setPatient(patient);
        return this;
    }

    public AppointmentBuilder withAppointmentKind(AppointmentKind appointmentKind) {
        appointment.setAppointmentKind(appointmentKind);
        return this;
    }
}
