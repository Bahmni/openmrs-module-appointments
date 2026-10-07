package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

@Transactional
public interface AppointmentNumberGenerator {
    String generateAppointmentNumber(@NotNull Appointment appointment);
}
