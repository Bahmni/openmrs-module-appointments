package org.openmrs.module.appointments.scheduler.tasks;

import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.notification.NotificationType;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.service.impl.PatientAppointmentNotifierService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SendAppointmentReminderTask extends AbstractTask {

    private final static String PROP_SEND_APPOINTMENT_REMINDER_EMAIL = "bahmni.appointment.teleConsultation.emailReminderEnabled";
    private final static String PROP_SEND_APPOINTMENT_REMINDER_EMAIL_TIMER = "bahmni.appointment.teleConsultation.appointmentReminderTimePeriod";

    @Override
    public void execute() {
        AppointmentsService appointmentsService = Context.getService(AppointmentsService.class);
        PatientAppointmentNotifierService patientAppointmentNotifierService = Context.getService(PatientAppointmentNotifierService.class);
        if (!shouldSendReminder()) {
            return;
        }
        String interval = Context.getAdministrationService().getGlobalProperty(PROP_SEND_APPOINTMENT_REMINDER_EMAIL_TIMER);

        Calendar calendar = Calendar.getInstance();
        Date from = calendar.getTime();
        calendar.add(Calendar.MINUTE, Integer.parseInt(interval));
        Date to = calendar.getTime();
        List<Appointment> appointments = appointmentsService.getAllAppointmentsByDate(from, to);
        List<Appointment> scheduledAppointments = appointments.stream()
                .filter(appointment -> appointment.getStatus().equals(AppointmentStatus.Scheduled))
                .collect(Collectors.toList());
        for (Appointment appointment : scheduledAppointments) {
            patientAppointmentNotifierService.notifyAll(appointment, NotificationType.Reminder);
        }
    }

    private Boolean shouldSendReminder() {
        String shouldSendReminderEmail = Context.getAdministrationService().getGlobalProperty(PROP_SEND_APPOINTMENT_REMINDER_EMAIL);
        return Boolean.valueOf(shouldSendReminderEmail);
    }
}
