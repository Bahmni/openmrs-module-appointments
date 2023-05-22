package org.openmrs.module.appointments.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.AppointmentReminderService;
import org.openmrs.module.appointments.service.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class AppointmentReminderServiceImpl implements AppointmentReminderService {
    private SMSService smsService;
//    @Autowired
    public AppointmentReminderServiceImpl(SMSService smsService) {
        this.smsService = smsService;
    }

//    @Autowired
//    SMSService smsService;

    @Override
    @Transactional(readOnly = true)
    public Object sendAppointmentReminderSMS(Appointment appointment) {
        String message = smsService.getAppointmentMessage(appointment.getPatient().getGivenName(), appointment.getPatient().getFamilyName(), appointment.getPatient().getId(), appointment.getDateFromStartDateTime(), appointment.getStartDateTime().getTime(), appointment.getService().getName(), appointment.getProvider().getName(), "+91987654321");
        return smsService.sendSMS(appointment.getPatient().getAttribute("phoneNumber").getValue(), message);

    }
}