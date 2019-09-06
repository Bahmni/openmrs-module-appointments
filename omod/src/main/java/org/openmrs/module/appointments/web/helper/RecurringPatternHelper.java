package org.openmrs.module.appointments.web.helper;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AbstractAppointmentRecurringPatternMapper;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.impl.DailyRecurringAppointmentsGenerationService;
import org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringPatternHelper {

    @Autowired
    @Qualifier("dailyRecurringAppointmentsGenerationService")
    DailyRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Autowired
    @Qualifier("weeklyRecurringAppointmentsGenerationService")
    WeeklyRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

    private AppointmentMapper appointmentMapper;

    // TODO Remove this Autowire and constructor
    @Autowired
    public RecurringPatternHelper(AppointmentMapper appointmentMapper) {
        this.appointmentMapper = appointmentMapper;
    }

    //todo Use strategy for day, week and month logics
    public List<Appointment> generateRecurringAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                                           RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> appointments = new ArrayList<>();
        try {
            switch (appointmentRecurringPattern.getType()) {
                case WEEK:
                    appointments = weeklyRecurringAppointmentsGenerationService.getAppointments(recurringAppointmentRequest);
                    break;
                case DAY:
                    appointments = dailyRecurringAppointmentsGenerationService.getAppointments(recurringAppointmentRequest);
                    break;
            }
        } catch (Exception e) {
        }
        return appointments;
    }

}
