package org.openmrs.module.appointments.conflicts;

import org.openmrs.module.appointments.model.Appointment;

import java.util.List;


public interface AppointmentConflictType {

    Enum getType();

    List<Appointment> getAppointmentConflicts(List<Appointment> appointment);
}
