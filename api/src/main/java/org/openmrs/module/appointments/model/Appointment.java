package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;

import java.io.Serializable;
import java.util.Date;

public class Appointment  extends BaseOpenmrsData implements Serializable {
    private Integer appointmentId;
    private Provider provider;
    private String appointmentNumber;
    private Location location;
    private Patient patient;
    private AppointmentService service;
    private AppointmentServiceType serviceType;
    private String status;
    private Date startDateTime;
    private Date endDateTime;
    private String appointmentsKind;
    private String comments;

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

    public Date getEndDateTime() {
        return endDateTime;
    }

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

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    @Override
    public Integer getId() {
        return getAppointmentId();
    }

    @Override
    public void setId(Integer integer) {
        setAppointmentId(integer);
    }

    public AppointmentService getService() {
        return service;
    }

    public void setService(AppointmentService service) {
        this.service = service;
    }

    public AppointmentServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(AppointmentServiceType serviceType) {
        this.serviceType = serviceType;
    }
}

