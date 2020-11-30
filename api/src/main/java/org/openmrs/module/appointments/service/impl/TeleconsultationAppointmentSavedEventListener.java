package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.email.notification.EmailNotificationException;
import org.openmrs.module.appointments.event.TeleconsultationAppointmentSavedEvent;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class TeleconsultationAppointmentSavedEventListener implements ApplicationListener<TeleconsultationAppointmentSavedEvent> {

    private Log log = LogFactory.getLog(this.getClass());

    @Autowired
    private TeleconsultationAppointmentNotificationService emailNotificationService;

    public TeleconsultationAppointmentSavedEventListener() {}
    public TeleconsultationAppointmentSavedEventListener(TeleconsultationAppointmentNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    public void onApplicationEvent(TeleconsultationAppointmentSavedEvent event) {
        try {
            emailNotificationService.sendTeleconsultationAppointmentLinkEmail(event.getAppointment());
        } catch (EmailNotificationException e) {
            log.error("Unable to send teleconsultation appointment email notification", e);
        }
    }
}
