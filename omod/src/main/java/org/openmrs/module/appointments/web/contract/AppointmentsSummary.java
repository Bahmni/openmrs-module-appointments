package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class AppointmentsSummary {
    private AppointmentServiceDefaultResponse appointmentService;
    private List appointmentCountList;

    @JsonCreator
    public AppointmentsSummary(@JsonProperty("appointmentService")AppointmentServiceDefaultResponse appointmentService,
                               @JsonProperty("appointmentCountList") List appointmentCountList) {
        this.appointmentService = appointmentService;
        this.appointmentCountList = appointmentCountList;
    }
    public AppointmentServiceDefaultResponse getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentServiceDefaultResponse appointmentService) {
        this.appointmentService = appointmentService;
    }

    public List getAppointmentCountList() {
        return appointmentCountList;
    }

    public void setAppointmentCountList(List appointmentCountList) {
        this.appointmentCountList = appointmentCountList;
    }
}
