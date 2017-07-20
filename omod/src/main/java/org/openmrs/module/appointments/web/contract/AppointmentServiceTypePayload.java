package org.openmrs.module.appointments.web.contract;

public class AppointmentServiceTypePayload {
    private String name;
    private Integer duration;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }
}
