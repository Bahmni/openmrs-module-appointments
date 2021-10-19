package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.util.LocaleUtility;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultTCAppointmentPatientEmailNotifier implements AppointmentEventNotifier {
    private final static String PROP_PATIENT_EMAIL_SUBJECT = "bahmni.appointment.teleConsultation.patientEmailNotificationSubject";
    private final static String PROP_PATIENT_EMAIL_TEMPLATE = "bahmni.appointment.teleConsultation.patientEmailNotificationTemplate";
    private final static String PROP_SEND_TC_APPT_EMAIL = "bahmni.appointment.teleConsultation.sendEmail";

    private final static String PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_SUBJECT = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationSubject";
    private final static String PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_TEMPLATE = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationTemplate";
    private final static String PROP_ADHOC_TELECONSULTATION_CC_EMAILS = "bahmni.appointment.adhocTeleConsultation.ccEmails";

    private static final String EMAIL_NOT_CONFIGURED = "Notification can not be sent to patient. Email address not configured.";
    private static final String EMAIL_SENT = "Email sent to Patient";
    private static final String MEDIUM_EMAIL = "EMAIL";
    private static final String EMAIL_FAILURE = "Failed to send email to patient";
    private static final String EMAIL_NOT_SENT = "Email notification for tele-consultation not configured to be sent to patient.";

    private Log log = LogFactory.getLog(this.getClass());
    private MailSender mailSender;

    public DefaultTCAppointmentPatientEmailNotifier() {}
    public DefaultTCAppointmentPatientEmailNotifier(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String getMedium() {
        return MEDIUM_EMAIL;
    }

    @Override
    public boolean isApplicable(final Appointment appointment) {
        boolean sendEmailToPatient = shouldSendEmailToPatient();
        if (!sendEmailToPatient) {
            log.warn(EMAIL_NOT_SENT);
        }
        if (appointment.getAppointmentKind() != null && appointment.getAppointmentKind().equals(AppointmentKind.Virtual)) {
            return sendEmailToPatient;
        }
        return false;
    }

    @Override
    public NotificationResult sendNotification(final Appointment appointment) throws NotificationException {
        Patient patient = appointment.getPatient();
        PersonAttribute patientEmailAttribute = patient.getPerson().getAttribute("email");
        if (patientEmailAttribute != null) {
            String patientEmail = patientEmailAttribute.getValue();
            String patientName = appointment.getPatient().getGivenName();
            String emailSubject = getEmailSubject();
            String emailBody = getEmailBody(patientName, appointment.getService(), appointment.getProviders(), appointment.getStartDateTime(), appointment.getTeleHealthVideoLink());
            try {
                log.info("Sending mail through: " +  mailSender.getClass());
                mailSender.send(emailSubject, emailBody, new String[] { patientEmail }, null, null);
                return new NotificationResult("", "EMAIL", 0, EMAIL_SENT);
            } catch (Exception e) {
                log.error(EMAIL_FAILURE, e);
                throw new NotificationException(EMAIL_FAILURE, e);
            }
        } else {
            log.warn(EMAIL_NOT_CONFIGURED);
            return new NotificationResult(null, "EMAIL", 1, EMAIL_NOT_CONFIGURED);
        }
    }

    public NotificationResult sendNotification(final Patient patient, final String provider, final String link) throws NotificationException {
        PersonAttribute patientEmailAttribute = patient.getPerson().getAttribute("email");
        if (patientEmailAttribute != null) {
            String patientEmail = patientEmailAttribute.getValue();
            String patientName = patient.getGivenName();
            String emailSubject = getAdhocTeleconsultationEmailSubject();
            String emailBody = getAdhocTeleconsultationEmailBody(patientName, provider, link);
            String ccEmails = getAdhocTeleconsultationCCEmails();
            String [] ccEmailArray = null;
            if (ccEmails != null && !("".equals(ccEmails))) {
                ccEmailArray = ccEmails.split(",");
            }

            try {
                log.info("Sending mail through: " +  mailSender.getClass());
                mailSender.send(emailSubject, emailBody, new String[] { patientEmail }, ccEmailArray, null);
                return new NotificationResult("", "EMAIL", 0, EMAIL_SENT);
            } catch (Exception e) {
                log.error(EMAIL_FAILURE, e);
                throw new NotificationException(EMAIL_FAILURE, e);
            }
        } else {
            log.warn(EMAIL_NOT_CONFIGURED);
            return new NotificationResult(null, "EMAIL", 1, EMAIL_NOT_CONFIGURED);
        }
    }

    private boolean shouldSendEmailToPatient() {
        String shouldSendEmail = Context.getAdministrationService().getGlobalProperty(PROP_SEND_TC_APPT_EMAIL, "false");
        return Boolean.valueOf(shouldSendEmail);
    }

    private String getEmailBody(String patientName, AppointmentServiceDefinition service, Set<AppointmentProvider> providers, Date appointmentDate, String link) {
        String emailTemplate = Context.getAdministrationService().getGlobalProperty(PROP_PATIENT_EMAIL_TEMPLATE);
        String practitioners =
                providers != null ?
                        providers.stream()
                                .map(appointmentProvider -> appointmentProvider.getProvider().getName())
                                .collect(Collectors.joining(","))
                        : "";
        Object[] arguments = {patientName, practitioners, appointmentDate, link};
        if (emailTemplate == null || "".equals(emailTemplate)) {
            return Context.getMessageSourceService().getMessage(PROP_PATIENT_EMAIL_TEMPLATE, arguments, LocaleUtility.getDefaultLocale());
        } else {
            return new MessageFormat(emailTemplate).format(arguments);
        }
    }

    private String getEmailSubject() {
        String emailSubject = Context.getAdministrationService().getGlobalProperty(PROP_PATIENT_EMAIL_SUBJECT);
        if (emailSubject == null || "".equals(emailSubject)) {
            emailSubject = Context.getMessageSourceService().getMessage(PROP_PATIENT_EMAIL_SUBJECT, null, LocaleUtility.getDefaultLocale());
        }
        return emailSubject;
    }

    private String getAdhocTeleconsultationEmailSubject() {
        String adhocTeleconsultationEmailSubject = Context.getAdministrationService().getGlobalProperty(PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_SUBJECT);
        if (adhocTeleconsultationEmailSubject == null || "".equals(adhocTeleconsultationEmailSubject)) {
            adhocTeleconsultationEmailSubject = Context.getMessageSourceService().getMessage(PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_SUBJECT, null, LocaleUtility.getDefaultLocale());
        }
        return adhocTeleconsultationEmailSubject;
    }

    private String getAdhocTeleconsultationEmailBody(String patientName, String provider, String link) {
        String adhocTeleconsultationEmailTemplate = Context.getAdministrationService().getGlobalProperty(PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_TEMPLATE);
        Object[] arguments = {patientName, provider, link};
        if (adhocTeleconsultationEmailTemplate == null || "".equals(adhocTeleconsultationEmailTemplate)) {
            return Context.getMessageSourceService().getMessage(PROP_ADHOC_TELECONSULTATION_PATIENT_EMAIL_TEMPLATE, arguments, LocaleUtility.getDefaultLocale());
        } else {
            return new MessageFormat(adhocTeleconsultationEmailTemplate).format(arguments);
        }
    }

    private String getAdhocTeleconsultationCCEmails() {
        String adhocTeleconsultationCCEmails = Context.getAdministrationService().getGlobalProperty(PROP_ADHOC_TELECONSULTATION_CC_EMAILS);
        if (adhocTeleconsultationCCEmails == null || "".equals(adhocTeleconsultationCCEmails)) {
            adhocTeleconsultationCCEmails = Context.getMessageSourceService().getMessage(PROP_ADHOC_TELECONSULTATION_CC_EMAILS, null, LocaleUtility.getDefaultLocale());
        }
        return adhocTeleconsultationCCEmails;
    }

    public void setMailSender(MailSender mailSender) {
        log.warn("Replacing default MailSender: " + this.mailSender + ", with:" + mailSender);
        this.mailSender = mailSender;
    }
}
