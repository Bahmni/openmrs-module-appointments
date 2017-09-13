package org.openmrs.module.appointments.scheduler.tasks;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MarkAppointmentAsCompleteTask extends AbstractTask {

    @Override
    public void execute() {
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        AdministrationService administrationService = Context.getService(AdministrationService.class);
        GlobalProperty schedulerMarksCompleteProperty = administrationService.getGlobalPropertyObject("SchedulerMarksComplete");
        Boolean schedulerMarksComplete = Boolean.valueOf(schedulerMarksCompleteProperty.getPropertyValue());

        if (!schedulerMarksComplete) {
            return;
        }

        Date today = new Date();
        List<Appointment> appointments = appointmentsService.getAllAppointmentsInDateRange(null, today);
        List<Appointment> scheduledAndCheckedInAppointments = appointments.stream()
                .filter(appointment -> isAppointmentCheckedIn(appointment))
                .collect(Collectors.toList());
        for (Appointment appointment : scheduledAndCheckedInAppointments) {
            String status = AppointmentStatus.Completed.toString();
            appointmentsService.changeStatus(appointment, status, today);
        }
    }

    private boolean isAppointmentCheckedIn(Appointment appointment) {
        return appointment.getStatus().equals(AppointmentStatus.CheckedIn);
    }

}
