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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultTCAppointmentSmsNotifier implements AppointmentEventNotifier {

    private final static String PROP_PATIENT_SMS_TEMPLATE = "bahmni.appointment.teleConsultation.patientSmsNotificationTemplate";
    private final static String PROP_SEND_TC_APPT_SMS = "bahmni.appointment.teleConsultation.sendSms";
    private final static String PROP_SEND_TC_APPT_CONTACT_ATTRIBUTE = "bahmni.appointment.teleConsultation.contactAttribute";

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
        String contactAttribute = Context.getAdministrationService().getGlobalProperty(PROP_SEND_TC_APPT_CONTACT_ATTRIBUTE);
        PersonAttribute patientContactAttribute = patient.getPerson().getAttribute(contactAttribute);
        Set<AppointmentProvider> providers = appointment.getProviders();
        List<String> contacts = new ArrayList<>();

        if (patientContactAttribute != null) {
            contacts.add(patientContactAttribute.getValue());
        }

        if (providers != null) {
            for (AppointmentProvider provider : providers) {
                PersonAttribute providerContactAttribute = provider.getProvider().getPerson().getAttribute(contactAttribute);
                if (providerContactAttribute != null) {
                    contacts.add(providerContactAttribute.getValue());
                }
            }
        }

        if (contacts.size() > 0) {
            String patientName = appointment.getPatient().getGivenName();
            String message = getMessage(patientName, appointment.getService(),
                    providers, appointment.getStartDateTime(),
                    appointment.getTeleHealthVideoLink());
            try {
                log.info("Sending sms through: " +  smsSender.getClass());
                smsSender.send(message, contacts);
                return new NotificationResult("", "SMS", NotificationResult.SUCCESS_STATUS, SMS_SENT);
            } catch (Exception e) {
                log.error(SMS_FAILURE, e);
                throw new NotificationException(SMS_FAILURE, e);
            }
        } else {
            log.warn(SMS_NOT_CONFIGURED);
            return new NotificationResult(null, "SMS", NotificationResult.GENERAL_ERROR, SMS_NOT_CONFIGURED);
        }
    }

    private String getMessage(String patientName,
                              AppointmentServiceDefinition service,
                              Set<AppointmentProvider> providers,
                              Date appointmentDate, String link) {
        String smsTemplate = Context.getAdministrationService().getGlobalProperty(PROP_PATIENT_SMS_TEMPLATE);
        String practitioners =
                providers != null ?
                        providers.stream()
                                .map(appointmentProvider -> appointmentProvider.getProvider().getName())
                                .collect(Collectors.joining(","))
                        : "";
        Object[] arguments = {patientName, practitioners, appointmentDate, link};
        if (smsTemplate == null || "".equals(smsTemplate)) {
            return Context.getMessageSourceService().getMessage(PROP_PATIENT_SMS_TEMPLATE, arguments, LocaleUtility.getDefaultLocale());
        } else {
            return new MessageFormat(smsTemplate).format(arguments);
        }
    }

    private boolean shouldSendSmsToPatient() {
        String shouldSendSMS = Context.getAdministrationService().getGlobalProperty(PROP_SEND_TC_APPT_SMS, "false");
        return Boolean.valueOf(shouldSendSMS);
    }
}
