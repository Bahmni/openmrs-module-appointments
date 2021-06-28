package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.openmrs.module.appointments.event.TeleconsultationAppointmentSavedEvent;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeleconsultationAppointmentSavedEventListener implements ApplicationListener<TeleconsultationAppointmentSavedEvent> {

    private Log log = LogFactory.getLog(this.getClass());

    private List<AppointmentEventNotifier> eventNotifiers = new ArrayList<>();

    public TeleconsultationAppointmentSavedEventListener() {}
    public TeleconsultationAppointmentSavedEventListener(List<AppointmentEventNotifier> notifiers) {
        this.eventNotifiers = notifiers;
    }

    @Override
    public void onApplicationEvent(TeleconsultationAppointmentSavedEvent event) {
        if ((eventNotifiers == null) || eventNotifiers.isEmpty()) return;

        event.getAppointment().setEmailSent(false);
        log.error("No of notifiers:" + eventNotifiers.size());
        for (AppointmentEventNotifier eventNotifier : eventNotifiers) {
            try {
                NotificationResult result = eventNotifier.sendNotification(event.getAppointment());
                if (result.getStatus() != NotificationResult.SUCCESS_STATUS) {
                    event.getAppointment().setEmailSent(true);
                }
            } catch (NotificationException e) {
                log.error("Unable to send tele-consultation appointment information through "  + eventNotifier.getMedium(), e);
            }
        }
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
