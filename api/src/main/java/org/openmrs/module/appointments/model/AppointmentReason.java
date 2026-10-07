package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;

import java.io.Serializable;

public class AppointmentReason extends BaseOpenmrsData implements Serializable {

    private Integer appointmentReasonId;
    private Appointment appointment;
    private Concept concept;

    public AppointmentReason() {
        super();
    }

    @Override
    public Integer getId() {
        return getAppointmentReasonId();
    }

    @Override
    public void setId(Integer integer) {
        setAppointmentReasonId(integer);
    }

    public Integer getAppointmentReasonId() {
        return appointmentReasonId;
    }

    public void setAppointmentReasonId(Integer appointmentReasonId) {
        this.appointmentReasonId = appointmentReasonId;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }
}
