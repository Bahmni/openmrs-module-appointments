package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.notification.*;
import org.openmrs.util.LocaleUtility;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultTCAppointmentSmsNotifier implements AppointmentEventNotifier {

    private final static String PROP_PATIENT_SMS_TEMPLATE = "bahmni.appointment.teleConsultation.patientSmsNotificationTemplate";
    private final static String PROP_SEND_TC_APPT_SMS = "bahmni.appointment.teleConsultation.sendSms";

    private static final String MEDIUM_SMS = "SMS";
    private static final String SMS_NOT_CONFIGURED = "Notification can not be sent to patient. Mobile number not configured.";
    private static final String SMS_SENT = "SMS sent to Patient";
    private static final String SMS_FAILURE = "Failed to send sms to patient";
    private static final String SMS_NOT_SENT = "SMS notification for tele-consultation not configured to be sent to patient.";


    private Log log = LogFactory.getLog(this.getClass());
    private SmsSender smsSender;

    public DefaultTCAppointmentSmsNotifier() {}
    public DefaultTCAppointmentSmsNotifier(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    @Override
    public String getMedium() {
        return MEDIUM_SMS;
    }

    @Override
    public boolean isApplicable(final Appointment appointment) {
        boolean sendSmsToPatient = shouldSendSmsToPatient();
        if (!sendSmsToPatient) {
            log.warn(SMS_NOT_SENT);
        }
        if (appointment.getAppointmentKind() != null && appointment.getAppointmentKind().equals(AppointmentKind.Virtual)) {
            return sendSmsToPatient;
        }
        return false;
    }

    @Override
    public NotificationResult sendNotification(final Appointment appointment) throws NotificationException {
        Patient patient = appointment.getPatient();
        PersonAttribute patientMobileAttribute = patient.getPerson().getAttribute("mobile");
        Set<AppointmentProvider> providers = appointment.getProviders();
        String practitionerNumbers =
                providers != null ?
                        providers.stream()
                                .map(appointmentProvider -> appointmentProvider.getProvider()
                                        .getPerson().getAttribute("mobile").getValue())
                                .collect(Collectors.joining(","))
                        : "";
        if (patientMobileAttribute != null) {
            String patientEmail = patientMobileAttribute.getValue();
            String patientName = appointment.getPatient().getGivenName();
            String message = getMessage(patientName, appointment.getService(),
                    providers, appointment.getStartDateTime(),
                    appointment.getTeleHealthVideoLink());
            String [] mobileList = {};
            try {
                log.info("Sending sms through: " +  smsSender.getClass());
                smsSender.send(message, mobileList);
                return new NotificationResult("", "EMAIL", 0, SMS_SENT);
            } catch (Exception e) {
                log.error(SMS_FAILURE, e);
                throw new NotificationException(SMS_FAILURE, e);
            }
        } else {
            log.warn(SMS_NOT_CONFIGURED);
            return new NotificationResult(null, "EMAIL", 1, SMS_NOT_CONFIGURED);
        }
    }

    private String getMessage(String patientName,
                              AppointmentServiceDefinition service,
                              Set<AppointmentProvider> providers,
                              Date appointmentDate, String link) {
        String emailTemplate = Context.getAdministrationService().getGlobalProperty(PROP_PATIENT_SMS_TEMPLATE);
        String practitioners =
                providers != null ?
                        providers.stream()
                                .map(appointmentProvider -> appointmentProvider.getProvider().getName())
                                .collect(Collectors.joining(","))
                        : "";
        Object[] arguments = {patientName, practitioners, appointmentDate, link};
        if (emailTemplate == null || "".equals(emailTemplate)) {
            return Context.getMessageSourceService().getMessage(PROP_PATIENT_SMS_TEMPLATE, arguments, LocaleUtility.getDefaultLocale());
        } else {
            return new MessageFormat(emailTemplate).format(arguments);
        }
    }

    private boolean shouldSendSmsToPatient() {
        String shouldSendEmail = Context.getAdministrationService().getGlobalProperty(PROP_SEND_TC_APPT_SMS, "false");
        return Boolean.valueOf(shouldSendEmail);
    }
}
