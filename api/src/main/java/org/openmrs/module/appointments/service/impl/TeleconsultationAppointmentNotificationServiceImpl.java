package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.email.notification.EmailNotificationException;
import org.bahmni.module.email.notification.service.EmailNotificationService;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;

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
        if (patient.getAttribute("email") != null) {
            String email = patient.getAttribute("email").getValue();
            emailNotificationService.send(
                    Context.getMessageSourceService().getMessage(EMAIL_SUBJECT, null, null),
                    Context.getMessageSourceService().getMessage(EMAIL_BODY, new Object[]{ link }, null),
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
