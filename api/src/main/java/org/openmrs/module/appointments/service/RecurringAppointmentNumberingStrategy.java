package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;

import java.util.List;

public interface RecurringAppointmentNumberingStrategy {
    void applyAppointmentNumbers(List<Appointment> appointments,
                                  AppointmentRecurringPattern pattern);
}
