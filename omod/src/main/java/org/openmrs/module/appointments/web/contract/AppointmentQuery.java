package org.openmrs.module.appointments.web.contract;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties
public class AppointmentQuery {
    private String providerUuid;
    private String serviceUuid;
    private String serviceTypeUuid;
    private String locationUuid;
    private String patientUuid;
    private String status;
    private String appointmentKind;

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
