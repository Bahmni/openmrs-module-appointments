package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecurringAppointmentService {

    @Transactional
    List<Appointment> validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern,
                                      List<Appointment> appointments);

    @Transactional
    List<Appointment> validateAndUpdate(Appointment appointment, String clientTimeZone);
}
