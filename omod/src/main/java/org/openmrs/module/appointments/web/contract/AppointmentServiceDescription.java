package org.openmrs.module.appointments.web.contract;

import java.sql.Time;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.Size;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentServiceDescription {
    @Size(min = 1)
    private String name;
    private String description;
    private String specialityUuid;
    private Time startTime;
    private Time endTime;
    private Integer maxAppointmentsLimit;
    private Integer durationMins;
    private String locationUuid;
    private String uuid;
    private String color;
    private String initialAppointmentStatus;
    private List<ServiceWeeklyAvailabilityDescription> weeklyAvailability;
    private Set<AppointmentServiceTypeDescription> serviceTypes;

    public Set<AppointmentServiceTypeDescription> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(Set<AppointmentServiceTypeDescription> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialityUuid() {
        return specialityUuid;
    }

    public void setSpecialityUuid(String specialityUuid) {
        this.specialityUuid = specialityUuid;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxAppointmentsLimit() {
        return maxAppointmentsLimit;
    }

    public void setMaxAppointmentsLimit(Integer maxAppointmentsLimit) {
        this.maxAppointmentsLimit = maxAppointmentsLimit;
    }

    public Integer getDurationMins() {
        return durationMins;
    }

    public void setDurationMins(Integer durationMins) {
        this.durationMins = durationMins;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ServiceWeeklyAvailabilityDescription> getWeeklyAvailability() {
        return weeklyAvailability;
    }

    public void setWeeklyAvailability(List<ServiceWeeklyAvailabilityDescription> weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInitialAppointmentStatus() {
        return initialAppointmentStatus;
    }

    public void setInitialAppointmentStatus(String initialAppointmentStatus) {
        this.initialAppointmentStatus = initialAppointmentStatus;
    }
}
