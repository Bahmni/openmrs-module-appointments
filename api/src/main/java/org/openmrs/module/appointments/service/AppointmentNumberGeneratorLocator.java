package org.openmrs.module.appointments.service;

public interface AppointmentNumberGeneratorLocator {
    AppointmentNumberGenerator retrieveAppointmentNumberGenerator();

    void registerAppointmentNumberGenerator(AppointmentNumberGenerator generator);
}
