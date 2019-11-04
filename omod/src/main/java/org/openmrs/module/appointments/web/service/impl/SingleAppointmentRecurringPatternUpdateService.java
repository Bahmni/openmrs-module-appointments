package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SingleAppointmentRecurringPatternUpdateService {

    @Autowired
    private AppointmentsService appointmentsService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    public Appointment getUpdatedAppointment(RecurringAppointmentRequest recurringAppointmentRequest) {
        String uuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        Appointment appointment = appointmentsService.getAppointmentByUuid(uuid);
        Appointment newAppointment;
        final AppointmentRecurringPattern appointmentRecurringPattern = appointment.getAppointmentRecurringPattern();
        if (appointment.getRelatedAppointment() != null) {
            newAppointment = appointment;
            appointmentMapper.mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), newAppointment);
        } else {
            newAppointment = new Appointment();
            newAppointment.setPatient(appointment.getPatient());
            appointmentMapper.mapAppointmentRequestToAppointment(recurringAppointmentRequest.getAppointmentRequest(), newAppointment);
            appointment.setVoided(true);
            newAppointment.setRelatedAppointment(appointment);
            newAppointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
            appointmentRecurringPattern.getAppointments().add(newAppointment);
        }
        return newAppointment;
    }
}
