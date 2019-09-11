package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties
public class RecurringAppointmentRequest {

    private AppointmentRequest appointmentRequest;
    private RecurringPattern recurringPattern;
    private Boolean applyForAll= false;
    private String timeZone;

    public AppointmentRequest getAppointmentRequest() {
        return appointmentRequest;
    }

    public void setAppointmentRequest(AppointmentRequest appointmentRequest) {
        this.appointmentRequest = appointmentRequest;
    }

    public RecurringPattern getRecurringPattern() {
        return recurringPattern;
    }

    public void setRecurringPattern(RecurringPattern recurringPattern) {
        this.recurringPattern = recurringPattern;
    }

    public Boolean getApplyForAll() {
        return applyForAll;
    }

    public void setApplyForAll(Boolean applyForAll) {
        this.applyForAll = applyForAll;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
