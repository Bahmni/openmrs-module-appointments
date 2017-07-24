package org.openmrs.module.appointments.web.contract;

public class AppointmentDefaultResponse {
	private Integer appointmentId;
	private String providerUuid;
	private String appointmentNumber;
	private String locationUuid;
	private String patientUuid;
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

	public String getProviderUuid() {
		return providerUuid;
	}

	public void setProviderUuid(String providerUuid) {
		this.providerUuid = providerUuid;
	}

	public String getLocationUuid() {
		return locationUuid;
	}

	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}

	public String getPatientUuid() {
		return patientUuid;
	}

	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
}
