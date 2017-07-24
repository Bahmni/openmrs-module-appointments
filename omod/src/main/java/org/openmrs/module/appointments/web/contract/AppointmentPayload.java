package org.openmrs.module.appointments.web.contract;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties
public class AppointmentPayload {
    private String providerUuid;
    private String appointmentNumber;
    private String serviceUuid;
    private String serviceTypeUuid;
    private String locationUuid;
    private String patientUuid;
    private String status;
    private Date startDateTime;
    private Date endDateTime;
    private String appointmentsKind;
    private String comments;
    private String uuid;

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAppointmentsKind() {
        return appointmentsKind;
    }

    public void setAppointmentsKind(String appointmentsKind) {
        this.appointmentsKind = appointmentsKind;
    }

    public Date getEndDateTime() { return endDateTime;}

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

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
}
