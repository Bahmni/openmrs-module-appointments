package org.openmrs.module.appointments.web.contract;

import java.util.Date;

public class DailyAppointmentServiceSummary {
    //is this class necessary at all? its part of AppointmentsSummary
    private Integer allAppointmentsCount;
    private Integer missedAppointmentsCount;
    private Date appointmentDate;
    private String appointmentServiceUuid;

    public DailyAppointmentServiceSummary(Date appointmentDate, String appointmentServiceUuid, Integer allAppointmentsCount, Integer missedAppointmentsCount) {
        this.allAppointmentsCount = allAppointmentsCount;
        this.missedAppointmentsCount = missedAppointmentsCount;
        this.appointmentDate = appointmentDate;
        this.appointmentServiceUuid = appointmentServiceUuid;
    }

    public Integer getAllAppointmentsCount() {
        return allAppointmentsCount;
    }

    public void setAllAppointmentsCount(Integer allAppointmentsCount) {
        this.allAppointmentsCount = allAppointmentsCount;
    }

    public Integer getMissedAppointmentsCount() {
        return missedAppointmentsCount;
    }

    public void setMissedAppointmentsCount(Integer missedAppointmentsCount) {
        this.missedAppointmentsCount = missedAppointmentsCount;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentServiceUuid() {
        return appointmentServiceUuid;
    }

    public void setAppointmentServiceUuid(String appointmentServiceUuid) {
        this.appointmentServiceUuid = appointmentServiceUuid;
    }
}
