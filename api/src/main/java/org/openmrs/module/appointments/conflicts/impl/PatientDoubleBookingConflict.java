package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.module.appointments.conflicts.AppointmentConflictType;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;

import java.util.List;

import static org.openmrs.module.appointments.constants.AppointmentConflictTypeEnum.PATIENT_DOUBLE_BOOKING;

public class PatientDoubleBookingConflict implements AppointmentConflictType {

    private AppointmentsService appointmentsService;

    public void setAppointmentsService(AppointmentsService appointmentsService) {
        this.appointmentsService = appointmentsService;
    }

    @Override
    public String getType() {
        return PATIENT_DOUBLE_BOOKING.name();
    }

    @Override
    public List<Appointment> getAppointmentConflicts(Appointment appointment) {
        return null;
    }
}
