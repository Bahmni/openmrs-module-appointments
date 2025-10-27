package org.openmrs.module.appointments.web.contract;

public class AppointmentServiceAttributeTypeResponse {
    private String uuid;
    private String name;
    private String description;
    private String format;
    private String datatype;
    private Integer minOccurs;
    private Integer maxOccurs;
    private Boolean retired;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
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

    public Boolean getRetired() {
        return retired;
    }

    public void setRetired(Boolean retired) {
        this.retired = retired;
    }
}
