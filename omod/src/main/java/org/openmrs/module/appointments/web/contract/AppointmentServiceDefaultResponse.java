package org.openmrs.module.appointments.web.contract;

import java.util.Map;

public class AppointmentServiceDefaultResponse {
	private Integer appointmentServiceId;
	private String name;
	private String description;
	private Map speciality;
	private String startTime;
	private String endTime;
	private Integer maxAppointmentsLimit;
	private Integer durationMins;
	private Map location;
	private String uuid;
	private String color;
	private String initialAppointmentStatus;
	private String creatorName;

	public Integer getAppointmentServiceId() {
		return appointmentServiceId;
	}

	public void setAppointmentServiceId(Integer appointmentServiceId) {
		this.appointmentServiceId = appointmentServiceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Integer getMaxAppointmentsLimit() {
		return maxAppointmentsLimit;
	}

	public void setMaxAppointmentsLimit(Integer maxAppointmentsLimit) {
		this.maxAppointmentsLimit = maxAppointmentsLimit;
	}

	public Integer getDurationMins() {
		return durationMins;
	}

	public void setDurationMins(Integer durationMins) {
		this.durationMins = durationMins;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map getSpeciality() {
		return speciality;
	}

	public void setSpeciality(Map speciality) {
		this.speciality = speciality;
	}

	public Map getLocation() {
		return location;
	}

	public void setLocation(Map location) {
		this.location = location;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}


	public String getInitialAppointmentStatus() {
		return initialAppointmentStatus;
	}

	public void setInitialAppointmentStatus(String initialAppointmentStatus) {
		this.initialAppointmentStatus = initialAppointmentStatus;
	}
}
