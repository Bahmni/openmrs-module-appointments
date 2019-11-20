package org.openmrs.module.appointments.web.contract;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties
public class AppointmentRequest {
    private String appointmentNumber;
    private String uuid;
    private String patientUuid;
    private String serviceUuid;
    private String serviceTypeUuid;
    private String providerUuid;
    private String locationUuid;
    private Date startDateTime;
    private Date endDateTime;
    private String status;
    private String appointmentKind;
    private String comments;
    private List<AppointmentProviderDetail> providers = new ArrayList<>();

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getServiceUuid() {
        return serviceUuid;
    }

    public void setServiceUuid(String serviceUuid) {
        this.serviceUuid = serviceUuid;
    }

    public String getServiceTypeUuid() {
        return serviceTypeUuid;
    }

    public void setServiceTypeUuid(String serviceTypeUuid) {
        this.serviceTypeUuid = serviceTypeUuid;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public String getAppointmentKind() {
        return appointmentKind;
    }

    public void setAppointmentKind(String appointmentKind) {
        this.appointmentKind = appointmentKind;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<AppointmentProviderDetail> getProviders() {
        return providers;
    }

    public void setProviders(List<AppointmentProviderDetail> providers) {
        this.providers = providers;
    }
}
