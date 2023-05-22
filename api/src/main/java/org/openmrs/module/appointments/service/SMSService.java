package org.openmrs.module.appointments.service;

import java.util.Date;

public interface SMSService {

    String getAppointmentMessage(String name,String familyName ,int id, Date appointmentDate, long time,String service,String provider,String helpDeskPhone);

    Object sendSMS(String phoneNumber, String message);
}