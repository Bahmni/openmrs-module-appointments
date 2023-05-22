package org.openmrs.module.appointments.service;


import org.openmrs.annotation.Authorized;
import org.openmrs.module.appointments.model.Appointment;
import org.springframework.transaction.annotation.Transactional;

public interface AppointmentReminderService {
    @Transactional(readOnly = true)
    Object sendAppointmentReminderSMS(Appointment appointment);
}