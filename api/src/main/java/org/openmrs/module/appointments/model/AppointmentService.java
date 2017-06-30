package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;

import java.io.Serializable;
import java.sql.Time;

public class AppointmentService extends BaseOpenmrsData implements Serializable {
    private Integer appointmentServiceId;
    private String name;
    private String description;
    private Speciality speciality;
    private Time startTime;
    private Time endTime;
    private Integer maxAppointmentsLimit;
    private Integer durationMins;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(Integer durationMins) {
        this.durationMins = durationMins;
    }

    public Integer getMaxAppointmentsLimit() {
        return maxAppointmentsLimit;
    }

    public void setMaxAppointmentsLimit(Integer maxAppointmentsLimit) {
        this.maxAppointmentsLimit = maxAppointmentsLimit;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Speciality getSpeciality() {
        return speciality;
    }

    public void setSpeciality(Speciality speciality) {
        this.speciality = speciality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAppointmentServiceId() {
        return appointmentServiceId;
    }

    public void setAppointmentServiceId(Integer appointmentServiceId) {
        this.appointmentServiceId = appointmentServiceId;
    }

    @Override
    public Integer getId() {
        return getAppointmentServiceId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentServiceId(id);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
