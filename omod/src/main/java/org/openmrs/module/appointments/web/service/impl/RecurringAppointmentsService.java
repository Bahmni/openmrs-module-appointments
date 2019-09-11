package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringAppointmentsService {

    @Autowired
    @Qualifier("dailyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsService dailyRecurringAppointmentsGenerationService;

    @Autowired
    @Qualifier("weeklyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsService weeklyRecurringAppointmentsGenerationService;

    //todo Use strategy for day, week and month logics
    public List<Appointment> generateRecurringAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        RecurringAppointmentType recurringAppointmentType =
                RecurringAppointmentType.valueOf(recurringAppointmentRequest.getRecurringPattern().getType().toUpperCase());
        switch (recurringAppointmentType) {
            case WEEK:
                appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);
                break;
            case DAY:
                appointments = dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);
                break;
        }
        return appointments;
    }

    public List<Appointment> getUpdatedSetOfAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                          RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> newSetOfAppointments;
        if (appointmentRecurringPattern.getEndDate() == null) {
            if (recurringAppointmentRequest.getRecurringPattern().isFrequencyIncreased(appointmentRecurringPattern.getFrequency())) {
                newSetOfAppointments = addRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            } else if (recurringAppointmentRequest.getRecurringPattern().isFrequencyDecreased(appointmentRecurringPattern.getFrequency())) {
                newSetOfAppointments = deleteRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            } else {
                return new ArrayList<>(appointmentRecurringPattern.getAppointments());
            }
        } else {
            if (recurringAppointmentRequest.getRecurringPattern().isAfter(appointmentRecurringPattern.getEndDate())) {
                newSetOfAppointments = addRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            } else if (recurringAppointmentRequest.getRecurringPattern().isBefore(appointmentRecurringPattern.getEndDate())) {
                newSetOfAppointments = deleteRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
            } else {
                return new ArrayList<>(appointmentRecurringPattern.getAppointments());
            }
        }
        return newSetOfAppointments;
    }

    private List<Appointment> addRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                       RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        switch (appointmentRecurringPattern.getType()) {
            case WEEK:
                appointments = weeklyRecurringAppointmentsGenerationService.addAppointments(
                        appointmentRecurringPattern, recurringAppointmentRequest);
                break;
            case DAY:
                appointments = dailyRecurringAppointmentsGenerationService.addAppointments(
                        appointmentRecurringPattern, recurringAppointmentRequest);
                break;
        }
        return appointments;
    }

    private List<Appointment> deleteRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                          RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        switch (appointmentRecurringPattern.getType()) {
            case WEEK:
                appointments = weeklyRecurringAppointmentsGenerationService
                        .removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
                break;
            case DAY:
                appointments = dailyRecurringAppointmentsGenerationService
                        .removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
                break;
        }
        return appointments;
    }

}
