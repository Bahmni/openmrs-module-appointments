package org.openmrs.module.appointments.scheduler.tasks;


import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentReminderService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.impl.AppointmentReminderServiceImpl;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class ReminderForAppointment extends AbstractTask {

    private AppointmentReminderService appointmentReminderService;

    public ReminderForAppointment(AppointmentReminderService appointmentReminderService) {
        this.appointmentReminderService = appointmentReminderService;
    }


    @Override
    public void execute() {
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        AdministrationService administrationService = Context.getService(AdministrationService.class);
        String schedulerReminderTime = administrationService.getGlobalPropertyObject("SchedulerReminderBeforeHours").getPropertyValue();

        if (schedulerReminderTime.equals("0")) {
            return;
        }
        List<Appointment> appointments = appointmentsService.getAllAppointmentsReminder(schedulerReminderTime);
        System.out.println("hii");
        System.out.println(appointments);
        for (Appointment appointment: appointments) {
           appointmentReminderService.sendAppointmentReminderSMS(appointment);
        }
    }
}
