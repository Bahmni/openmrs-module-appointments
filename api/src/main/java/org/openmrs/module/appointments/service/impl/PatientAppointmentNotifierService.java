package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PatientAppointmentNotifierService {

    private static final String CANNOT_SEND_NOTIFICATION_USING_MEDIUM = "Unable to send tele-consultation appointment information through ";
    private static final String NOT_APPLICABLE = "This appointment is not applicable to the notifier. Medium: ";
    private Log log = LogFactory.getLog(this.getClass());

    private List<AppointmentEventNotifier> eventNotifiers = new ArrayList<>();

    //def constructor not required. Test application contexts should spring  wiring
    public PatientAppointmentNotifierService() {}
    public PatientAppointmentNotifierService(List<AppointmentEventNotifier> notifiers) {
        this.eventNotifiers = notifiers;
    }

    public List<NotificationResult> notifyAll(final Appointment appointment) {
        if ((eventNotifiers == null) || eventNotifiers.isEmpty()) return Collections.emptyList();
        log.info("Notifying TC Appointment. Number of notifiers:" + eventNotifiers.size());
        List<NotificationResult> notificationResults = new ArrayList<>();
        for (AppointmentEventNotifier eventNotifier : eventNotifiers) {
            try {
                if (eventNotifier.isApplicable(appointment)) {
                    log.debug("Invoking Appointment Notifier: " + eventNotifier.getClass());
                    NotificationResult result = eventNotifier.sendNotification(appointment);
                    notificationResults.add(result);
                } else {
                    log.info(NOT_APPLICABLE + eventNotifier.getMedium());
                }
            } catch (NotificationException e) {
                String msg = CANNOT_SEND_NOTIFICATION_USING_MEDIUM + eventNotifier.getMedium();
                log.error(msg, e);
                notificationResults.add(new NotificationResult("", eventNotifier.getMedium(), NotificationResult.GENERAL_ERROR, msg));
            }
        }
        return notificationResults;
    }

    public List<NotificationResult> notifyAll(final Patient patient, final String provider, final String link) {
        if ((eventNotifiers == null) || eventNotifiers.isEmpty()) return Collections.emptyList();
        log.info("Notifying AdhocTeleconsultation. Number of notifiers:" + eventNotifiers.size());
        List<NotificationResult> notificationResults = new ArrayList<>();
        for (AppointmentEventNotifier eventNotifier : eventNotifiers) {
            try {
                log.debug("Invoking Appointment Notifier: " + eventNotifier.getClass());
                NotificationResult result = eventNotifier.sendNotification(patient, provider, link);
                notificationResults.add(result);
            } catch (NotificationException e) {
                String msg = CANNOT_SEND_NOTIFICATION_USING_MEDIUM + eventNotifier.getMedium();
                log.error(msg, e);
                notificationResults.add(new NotificationResult("", eventNotifier.getMedium(), NotificationResult.GENERAL_ERROR, msg));
            }
        }
        return notificationResults;
    }

    public List<AppointmentEventNotifier> getEventNotifiers() {
        return eventNotifiers;
    }

    public void setEventNotifiers(List<AppointmentEventNotifier> eventNotifiers) {
        this.eventNotifiers = eventNotifiers;
    }

    public void registerNotifier(AppointmentEventNotifier notifier) {
        if (notifier.getMedium() != null) {
            List<AppointmentEventNotifier> exitingNotifiers = this.eventNotifiers.stream().filter(n -> n.getMedium().equalsIgnoreCase(notifier.getMedium())).collect(Collectors.toList());
            if  (exitingNotifiers != null && !exitingNotifiers.isEmpty() ) {
                this.eventNotifiers.removeAll(exitingNotifiers);
            }
            this.eventNotifiers.add(notifier);
        }
    }
}
