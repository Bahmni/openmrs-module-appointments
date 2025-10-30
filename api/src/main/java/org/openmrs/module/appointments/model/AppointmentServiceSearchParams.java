package org.openmrs.module.appointments.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties
public class AppointmentServiceSearchParams {
    private String locationUuid;
    private String specialityUuid;
    private Boolean includeVoided = false;
    private Integer limit = 100;

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getSpecialityUuid() {
        return specialityUuid;
    }

    public void setSpecialityUuid(String specialityUuid) {
        this.specialityUuid = specialityUuid;
    }

    public Boolean getIncludeVoided() {
        return includeVoided;
    }

    public void setIncludeVoided(Boolean includeVoided) {
        this.includeVoided = includeVoided;
    }


    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
