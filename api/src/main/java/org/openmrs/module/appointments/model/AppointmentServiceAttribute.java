package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;

public class AppointmentServiceAttribute extends BaseOpenmrsData implements Serializable {

    private Integer appointmentServiceAttributeId;
    private AppointmentServiceDefinition appointmentService;
    private AppointmentServiceAttributeType attributeType;
    private String valueReference;

    public Integer getAppointmentServiceAttributeId() {
        return appointmentServiceAttributeId;
    }

    public void setAppointmentServiceAttributeId(Integer appointmentServiceAttributeId) {
        this.appointmentServiceAttributeId = appointmentServiceAttributeId;
    }

    @Override
    public Integer getId() {
        return getAppointmentServiceAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceAttributeId(id);
    }

    public AppointmentServiceDefinition getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentServiceDefinition appointmentService) {
        this.appointmentService = appointmentService;
    }

    public AppointmentServiceAttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AppointmentServiceAttributeType attributeType) {
        this.attributeType = attributeType;
    }

    public String getValueReference() {
        return valueReference;
    }

    public void setValueReference(String valueReference) {
        this.valueReference = valueReference;
    }

    public Object getValue() {
        return valueReference;
    }

    public void setValue(Object value) {
        if (value == null) {
            this.valueReference = null;
        } else {
            this.valueReference = value.toString();
        }
    }
}
