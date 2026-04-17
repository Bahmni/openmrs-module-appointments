package org.openmrs.module.appointments.web.contract;

import org.openmrs.module.appointments.model.AppointmentStatus;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class AppointmentStatusChangeRequest {
    
    @NotNull(message = "List of appointment UUIDs is required")
    @Size(min = 1, message = "At least one appointment UUID must be specified")
    private List<String> appointmentUuids;
    
    @NotNull(message = "toStatus is required")
    private AppointmentStatus toStatus;
    
    public List<String> getAppointmentUuids() {
        return appointmentUuids;
    }
    
    public void setAppointmentUuids(List<String> appointmentUuids) {
        this.appointmentUuids = appointmentUuids;
    }
    
    public AppointmentStatus getToStatus() {
        return toStatus;
    }
    
    public void setToStatus(AppointmentStatus toStatus) {
        this.toStatus = toStatus;
    }
}
