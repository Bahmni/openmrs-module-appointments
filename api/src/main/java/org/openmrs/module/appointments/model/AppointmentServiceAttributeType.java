package org.openmrs.module.appointments.model;

import org.openmrs.attribute.AttributeType;
import org.openmrs.attribute.BaseAttributeType;

public class AppointmentServiceAttributeType extends BaseAttributeType<AppointmentServiceDefinition> implements AttributeType<AppointmentServiceDefinition> {

    private Integer appointmentServiceAttributeTypeId;

    @Override
    public Integer getId() {
        return getAppointmentServiceAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceAttributeTypeId(id);
    }

    public Integer getAppointmentServiceAttributeTypeId() {
        return appointmentServiceAttributeTypeId;
    }

    public void setAppointmentServiceAttributeTypeId(Integer appointmentServiceAttributeTypeId) {
        this.appointmentServiceAttributeTypeId = appointmentServiceAttributeTypeId;
    }
}
