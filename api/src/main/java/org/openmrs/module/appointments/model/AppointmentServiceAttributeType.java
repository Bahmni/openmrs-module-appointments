package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsMetadata;

import java.io.Serializable;

public class AppointmentServiceAttributeType extends BaseOpenmrsMetadata implements Serializable {

    private Integer appointmentServiceAttributeTypeId;
    private String datatype;
    private String datatypeConfig;
    private String preferredHandler;
    private String handlerConfig;
    private Integer minOccurs;
    private Integer maxOccurs;

    public Integer getAppointmentServiceAttributeTypeId() {
        return appointmentServiceAttributeTypeId;
    }

    public void setAppointmentServiceAttributeTypeId(Integer appointmentServiceAttributeTypeId) {
        this.appointmentServiceAttributeTypeId = appointmentServiceAttributeTypeId;
    }

    @Override
    public Integer getId() {
        return getAppointmentServiceAttributeTypeId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceAttributeTypeId(id);
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getDatatypeConfig() {
        return datatypeConfig;
    }

    public void setDatatypeConfig(String datatypeConfig) {
        this.datatypeConfig = datatypeConfig;
    }

    public String getPreferredHandler() {
        return preferredHandler;
    }

    public void setPreferredHandler(String preferredHandler) {
        this.preferredHandler = preferredHandler;
    }

    public String getHandlerConfig() {
        return handlerConfig;
    }

    public void setHandlerConfig(String handlerConfig) {
        this.handlerConfig = handlerConfig;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }
}
