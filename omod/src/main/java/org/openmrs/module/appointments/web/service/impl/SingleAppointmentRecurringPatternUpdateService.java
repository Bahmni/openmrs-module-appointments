package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.AppointmentRecurringPatternUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

@Component
@Qualifier("singleAppointmentRecurringPatternUpdateService")
public class SingleAppointmentRecurringPatternUpdateService implements AppointmentRecurringPatternUpdateService {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private AppointmentServiceHelper appointmentServiceHelper;

    @Override
    public AppointmentRecurringPattern getUpdatedRecurringPattern(RecurringAppointmentRequest recurringAppointmentRequest){
        String uuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        Appointment newAppointment;
        if (appointment.getRelatedAppointment() != null) {
            newAppointment = appointment;
        } else {
            newAppointment = getNewAppointmentFrom(appointment);
            appointment.setVoided(true);
            newAppointment.setRelatedAppointment(appointment);
        }
        appointmentMapper.mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), newAppointment);
        setAppointmentAudit(newAppointment);
        final AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        appointmentRecurringPattern.getAppointments().add(newAppointment);
        return appointmentRecurringPattern;
    }


    private Appointment getNewAppointmentFrom(Appointment oldAppointment){
        Appointment appointment = new Appointment();
        appointment.setService(oldAppointment.getService());
        appointment.setStatus(oldAppointment.getStatus());
        appointment.setEndDateTime(oldAppointment.getEndDateTime());
        appointment.setStartDateTime(oldAppointment.getStartDateTime());
        appointment.setServiceType(oldAppointment.getServiceType());
        appointment.setPatient(oldAppointment.getPatient());
        appointment.setLocation(oldAppointment.getLocation());
        appointment.setAppointmentKind(oldAppointment.getAppointmentKind());
        appointment.setComments(oldAppointment.getComments());
        appointment.setAppointmentAudits(new HashSet<>());
        appointment.setAppointmentNumber(oldAppointment.getAppointmentNumber());
        return appointment;
    }

    //TODO : duplicate code from service, needs to be abstractred and refactored
    private void setAppointmentAudit(Appointment appointment) {
        try {
            String notes = appointmentServiceHelper.getAppointmentAsJsonString(appointment);
            updateAppointmentAudits(appointment, notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO : duplicate code from service, needs to be abstractred and refactored
    private void updateAppointmentAudits(Appointment appointment, String notes) {
        AppointmentAudit appointmentAudit = appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes);
        if (appointment.getAppointmentAudits() != null) {
            appointment.getAppointmentAudits().addAll(new HashSet<>(Collections.singletonList(appointmentAudit)));
        } else {
            appointment.setAppointmentAudits(new HashSet<>(Collections.singletonList(appointmentAudit)));
        }
    }

}
