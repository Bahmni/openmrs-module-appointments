package org.openmrs.module.appointments.model;

public enum AppointmentStatus {
    Scheduled("scheduled"), CheckedIn("CheckedIn"), Started("Started"),
    Completed("Completed"), Cancelled("Cancelled"), Missed("Missed");

    private final String value;

    AppointmentStatus(String value) {
        this.value = value;
    }
}
