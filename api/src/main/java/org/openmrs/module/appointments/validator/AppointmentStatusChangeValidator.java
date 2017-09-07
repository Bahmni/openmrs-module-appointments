package org.openmrs.module.appointments.validator;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;

import java.util.List;

public interface AppointmentStatusChangeValidator {
	 void validate(Appointment appointment, AppointmentStatus status, List<String> errors);
}
