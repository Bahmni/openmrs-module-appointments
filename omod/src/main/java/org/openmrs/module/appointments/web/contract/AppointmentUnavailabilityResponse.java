package org.openmrs.module.appointments.web.contract;

public class AppointmentUnavailabilityResponse {

    private String uuid;
    private AppointmentUnavailabilityLocationResponse location;
    private AppointmentUnavailabilityServiceResponse service;
    private AppointmentUnavailabilityProviderResponse provider;
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

    public AppointmentUnavailabilityLocationResponse getLocation() {
        return location;
    }

    public void setLocation(AppointmentUnavailabilityLocationResponse location) {
        this.location = location;
    }

    public AppointmentUnavailabilityServiceResponse getService() {
        return service;
    }

    public void setService(AppointmentUnavailabilityServiceResponse service) {
        this.service = service;
    }

    public AppointmentUnavailabilityProviderResponse getProvider() {
        return provider;
    }

    public void setProvider(AppointmentUnavailabilityProviderResponse provider) {
        this.provider = provider;
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
