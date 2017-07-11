package org.openmrs.module.appointments.web.contract;

import java.util.List;
import java.util.Map;

public class AppointmentServiceFullResponse extends AppointmentServiceDefaultResponse{

    private List weeklyAvailability;

    public List getWeeklyAvailability() {
        return weeklyAvailability;
    }

    public void setWeeklyAvailability(List weeklyAvailability) {
        this.weeklyAvailability = weeklyAvailability;
    }
}
