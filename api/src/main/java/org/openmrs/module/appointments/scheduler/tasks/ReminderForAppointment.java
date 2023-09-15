package org.openmrs.module.appointments.scheduler.tasks;


import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.List;

public class ReminderForAppointment extends AbstractTask {

    @Override
    public void execute() {
        AdministrationService administrationService = Context.getService(AdministrationService.class);

        boolean scheduleSMS = Boolean.parseBoolean(administrationService.getGlobalProperty("sms.enableAppointmentReminderSMSAlert", "false"));

        if (!scheduleSMS) {
            return;
        }
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        String schedulerReminderTime = administrationService.getGlobalPropertyObject("SchedulerReminderBeforeHours").getPropertyValue();
        List<Appointment> appointments = appointmentsService.getAllAppointmentsReminder(schedulerReminderTime);
        for (Appointment appointment: appointments) {
           appointmentsService.sendAppointmentReminderSMS(appointment);
        }
    }
}
