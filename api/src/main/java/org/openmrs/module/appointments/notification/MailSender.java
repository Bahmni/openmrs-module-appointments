package org.openmrs.module.appointments.notification;

public interface MailSender {
    void send(String subject, String body, String[] to, String[] cc, String[] bcc);
}
