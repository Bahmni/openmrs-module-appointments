package org.openmrs.module.appointments.web.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Time;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentServicePayload {
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
    private List<ServiceWeeklyAvailabilityPayload> weeklyAvailability;

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

    public List<ServiceWeeklyAvailabilityPayload> getWeeklyAvailability() {
        return weeklyAvailability;
    }

    public void setWeeklyAvailability(List<ServiceWeeklyAvailabilityPayload> weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }
}
