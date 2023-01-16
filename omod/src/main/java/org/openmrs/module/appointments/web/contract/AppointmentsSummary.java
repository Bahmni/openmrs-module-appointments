package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;
import java.util.List;
import java.util.Date;

public class AppointmentsSummary {
    private AppointmentServiceDefaultResponse appointmentService;
    private Map<String, DailyAppointmentServiceSummary> appointmentCountMap;

    private List<DailyAppointmentServiceSummary> services;


    private String appointmentDate;



    private Integer totalServiceCount;

    @JsonCreator
    public AppointmentsSummary(@JsonProperty("appointmentService")AppointmentServiceDefaultResponse appointmentService,
                               @JsonProperty("appointmentCountMap") Map appointmentCountMap) {
        this.appointmentService = appointmentService;
        this.appointmentCountMap = appointmentCountMap;
    }
    @JsonCreator
    public AppointmentsSummary(@JsonProperty("services") List services,
                               @JsonProperty("appointmentDate") String appointmentDate,
                               @JsonProperty("dailAappointmentCount") Integer totalServiceCount) {
        this.services = services;
        this.appointmentDate = appointmentDate;
        this.totalServiceCount = totalServiceCount;

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

    public List<DailyAppointmentServiceSummary> getServices() {
        return services;
    }

    public void setServices(List<DailyAppointmentServiceSummary> dailyAppointmentList) {
        this.services = dailyAppointmentList;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Integer gettotalServiceCount() {
        return totalServiceCount;
    }

    public void settotalServiceCount(Integer totalServiceCount) {
        this.totalServiceCount = totalServiceCount;
    }

}
