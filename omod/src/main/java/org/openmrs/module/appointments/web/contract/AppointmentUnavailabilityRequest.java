package org.openmrs.module.appointments.web.contract;

import javax.validation.constraints.NotNull;

public class AppointmentUnavailabilityRequest {

    @NotNull(message = "locationUuid is required")
    private String locationUuid;

    private String appointmentServiceUuid;

    private String providerUuid;

    @NotNull(message = "startDate is required")
    private String startDate;

    @NotNull(message = "startTime is required")
    private String startTime;

    @NotNull(message = "endDate is required")
    private String endDate;

    @NotNull(message = "endTime is required")
    private String endTime;

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getAppointmentServiceUuid() {
        return appointmentServiceUuid;
    }

    public void setAppointmentServiceUuid(String appointmentServiceUuid) {
        this.appointmentServiceUuid = appointmentServiceUuid;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
