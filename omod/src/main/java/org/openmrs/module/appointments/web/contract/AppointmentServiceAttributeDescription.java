package org.openmrs.module.appointments.web.contract;

public class AppointmentServiceAttributeDescription {
    private String uuid;
    private String attributeTypeUuid;
    private String value;
    private Boolean voided;
    private String voidReason;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAttributeTypeUuid() {
        return attributeTypeUuid;
    }

    public void setAttributeTypeUuid(String attributeTypeUuid) {
        this.attributeTypeUuid = attributeTypeUuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getVoided() {
        return voided;
    }

    public void setVoided(Boolean voided) {
        this.voided = voided;
    }

    public String getVoidReason() {
        return voidReason;
    }

    public void setVoidReason(String voidReason) {
        this.voidReason = voidReason;
    }
}
