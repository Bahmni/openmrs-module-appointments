package org.openmrs.module.appointments.web.contract;

import java.sql.Time;
import java.time.DayOfWeek;

public class ServiceWeeklyAvailabilityDescription {
	private String uuid;
	private Time startTime;
	private Time endTime;
	private Integer maxAppointmentsLimit;
	private DayOfWeek dayOfWeek;
	private boolean voided;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
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

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public boolean isVoided() {
		return voided;
	}

	public void setVoided(boolean voided) {
		this.voided = voided;
	}
}
