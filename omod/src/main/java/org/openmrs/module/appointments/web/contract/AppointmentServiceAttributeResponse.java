package org.openmrs.module.appointments.web.contract;

public class AppointmentServiceAttributeResponse {
    private String uuid;
    private String attributeType;
    private String attributeTypeUuid;
    private Object value;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttributeTypeUuid() {
        return attributeTypeUuid;
    }

    public void setAttributeTypeUuid(String attributeTypeUuid) {
        this.attributeTypeUuid = attributeTypeUuid;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
