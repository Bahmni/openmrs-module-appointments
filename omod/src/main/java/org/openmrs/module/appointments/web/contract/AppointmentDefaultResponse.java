package org.openmrs.module.appointments.web.contract;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AppointmentDefaultResponse {
	private String uuid;
	private String appointmentNumber;
	private Map patient;
	private AppointmentServiceDefaultResponse service;
	private Map serviceType;
	private Map provider;
	private Map location;
	private Date startDateTime;
	private Date endDateTime;
	private String appointmentKind;
	private String status;
	private String comments;
	private Map additionalInfo;
	private Boolean teleconsultation;
	private List<AppointmentProviderDetail> providers;
	private Boolean isRecurring;
	private Boolean voided;
	private Boolean isEmailIdAvailable;

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

	public AppointmentServiceDefaultResponse getService() {
		return service;
	}

	public void setService(AppointmentServiceDefaultResponse service) {
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

	public Date getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	public Date getEndDateTime() {
		return endDateTime;
	}
	
	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}
	
	public String getAppointmentKind() {
		return appointmentKind;
	}
	
	public void setAppointmentKind(String appointmentKind) {
		this.appointmentKind = appointmentKind;
	}

	public Boolean isTeleconsultation() {return teleconsultation; }

	public void setTeleconsultation(Boolean teleconsultation)
	{
		this.teleconsultation = teleconsultation;
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

	public Map getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Map additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public void setProviders(List<AppointmentProviderDetail> providers) {
		this.providers = providers;
	}

	public List<AppointmentProviderDetail> getProviders() {
		return providers;
	}

    public Boolean getRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public Boolean getEmailIdAvailable() {
		return isEmailIdAvailable;
	}

	public void setEmailIdAvailable(Boolean emailIdAvailable) {
		isEmailIdAvailable = emailIdAvailable;
	}
}
