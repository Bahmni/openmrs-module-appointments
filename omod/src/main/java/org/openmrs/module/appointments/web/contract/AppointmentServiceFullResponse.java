package org.openmrs.module.appointments.web.contract;

import java.util.List;

public class AppointmentServiceFullResponse extends AppointmentServiceDefaultResponse{

    private List weeklyAvailability;
    private List serviceTypes;

    public List getWeeklyAvailability() {
        return weeklyAvailability;
    }

    public void setWeeklyAvailability(List weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }

    public List getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List serviceTypes) {
        this.serviceTypes = serviceTypes;
    }
}
