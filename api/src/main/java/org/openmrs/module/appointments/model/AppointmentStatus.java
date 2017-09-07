package org.openmrs.module.appointments.model;

public enum AppointmentStatus {
    Scheduled("Scheduled", 0), CheckedIn("CheckedIn", 1), Completed("Completed", 2), Cancelled("Cancelled", 2), Missed("Missed", 2);

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
