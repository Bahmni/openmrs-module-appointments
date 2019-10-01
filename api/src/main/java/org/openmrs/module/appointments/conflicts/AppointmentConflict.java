package org.openmrs.module.appointments.conflicts;

import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.Appointment;

import java.util.List;


public interface AppointmentConflict {

    AppointmentConflictType getType();

    List<Appointment> getConflicts(List<Appointment> appointment);
}
