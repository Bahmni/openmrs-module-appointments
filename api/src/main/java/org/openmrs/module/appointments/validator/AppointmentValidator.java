package org.openmrs.module.appointments.validator;

import org.openmrs.module.appointments.model.Appointment;

import java.util.List;

public interface AppointmentValidator {

	void validate(Appointment appointment, List<String> errors);
}
