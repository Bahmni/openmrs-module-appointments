package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.User;


public class AppointmentAudit extends BaseOpenmrsData {

	private Integer appointmentAuditId;

	private Appointment appointment;

	private String notes;

	private AppointmentStatus status;

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public Integer getAppointmentAuditId() {
		return appointmentAuditId;
	}

	public void setAppointmentAuditId(Integer appointmentAuditId) {
		this.appointmentAuditId = appointmentAuditId;
	}

	@Override
	public Integer getId() {
		return getAppointmentAuditId();
	}

	@Override
	public void setId(Integer id) {
		setAppointmentAuditId(id);
	}
}
