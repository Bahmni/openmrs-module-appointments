package org.openmrs.module.appointments.web.service;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.DAY;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.WEEK;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.valueOf;

@Component
public interface AppointmentRecurringPatternUpdateService {

    AppointmentRecurringPattern getUpdatedRecurringPattern(RecurringAppointmentRequest recurringAppointmentRequest);
}
