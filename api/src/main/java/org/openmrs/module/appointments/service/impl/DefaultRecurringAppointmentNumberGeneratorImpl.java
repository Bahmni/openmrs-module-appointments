package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentNumberGenerator;
import org.openmrs.module.appointments.service.AppointmentNumberGeneratorLocator;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberGenerator;
import java.util.List;

public class DefaultRecurringAppointmentNumberGeneratorImpl
        implements RecurringAppointmentNumberGenerator {

    private Log log = LogFactory.getLog(this.getClass());

    private AppointmentNumberGeneratorLocator appointmentNumberGeneratorLocator;

    public DefaultRecurringAppointmentNumberGeneratorImpl(
            AppointmentNumberGeneratorLocator appointmentNumberGeneratorLocator) {
        this.appointmentNumberGeneratorLocator = appointmentNumberGeneratorLocator;
    }

    @Override
    public void setAppointmentNumbers(List<Appointment> appointments,
                                         AppointmentRecurringPattern pattern) {
        if (appointments == null || appointments.isEmpty()) {
            return;
        }

        String existingNumber = appointments.stream()
                .map(Appointment::getAppointmentNumber)
                .filter(number -> number != null)
                .findFirst()
                .orElse(null);

        if (existingNumber != null) {
            appointments.forEach(appointment -> {
                if (appointment.getAppointmentNumber() == null) {
                    appointment.setAppointmentNumber(existingNumber);
                }
            });
            return;
        }

        AppointmentNumberGenerator appointmentNumberGenerator =
                appointmentNumberGeneratorLocator.retrieveAppointmentNumberGenerator();

        if (appointmentNumberGenerator == null) {
            log.warn("Can not generate appointment number. No generator found");
            return;
        }

        String sharedNumber = appointmentNumberGenerator
                .generateAppointmentNumber(appointments.get(0));

        appointments.forEach(appointment -> {
            if (appointment.getAppointmentNumber() == null) {
                appointment.setAppointmentNumber(sharedNumber);
            }
        });
    }
}
