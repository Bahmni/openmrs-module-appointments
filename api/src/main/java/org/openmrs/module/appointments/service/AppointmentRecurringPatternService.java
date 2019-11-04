package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AppointmentRecurringPatternService {

    @Transactional
    AppointmentRecurringPattern validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern);

    @Transactional
    Appointment update(AppointmentRecurringPattern appointmentRecurringPattern, List<Appointment> updatedAppointments);

    @Transactional
    List<Appointment> changeStatus(Appointment appointment, String toStatus, String clientTimeZone);

    @Transactional
    AppointmentRecurringPattern update(AppointmentRecurringPattern appointmentRecurringPattern, Appointment editedAppointment);
}
