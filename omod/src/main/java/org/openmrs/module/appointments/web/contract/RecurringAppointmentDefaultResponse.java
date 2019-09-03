package org.openmrs.module.appointments.web.contract;

public class RecurringAppointmentDefaultResponse {

    private AppointmentDefaultResponse appointmentDefaultResponse;
    private RecurringPattern recurringPattern;

    public AppointmentDefaultResponse getAppointmentDefaultResponse() {
        return appointmentDefaultResponse;
    }

    public RecurringPattern getRecurringPattern() {
        return recurringPattern;
    }

    public void setAppointmentDefaultResponse(AppointmentDefaultResponse appointmentDefaultResponse) {
        this.appointmentDefaultResponse = appointmentDefaultResponse;
    }

    public void setRecurringPattern(RecurringPattern recurringPattern) {
        this.recurringPattern = recurringPattern;
    }
}
