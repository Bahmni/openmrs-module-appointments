package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AppointmentsSummary {
    private AppointmentServiceDefaultResponse appointmentService;
    private Map<String, AppointmentCount> appointmentCountList;

    @JsonCreator
    public AppointmentsSummary(@JsonProperty("appointmentService")AppointmentServiceDefaultResponse appointmentService,
                               @JsonProperty("appointmentCountList") Map appointmentCountList) {
        this.appointmentService = appointmentService;
        this.appointmentCountList = appointmentCountList;
    }
    public AppointmentServiceDefaultResponse getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentServiceDefaultResponse appointmentService) {
        this.appointmentService = appointmentService;
    }

    public Map<String, AppointmentCount> getAppointmentCountList() {
        return appointmentCountList;
    }

    public void setAppointmentCountList(Map<String, AppointmentCount> appointmentCountList) {
        this.appointmentCountList = appointmentCountList;
    }
}
