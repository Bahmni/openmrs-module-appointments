package org.openmrs.module.appointments.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Location;
import org.openmrs.Provider;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class AppointmentUnavailability extends BaseOpenmrsData implements Serializable {

    private Integer appointmentUnavailabilityId;
    private Location location;
    private AppointmentServiceDefinition service;
    private Provider provider;
    private Date startDate;
    private Time startTime;
    private Date endDate;
    private Time endTime;

    public Integer getAppointmentUnavailabilityId() {
        return appointmentUnavailabilityId;
    }

    public void setAppointmentUnavailabilityId(Integer appointmentUnavailabilityId) {
        this.appointmentUnavailabilityId = appointmentUnavailabilityId;
    }

    @Override
    public Integer getId() {
        return getAppointmentUnavailabilityId();
    }

    @Override
    public void setId(Integer id) {
        setAppointmentUnavailabilityId(id);
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public AppointmentServiceDefinition getService() {
        return service;
    }

    public void setService(AppointmentServiceDefinition service) {
        this.service = service;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Time getStartTime() {
        return startTime;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Time getEndTime() {
        return endTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }
}
