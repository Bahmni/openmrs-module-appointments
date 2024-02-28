package org.openmrs.module.appointments.scheduler.tasks;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.service.CommunicationService;
import org.bahmni.module.communication.service.MessageBuilderService;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentArgumentsMapper;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.List;

public class ReminderForAppointment extends AbstractTask {
    private Log log = LogFactory.getLog(this.getClass());

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
            PersonAttribute phoneNumber = appointment.getPatient().getAttribute("phoneNumber");
            if (null == phoneNumber) {
                log.info("Since no mobile number found for the patient. SMS not sent.");
                return;
            }
            MessageBuilderService smsBuilderService =Context.getService(MessageBuilderService.class);
            AppointmentArgumentsMapper appointmentArgumentsMapper=Context.getService(AppointmentArgumentsMapper.class);
            String message = smsBuilderService.getAppointmentReminderMessage(appointmentArgumentsMapper.createArgumentsMapForAppointmentBooking(appointment),appointmentArgumentsMapper.getProvidersNameInString(appointment));
            CommunicationService communicationService=Context.getService(CommunicationService.class);
            communicationService.sendSMS(phoneNumber.getValue(), message);
        }
    }
}
