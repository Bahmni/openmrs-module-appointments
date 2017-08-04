package org.openmrs.module.appointments.web.contract;

import java.util.Map;

public class AppointmentDefaultResponse {
	private String uuid;
	private String appointmentNumber;
	private Map patient;
	private Map service;
	private Map serviceType;
	private Map provider;
	private Map location;
	private String startDateTime;
	private String endDateTime;
	private String appointmentKind;
	private String status;
	private String comments;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getAppointmentNumber() {
		return appointmentNumber;
	}

	public void setAppointmentNumber(String appointmentNumber) {
		this.appointmentNumber = appointmentNumber;
	}

	public Map getPatient() {
		return patient;
	}

	public void setPatient(Map patient) {
		this.patient = patient;
	}

	public Map getService() {
		return service;
	}

	public void setService(Map service) {
		this.service = service;
	}
	
	public Map getServiceType() {
		return serviceType;
	}
	
	public void setServiceType(Map serviceType) {
		this.serviceType = serviceType;
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

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	public String getEndDateTime() {
		return endDateTime;
	}
	
	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}
	
	public String getAppointmentKind() {
		return appointmentKind;
	}
	
	public void setAppointmentKind(String appointmentKind) {
		this.appointmentKind = appointmentKind;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
}
