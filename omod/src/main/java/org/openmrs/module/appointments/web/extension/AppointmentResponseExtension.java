package org.openmrs.module.appointments.web.extension;

import org.openmrs.module.appointments.model.Appointment;

import java.util.List;
import java.util.Map;

public interface AppointmentResponseExtension {
	Map<String, String> run(Appointment appointment);
}
