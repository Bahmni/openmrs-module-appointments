package org.openmrs.module.appointments.model;

public enum AppointmentKind {
    Scheduled("Scheduled"), WalkIn("WalkIn"), Virtual("Virtual");

    private final String value;

    AppointmentKind(String value) {
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}


