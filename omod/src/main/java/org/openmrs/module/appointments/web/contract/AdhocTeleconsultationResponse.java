package org.openmrs.module.appointments.web.contract;

public class AdhocTeleconsultationResponse {
    private String uuid;
    private String link;

    public AdhocTeleconsultationResponse() {
    }

    public AdhocTeleconsultationResponse(String uuid, String link) {
        this.uuid = uuid;
        this.link = link;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
