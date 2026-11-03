package org.openmrs.module.appointments.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.service.AppointmentStatusMarkerService;
import org.openmrs.scheduler.tasks.AbstractTask;


public class AppointmentsMissedWeeklyScheduler extends AbstractTask {
    @Override
    public void execute() {
        AppointmentStatusMarkerService appointmentStatusMarkerService = Context.getService(AppointmentStatusMarkerService.class);
        appointmentStatusMarkerService.markAllMissedAppointments(appointmentStatusMarkerService.schedulertStartDateTime(7), appointmentStatusMarkerService.schedulerEndDateTime());
 }
}
