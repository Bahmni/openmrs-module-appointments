package org.openmrs.module.appointments.notification.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.openmrs.module.appointments.notification.SmsSender;

public class DefaultAppointmentPatientSmsNotifier implements AppointmentEventNotifier {
	private Log log = LogFactory.getLog(this.getClass());

	private final static String PROP_SEND_APPT_SMS = "bahmni.appointment.sendSms";

    private static final String SMS_NOT_CONFIGURED = "SMS notification can not be sent to patient. Phone number not configured.";
    private static final String SMS_SENT = "SMS sent to patient";
    private static final String MEDIUM_SMS = "SMS";
    private static final String SMS_FAILURE = "Failed to send sms to patient";
    private static final String SMS_NOT_SENT = "SMS notification not configured to be sent to patient.";

    private SmsSender smsSender;

    public DefaultAppointmentPatientSmsNotifier() {}

    public DefaultAppointmentPatientSmsNotifier(SmsSender smsSender) {
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
        return sendSmsToPatient;
    }

    @Override
    public NotificationResult sendNotification(final Appointment appointment) throws NotificationException {
        Patient patient = appointment.getPatient();
        PersonAttribute patientPhoneAttribute = patient.getPerson().getAttribute("phoneNumber");
        if (patientPhoneAttribute != null) {
            String patientPhoneNumber = patientPhoneAttribute.getValue();
            String patientName = appointment.getPatient().getGivenName();
            String apptServiceName = appointment.getService().getName();
            String teleLink = StringUtils.isNotBlank(appointment.getTeleHealthVideoLink()) ? appointment.getTeleHealthVideoLink() : "";
            String smsType =  appointment.getAppointmentKind().toString();
            try {
                log.info("Sending sms through: " +  smsSender.getClass());
                smsSender.send(patientPhoneNumber, patientName, smsType, apptServiceName, appointment.getStartDateTime(), teleLink);
                return new NotificationResult("", "SMS", 0, SMS_SENT);
            } catch (Exception e) {
                log.error(SMS_FAILURE, e);
                throw new NotificationException(SMS_FAILURE, e);
            }
        } else {
            log.warn(SMS_NOT_CONFIGURED);
            return new NotificationResult(null, "SMS", 1, SMS_NOT_CONFIGURED);
        }
    }

    private boolean shouldSendSmsToPatient() {
        String shouldSendEmail = Context.getAdministrationService().getGlobalProperty(PROP_SEND_APPT_SMS, "false");
        return Boolean.valueOf(shouldSendEmail);
    }

    public void setSmsSender(SmsSender smsSender) {
        log.warn("Replacing default SmsSender: " + this.smsSender + ", with:" + smsSender);
        this.smsSender = smsSender;
    }
}
