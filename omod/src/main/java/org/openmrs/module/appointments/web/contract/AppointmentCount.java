package org.openmrs.module.appointments.web.contract;

import java.util.Date;

public class AppointmentCount {
    private Integer allAppointmentsCount;
    private Integer missedAppointmentsCount;
    private Date appointmentDate;
    private String appointmentServiceUuid;

    public AppointmentCount(Integer allAppointmentsCount, Integer missedAppointmentsCount, Date appointmentDate, String appointmentServiceUuid) {
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
