package org.openmrs.module.appointments.web.contract;

public class AppointmentUnavailabilityResponse {

    private String uuid;
    private String locationUuid;
    private String locationName;
    private String appointmentServiceUuid;
    private String appointmentServiceName;
    private String providerUuid;
    private String providerName;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Boolean voided;
    private String dateCreated;
    private String creatorName;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAppointmentServiceUuid() {
        return appointmentServiceUuid;
    }

    public void setAppointmentServiceUuid(String appointmentServiceUuid) {
        this.appointmentServiceUuid = appointmentServiceUuid;
    }

    public String getAppointmentServiceName() {
        return appointmentServiceName;
    }

    public void setAppointmentServiceName(String appointmentServiceName) {
        this.appointmentServiceName = appointmentServiceName;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
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

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
