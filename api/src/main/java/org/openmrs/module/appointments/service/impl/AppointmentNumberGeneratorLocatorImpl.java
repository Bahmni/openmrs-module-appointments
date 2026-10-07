package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.service.AppointmentNumberGenerator;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberGenerator;

public class AppointmentNumberGeneratorLocatorImpl implements org.openmrs.module.appointments.service.AppointmentNumberGeneratorLocator {
    AppointmentNumberGenerator appointmentNumberGenerator;
    RecurringAppointmentNumberGenerator recurringAppointmentNumberGenerator;

    public AppointmentNumberGeneratorLocatorImpl(AppointmentNumberGenerator appointmentNumberGenerator) {
        this.appointmentNumberGenerator = appointmentNumberGenerator;
    }

    public AppointmentNumberGeneratorLocatorImpl(AppointmentNumberGenerator appointmentNumberGenerator,
            RecurringAppointmentNumberGenerator recurringAppointmentNumberGenerator) {
        this.appointmentNumberGenerator = appointmentNumberGenerator;
        this.recurringAppointmentNumberGenerator = recurringAppointmentNumberGenerator;
    }

    @Override
    public AppointmentNumberGenerator retrieveAppointmentNumberGenerator() {
        return appointmentNumberGenerator;
    }

    @Override
    public void registerAppointmentNumberGenerator(AppointmentNumberGenerator generator) {
        if (generator != null) {
            this.appointmentNumberGenerator = generator;
        }
    }

    @Override
    public RecurringAppointmentNumberGenerator retrieveRecurringAppointmentNumberGenerator() {
        return recurringAppointmentNumberGenerator;
    }

    @Override
    public void registerRecurringAppointmentNumberGenerator(RecurringAppointmentNumberGenerator generator) {
        if (generator != null) {
            this.recurringAppointmentNumberGenerator = generator;
        }
    }

}
