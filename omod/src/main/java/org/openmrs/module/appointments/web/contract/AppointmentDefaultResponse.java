package org.openmrs.module.appointments.web.contract;

import java.util.Map;

public class AppointmentDefaultResponse {
	private Integer appointmentId;
	private Map provider;
	private String appointmentNumber;
	private Map location;
	private Map patient;
	private String status;
	private String startDateTime;
	private String endDateTime;
	private String AppointmentsKind;
	private String comments;
	private String uuid;


	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAppointmentsKind() {
		return AppointmentsKind;
	}

	public void setAppointmentsKind(String appointmentsKind) {
		AppointmentsKind = appointmentsKind;
	}

	public String getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAppointmentNumber() {
		return appointmentNumber;
	}

	public void setAppointmentNumber(String appointmentNumber) {
		this.appointmentNumber = appointmentNumber;
	}

	public Integer getAppointmentId() {
		return appointmentId;
	}

	public void setAppointmentId(int appointmentId) {
		this.appointmentId = appointmentId;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Map getProvider() {
		return provider;
	}

	public void setProvider(Map provider) {
		this.provider = provider;
	}

	public Map getLocation() {
		return location;
	}

	public void setLocation(Map location) {
		this.location = location;
	}

	public Map getPatient() {
		return patient;
	}

	public void setPatient(Map patient) {
		this.patient = patient;
	}
}
