package org.openmrs.module.appointments.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties
public class AppointmentSearchRequestModel {
    private List<String> providerUuids;
    private List<String> serviceUuids;
    private List<String> serviceTypeUuids;
    private List<String> locationUuids;
    private List<String> patientUuids;
    private String status;
    private List<String> priorities;
    private String appointmentKind;
    private Boolean withoutDates = false;

    public List<String> getProviderUuids() {
        return providerUuids;
    }

    public void setProviderUuids(List<String> providerUuids) {
        this.providerUuids = providerUuids;
    }

    public List<String> getServiceUuids() {
        return serviceUuids;
    }

    public void setServiceUuids(List<String> serviceUuids) {
        this.serviceUuids = serviceUuids;
    }

    public List<String> getServiceTypeUuids() {
        return serviceTypeUuids;
    }

    public void setServiceTypeUuids(List<String> serviceTypeUuids) {
        this.serviceTypeUuids = serviceTypeUuids;
    }

    public List<String> getLocationUuids() {
        return locationUuids;
    }

    public void setLocationUuids(List<String> locationUuids) {
        this.locationUuids = locationUuids;
    }

    public List<String> getPatientUuids() {
        return patientUuids;
    }

    public void setPatientUuids(List<String> patientUuids) {
        this.patientUuids = patientUuids;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentKind() {
        return appointmentKind;
    }

    public void setAppointmentKind(String appointmentKind) {
        this.appointmentKind = appointmentKind;
    }

    public Boolean isWithoutDates() {
        return withoutDates;
    }

    public void setWithoutDates(Boolean withoutDates) {
        this.withoutDates = withoutDates;
    }

    public List<String> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<String> priorities) {
        this.priorities = priorities;
    }
}
