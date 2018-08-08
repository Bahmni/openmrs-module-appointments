package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

public class AppointmentsSummary {
    private AppointmentServiceDefaultResponse appointmentService;
    private Map<String, DailyAppointmentServiceSummary> appointmentCountMap;

    @JsonCreator
    public AppointmentsSummary(@JsonProperty("appointmentService")AppointmentServiceDefaultResponse appointmentService,
                               @JsonProperty("appointmentCountMap") Map appointmentCountMap) {
        this.appointmentService = appointmentService;
        this.appointmentCountMap = appointmentCountMap;
    }
    public AppointmentServiceDefaultResponse getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentServiceDefaultResponse appointmentService) {
        this.appointmentService = appointmentService;
    }

    public Map<String, DailyAppointmentServiceSummary> getAppointmentCountMap() {
        return appointmentCountMap;
    }

    public void setAppointmentCountMap(Map<String, DailyAppointmentServiceSummary> appointmentCountMap) {
        this.appointmentCountMap = appointmentCountMap;
    }
}
