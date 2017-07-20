package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;

import java.io.Serializable;
import java.sql.Time;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;


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
    private Set<ServiceWeeklyAvailability> weeklyAvailability;
    private Set<AppointmentServiceType> serviceTypes;


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

    public Set<ServiceWeeklyAvailability> getWeeklyAvailability(boolean includeVoided) {
        if(includeVoided || this.weeklyAvailability == null)
            return this.weeklyAvailability;

        Set<ServiceWeeklyAvailability> nonVoided = new LinkedHashSet<>(this.weeklyAvailability);
        Iterator<ServiceWeeklyAvailability> i = nonVoided.iterator();
        while (i.hasNext()) {
            ServiceWeeklyAvailability availability = i.next();
            if (availability.getVoided()) {
                i.remove();
            }
        }
        return nonVoided;
    }

    public Set<ServiceWeeklyAvailability> getWeeklyAvailability() {
        return getWeeklyAvailability(false);
    }

    public void setWeeklyAvailability(
            Set<ServiceWeeklyAvailability> weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }

    public Set<AppointmentServiceType> getServiceTypes() {
        if (serviceTypes == null) {
            serviceTypes = new LinkedHashSet<>();
        }
        return serviceTypes;
    }

    public void setServiceTypes(Set<AppointmentServiceType> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }
}
