package org.openmrs.module.appointments.validator.impl;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultAppointmentValidator implements AppointmentValidator {

	@Override
	public void validate(Appointment appointment, List<String> errors) {
		if (appointment.getPatient() == null)
			errors.add("Appointment cannot be created without Patient");
		if (appointment.getService() == null)
			errors.add("Appointment cannot be created without Service");
	}
}
