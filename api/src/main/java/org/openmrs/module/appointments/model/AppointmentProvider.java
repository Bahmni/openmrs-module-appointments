package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Provider;

import java.io.Serializable;

public class AppointmentProvider extends BaseOpenmrsData implements Serializable {

    private Integer appointmentProviderId;
    private Appointment appointment;
    private Provider provider;
    private AppointmentProviderResponse response;
    private String comments;
    private Boolean voided;

    public AppointmentProvider(AppointmentProvider appointmentProvider) {
        this.appointment = appointmentProvider.getAppointment();
        this.provider = appointmentProvider.getProvider();
        this.response = appointmentProvider.getResponse();
        this.comments = appointmentProvider.getComments();
        this.voided = appointmentProvider.getVoided();
    }

    public AppointmentProvider() {
    }

    @Override
    public Integer getId() {
        return getAppointmentProviderId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentProviderId(id);
    }

    public Integer getAppointmentProviderId() {
        return appointmentProviderId;
    }

    public void setAppointmentProviderId(Integer appointmentProviderId) {
        this.appointmentProviderId = appointmentProviderId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public AppointmentProviderResponse getResponse() {
        return response;
    }

    public void setResponse(AppointmentProviderResponse response) {
        this.response = response;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    @Override
    public Boolean getVoided() {
        return voided;
    }

    @Override
    public void setVoided(Boolean voided) {
        this.voided = voided;
    }
}
