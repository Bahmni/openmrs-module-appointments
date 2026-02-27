package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.service.RecurringAppointmentNumberingStrategy;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberingStrategyLocator;

public class RecurringAppointmentNumberingStrategyLocatorImpl
        implements RecurringAppointmentNumberingStrategyLocator {

    private RecurringAppointmentNumberingStrategy recurringAppointmentNumberingStrategy;

    public RecurringAppointmentNumberingStrategyLocatorImpl(
            RecurringAppointmentNumberingStrategy recurringAppointmentNumberingStrategy) {
        this.recurringAppointmentNumberingStrategy = recurringAppointmentNumberingStrategy;
    }

    @Override
    public RecurringAppointmentNumberingStrategy retrieveRecurringAppointmentNumberingStrategy() {
        return recurringAppointmentNumberingStrategy;
    }

    @Override
    public void registerRecurringAppointmentNumberingStrategy(
            RecurringAppointmentNumberingStrategy strategy) {
        if (strategy != null) {
            this.recurringAppointmentNumberingStrategy = strategy;
        }
    }
}
