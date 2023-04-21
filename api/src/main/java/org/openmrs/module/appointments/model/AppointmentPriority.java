package org.openmrs.module.appointments.model;

public enum AppointmentPriority {
    AsNeeded("AsNeeded"), Routine("Routine"), Emergency("Emergency"), Invalid("Invalid");

    private final String value;

    AppointmentPriority(String value) {
        this.value = value;
    }
}


