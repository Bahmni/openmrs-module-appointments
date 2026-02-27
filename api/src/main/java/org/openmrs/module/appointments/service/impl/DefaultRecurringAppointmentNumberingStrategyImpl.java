package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentNumberGenerator;
import org.openmrs.module.appointments.service.AppointmentNumberGeneratorLocator;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberingStrategy;
import java.util.List;

public class DefaultRecurringAppointmentNumberingStrategyImpl
        implements RecurringAppointmentNumberingStrategy {

    private Log log = LogFactory.getLog(this.getClass());

    private AppointmentNumberGeneratorLocator appointmentNumberGeneratorLocator;

    public DefaultRecurringAppointmentNumberingStrategyImpl(
            AppointmentNumberGeneratorLocator appointmentNumberGeneratorLocator) {
        this.appointmentNumberGeneratorLocator = appointmentNumberGeneratorLocator;
    }

    @Override
    public void applyAppointmentNumbers(List<Appointment> appointments,
                                         AppointmentRecurringPattern pattern) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }

        // Check if this is an update scenario (appointments already have numbers)
        String existingNumber = appointments.stream()
                .map(Appointment::getAppointmentNumber)
                .filter(number -> number != null)
                .findFirst()
                .orElse(null);

        if (existingNumber != null) {
            // Update scenario - reuse existing number for any new appointments
            appointments.forEach(appointment -> {
                if (appointment.getAppointmentNumber() == null) {
                    appointment.setAppointmentNumber(existingNumber);
                }
            });
            return;
        }

        // New series scenario - generate shared number
        AppointmentNumberGenerator appointmentNumberGenerator =
                appointmentNumberGeneratorLocator.retrieveAppointmentNumberGenerator();

        if (appointmentNumberGenerator == null) {
            log.warn("Can not generate appointment number. No generator found");
            return;
        }

        String sharedNumber = appointmentNumberGenerator
                .generateAppointmentNumber(appointments.get(0));

        // Apply the same number to all appointments in the series
        appointments.forEach(appointment -> {
            if (appointment.getAppointmentNumber() == null) {
                appointment.setAppointmentNumber(sharedNumber);
            }
        });
    }
}
