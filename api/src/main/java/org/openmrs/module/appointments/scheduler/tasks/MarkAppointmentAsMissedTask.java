package org.openmrs.module.appointments.scheduler.tasks;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Date;
import java.util.List;

public class MarkAppointmentAsMissedTask extends AbstractTask {
    private long SECOND_IN_MILLI = 1000;

    @Override
    public void execute() {
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        SchedulerService schedulerService = Context.getService(SchedulerService.class);
        AdministrationService administrationService = Context.getService(AdministrationService.class);
        GlobalProperty schedulerMarksCompleteProperty = administrationService.getGlobalPropertyObject("SchedulerMarksComplete");
        Boolean schedulerMarksComplete = Boolean.valueOf(schedulerMarksCompleteProperty.getPropertyValue());
        TaskDefinition task = schedulerService.getTaskByName("Mark Appointment As Missed Task");
        long interval = task.getRepeatInterval();
        Date startDate = new Date(System.currentTimeMillis() - interval * SECOND_IN_MILLI);
        Date today = new Date();
        List<Appointment> appointments = appointmentsService.getAllAppointmentsInDateRange(startDate, today);
        for (Appointment appointment : appointments) {
            String status = AppointmentStatus.Missed.toString();
            if ((!schedulerMarksComplete && appointment.getStatus().equals(AppointmentStatus.CheckedIn)) || appointment.getStatus().equals(AppointmentStatus.Scheduled)) {
                appointmentsService.changeStatus(appointment, status, today);
            }
        }
    }
}
