package org.openmrs.module.appointments.service;

public interface AppointmentVisitLocation {
    String getFacilityName(String locationUuid);
    void setBaseURL(String baseURL);

}
