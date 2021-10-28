package org.openmrs.module.appointments.model;

import org.openmrs.module.appointments.notification.NotificationResult;

import java.util.List;

public class AdhocTeleconsultationResponse {
    private String uuid;
    private String link;
    private List<NotificationResult> notificationResults;

    public AdhocTeleconsultationResponse() {
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

    public List<NotificationResult> getNotificationResults() {
        return notificationResults;
    }

    public void setNotificationResults(List<NotificationResult> notificationResults) {
        this.notificationResults = notificationResults;
    }
}
