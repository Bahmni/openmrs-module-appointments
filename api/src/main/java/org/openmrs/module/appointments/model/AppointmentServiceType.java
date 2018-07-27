package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;

public class AppointmentServiceType extends BaseOpenmrsData implements Serializable, Comparable<AppointmentServiceType> {

    private Integer id;
    private AppointmentService appointmentService;
    private String name;
    private Integer duration;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

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

    public AppointmentService getAppointmentService() {
        return appointmentService;
    }

    public void setAppointmentService(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Override
    public int compareTo(AppointmentServiceType o) {
        if (this.getName().compareTo(o.getName()) != 0) {
            return this.getName().compareTo(o.getName());
        } else if (this.getAppointmentService().getUuid().compareTo(o.getAppointmentService().getUuid()) != 0 ) {
            return this.getAppointmentService().getUuid().compareTo(o.getAppointmentService().getUuid());
        }
        return this.getDuration().compareTo(o.getDuration());
    }
}
