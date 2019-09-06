package org.openmrs.module.appointments.web.service.impl;

import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringAppointmentsService {

    @Autowired
    @Qualifier("dailyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Autowired
    @Qualifier("weeklyRecurringAppointmentsGenerationService")
    AbstractRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

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

}
