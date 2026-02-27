package org.openmrs.module.appointments.service;

public interface RecurringAppointmentNumberingStrategyLocator {
    RecurringAppointmentNumberingStrategy retrieveRecurringAppointmentNumberingStrategy();

    void registerRecurringAppointmentNumberingStrategy(RecurringAppointmentNumberingStrategy strategy);
}
