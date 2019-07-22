package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface RecurringAppointmentService {

    @Transactional
    List<Appointment> validateAndSave(AppointmentRecurringPattern appointmentRecurringPattern);

    @Transactional
    List<Appointment> validateAndUpdate(Appointment appointment, String clientTimeZone);

    @Transactional
    void changeStatus(Appointment appointment, String toStatus, Date onDate, String clientTimeZone);

    @Transactional
    AppointmentRecurringPattern validateAndUpdate(AppointmentRecurringPattern appointmentRecurringPattern);
}
