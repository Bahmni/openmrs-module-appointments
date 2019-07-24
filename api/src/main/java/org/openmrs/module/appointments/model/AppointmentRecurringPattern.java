package org.openmrs.module.appointments.model;

import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AppointmentRecurringPattern {

    private Integer id;
    private Integer frequency;
    private Integer period;
    private Date endDate;
    private RecurringAppointmentType type;
    private String daysOfWeek;
    private Set<Appointment> appointments = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public RecurringAppointmentType getType() {
        return type;
    }

    public void setType(RecurringAppointmentType type) {
        this.type = type;
    }

    public String getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public Set<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(Set<Appointment> appointments) {
        this.appointments = appointments;
    }

    public Boolean isAppointmentsEmptyOrNull (){
        return this.appointments == null || this.appointments.isEmpty();
    }

    public Set<Appointment> getActiveAppointments() {
        return getAppointments().stream()
                .filter(appointment -> !appointment.getVoided()).collect(Collectors.toSet());
    }

}
