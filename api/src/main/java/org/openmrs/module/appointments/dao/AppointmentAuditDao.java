package org.openmrs.module.appointments.dao;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;

import java.util.List;

public interface AppointmentAuditDao {

	void save(AppointmentAudit appointmentAuditEvent);

	List<AppointmentAudit> getAppointmentHistoryForAppointment(Appointment appointment);

	AppointmentAudit getPriorStatusChangeEvent(Appointment appointment);
}
