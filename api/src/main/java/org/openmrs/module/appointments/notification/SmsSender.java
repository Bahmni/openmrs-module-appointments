package org.openmrs.module.appointments.notification;

import java.util.Date;

public interface SmsSender {
    void send(String recipientMobileNumber, String patientName, String smsType, String serviceName, Date startDateTime, String teleLink);
}
