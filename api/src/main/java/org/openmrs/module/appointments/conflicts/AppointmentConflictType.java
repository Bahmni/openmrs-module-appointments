package org.openmrs.module.appointments.conflicts;

import org.openmrs.module.appointments.model.Appointment;

import java.util.List;


public interface AppointmentConflictType {

    String getType();

    List<Appointment> getAppointmentConflicts(Appointment appointment);
}
