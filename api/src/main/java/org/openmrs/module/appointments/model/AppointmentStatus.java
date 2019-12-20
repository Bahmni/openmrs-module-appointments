package org.openmrs.module.appointments.model;

public enum AppointmentStatus {
    Requested("Requested", 0), Scheduled("Scheduled", 1), CheckedIn("CheckedIn", 2), Completed("Completed", 3), Cancelled("Cancelled", 3), Missed("Missed", 3);

    private final String value;
    private final int sequence;

    AppointmentStatus(String value, int sequence) {
        this.value = value;
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }
}
