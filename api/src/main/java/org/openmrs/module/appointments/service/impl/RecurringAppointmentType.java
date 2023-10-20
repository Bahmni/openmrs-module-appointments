package org.openmrs.module.appointments.service.impl;

public enum RecurringAppointmentType {
    DAY, WEEK;
    public String getTypeAsString() {
        return this.name();
    }
}
