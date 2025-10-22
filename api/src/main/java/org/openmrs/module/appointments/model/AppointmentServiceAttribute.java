package org.openmrs.module.appointments.model;

import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.BaseAttribute;

public class AppointmentServiceAttribute extends BaseAttribute<AppointmentServiceAttributeType, AppointmentServiceDefinition> implements Attribute<AppointmentServiceAttributeType, AppointmentServiceDefinition> {

    private Integer appointmentServiceAttributeId;

    @Override
    public Integer getId() {
        return getAppointmentServiceAttributeId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceAttributeId(id);
    }

    public AppointmentServiceDefinition getAppointmentService() {
        return getOwner();
    }

    public void setAppointmentService(AppointmentServiceDefinition appointmentService) {
        setOwner(appointmentService);
    }

    public Integer getAppointmentServiceAttributeId() {
        return appointmentServiceAttributeId;
    }

    public void setAppointmentServiceAttributeId(Integer appointmentServiceAttributeId) {
        this.appointmentServiceAttributeId = appointmentServiceAttributeId;
    }
}
