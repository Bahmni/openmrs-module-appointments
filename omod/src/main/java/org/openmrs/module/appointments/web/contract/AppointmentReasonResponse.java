package org.openmrs.module.appointments.web.contract;

public class AppointmentReasonResponse {
    private String conceptUuid;
    private String name;

    public AppointmentReasonResponse() {
    }

    public AppointmentReasonResponse(String conceptUuid, String name) {
        this.conceptUuid = conceptUuid;
        this.name = name;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
