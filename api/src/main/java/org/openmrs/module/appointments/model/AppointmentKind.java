package org.openmrs.module.appointments.model;

public enum AppointmentKind {
    Scheduled("Scheduled"), WalkIn("WalkIn");

    private final String value;

    AppointmentKind(String value) {
        this.value = value;
    }
}


