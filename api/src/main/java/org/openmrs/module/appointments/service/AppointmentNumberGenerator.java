package org.openmrs.module.appointments.service;

import org.openmrs.module.appointments.model.Appointment;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Transactional
public interface AppointmentNumberGenerator {
    String generateAppointmentNumber(@NotNull Appointment appointment);

    /**
     * Apply appointment numbers to a batch of appointments.
     * Default behavior: generate one number and apply to all (shared number strategy).*
     * @param appointments List of appointments to apply numbers to. Null or empty lists are ignored.
     */
    default void applyAppointmentNumbers(@NotNull List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }

        String sharedNumber = generateAppointmentNumber(appointments.get(0));
        appointments.forEach(appointment -> {
            if (appointment.getAppointmentNumber() == null) {
                appointment.setAppointmentNumber(sharedNumber);
            }
        });
    }
}
