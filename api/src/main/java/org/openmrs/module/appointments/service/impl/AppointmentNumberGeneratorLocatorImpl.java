package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.service.AppointmentNumberGenerator;

public class AppointmentNumberGeneratorLocatorImpl implements org.openmrs.module.appointments.service.AppointmentNumberGeneratorLocator {
    AppointmentNumberGenerator appointmentNumberGenerator;

    public AppointmentNumberGeneratorLocatorImpl(AppointmentNumberGenerator appointmentNumberGenerator) {
        this.appointmentNumberGenerator = appointmentNumberGenerator;
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

}
