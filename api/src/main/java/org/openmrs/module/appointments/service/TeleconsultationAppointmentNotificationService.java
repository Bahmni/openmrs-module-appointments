package org.openmrs.module.appointments.service;

import org.bahmni.module.email.notification.EmailNotificationException;
import org.openmrs.module.appointments.model.Appointment;

public interface TeleconsultationAppointmentNotificationService {
    void sendTeleconsultationAppointmentLinkEmail(Appointment appointment) throws EmailNotificationException;
}
