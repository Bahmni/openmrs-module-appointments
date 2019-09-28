package org.openmrs.module.appointments.conflicts.impl;

import org.openmrs.module.appointments.conflicts.AppointmentConflictType;
import org.openmrs.module.appointments.model.Appointment;

import java.util.List;

import static org.openmrs.module.appointments.constants.AppointmentConflictTypeEnum.SERVICE_UNAVAILABLE;

public class AppointmentServiceUnavailabilityConflict implements AppointmentConflictType {

    @Override
    public String getType() {
        return SERVICE_UNAVAILABLE.name();
    }

    @Override
    public List<Appointment> getAppointmentConflicts(Appointment appointment) {
        return null;
    }
}
