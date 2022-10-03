package org.openmrs.module.appointments.notification;

import org.openmrs.Patient;
import org.openmrs.module.appointments.model.Appointment;

/**
 * A notifier is responsible to send a notification about an appointment.
 * The notifier is called from a spring application event listener, and therefore
 * should try to do perform the job Async rather than blocking.
 *
 * sendNotification() now takes an appointment object. We may want to pass a clone
 * or a minimal object instead of exposing a persistent Hibernate entity
 * the method may also be considered to return a status object, especially if we are
 * thinking of async to pass information like acknowledgement/txn-id
 *
 * As of now, this is a blocking call. and sendNotification() should throw exception to
 * indicate failure.
 *
 */
public interface AppointmentEventNotifier {
    /**
     * Should send medium that will be used for sending notification.
     * example - EMAIL, SMS etc
     * @return
     */
    String getMedium();
    boolean isApplicable(final Appointment appointment);
    NotificationResult sendNotification(final Appointment appointment) throws NotificationException;
    NotificationResult sendNotification(final Patient patient,  final String provider, final String link) throws NotificationException;
}
