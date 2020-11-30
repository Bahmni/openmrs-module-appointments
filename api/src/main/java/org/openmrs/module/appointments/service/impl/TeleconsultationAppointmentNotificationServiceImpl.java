package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.email.notification.EmailNotificationException;
import org.bahmni.module.email.notification.service.EmailNotificationService;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TeleconsultationAppointmentNotificationServiceImpl implements TeleconsultationAppointmentNotificationService {
    private final static String EMAIL_SUBJECT = "teleconsultation.appointment.email.subject";
    private final static String EMAIL_BODY = "teleconsultation.appointment.email.body";

    private Log log = LogFactory.getLog(this.getClass());

    private EmailNotificationService emailNotificationService;

    private TeleconsultationAppointmentService teleconsultationAppointmentService = new TeleconsultationAppointmentService();

    public TeleconsultationAppointmentNotificationServiceImpl() {}
    public TeleconsultationAppointmentNotificationServiceImpl(
            EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    public void sendTeleconsultationAppointmentLinkEmail(Appointment appointment) throws EmailNotificationException {
        String link = teleconsultationAppointmentService.getTeleconsultationURL(appointment);
        Patient patient = appointment.getPatient();
        PersonAttribute patientEmailAttribute = patient.getAttribute("email");
        if (patientEmailAttribute != null) {
            String email = patientEmailAttribute.getValue();
            String patientName = appointment.getPatient().getGivenName();
            String doctor = "";
            if(appointment.getProviders() != null) {
                AppointmentProvider provider = appointment.getProviders().iterator().next();
                doctor = " with Dr. " + provider.getProvider().getPerson().getGivenName();
            }
            Date appointmentStart = appointment.getStartDateTime();
            String day = new SimpleDateFormat("EEEE").format(appointmentStart);
            String date = new SimpleDateFormat("dd/MM/yy").format(appointmentStart);
            String time = new SimpleDateFormat("hh:mm a").format(appointmentStart);
            emailNotificationService.send(
                    Context.getMessageSourceService().getMessage(EMAIL_SUBJECT, null, null),
                    Context.getMessageSourceService().getMessage(
                            EMAIL_BODY,
                            new Object[]{
                                    patientName,
                                    doctor,
                                    day,
                                    date,
                                    time,
                                    link
                            },
                            null
                    ),
                    new String[] { email },
                    null,
                    null);
        } else {
            log.warn("Attempting to send an email to a patient without an email address");
        }
    }

    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

}
