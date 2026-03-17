package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface RecurringAppointmentNumberGenerator {
    void setAppointmentNumbers(List<Appointment> appointments,
                                  AppointmentRecurringPattern pattern);
}
