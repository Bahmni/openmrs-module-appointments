package org.openmrs.module.appointments.model;

public enum AppointmentStatus {
    Requested("Requested", 0), WaitList("WaitList", 0), Scheduled("Scheduled", 1),Arrived("Arrived",2), CheckedIn("CheckedIn", 3), Completed("Completed", 4), Cancelled("Cancelled", 4), Missed("Missed", 4);

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
