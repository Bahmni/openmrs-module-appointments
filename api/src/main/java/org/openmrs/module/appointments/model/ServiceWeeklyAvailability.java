package org.openmrs.module.appointments.model;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;
import java.sql.Time;
import java.time.DayOfWeek;

public class ServiceWeeklyAvailability extends BaseOpenmrsData implements Serializable{

	private Integer serviceWeeklyAvailabilityId;

	private DayOfWeek dayOfWeek;

	private Time startTime;

	private AppointmentServiceDefinition service;

	private Integer maxAppointmentsLimit;

	private Time endTime;

	public Integer getServiceWeeklyAvailabilityId() {
		return serviceWeeklyAvailabilityId;
	}

	public void setServiceWeeklyAvailabilityId(Integer serviceWeeklyAvailabilityId) {
		this.serviceWeeklyAvailabilityId = serviceWeeklyAvailabilityId;
	}

	public AppointmentServiceDefinition getService() {
		return service;
	}

	public void setService(AppointmentServiceDefinition service) {
		this.service = service;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public Integer getMaxAppointmentsLimit() {
		return maxAppointmentsLimit;
	}

	public void setMaxAppointmentsLimit(Integer maxAppointmentsLimit) {
		this.maxAppointmentsLimit = maxAppointmentsLimit;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public boolean isSameDay(String day){
		return this.getDayOfWeek().toString().equalsIgnoreCase(day);
	}

	@Override
	public Integer getId() {
		return getServiceWeeklyAvailabilityId();
	}

	@Override
	public void setId(Integer id) {
		setServiceWeeklyAvailabilityId(id);
	}
}
