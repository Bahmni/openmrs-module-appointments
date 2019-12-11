package org.openmrs.module.appointments.web.mapper;

import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.WEEK;
import static org.openmrs.module.appointments.service.impl.RecurringAppointmentType.valueOf;

@Component
public class RecurringPatternMapper {

    public AppointmentRecurringPattern fromRequest(RecurringPattern recurringPattern) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setEndDate(recurringPattern.getEndDate());
        appointmentRecurringPattern.setPeriod(recurringPattern.getPeriod());
        appointmentRecurringPattern.setFrequency(recurringPattern.getFrequency());
        String recurringPatternType = recurringPattern.getType();
        appointmentRecurringPattern.setType(valueOf(recurringPatternType.toUpperCase()));
        if (appointmentRecurringPattern.getType() == WEEK) {
            appointmentRecurringPattern.setDaysOfWeek(recurringPattern.getDaysOfWeek().stream().map(String::toUpperCase)
                    .collect(Collectors.joining(",")));
        }
        return appointmentRecurringPattern;
    }

    public RecurringPattern mapToResponse(AppointmentRecurringPattern appointmentRecurringPattern) {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setType(appointmentRecurringPattern.getType().toString());
        recurringPattern.setPeriod(appointmentRecurringPattern.getPeriod());
        Date endDate = appointmentRecurringPattern.getEndDate();
        if (endDate != null) {
            recurringPattern.setEndDate(endDate);
        } else {
            recurringPattern.setFrequency(appointmentRecurringPattern.getFrequency());
        }
        List<String> daysOfWeek = appointmentRecurringPattern.getDaysOfWeek() != null ?
                Arrays.asList(appointmentRecurringPattern.getDaysOfWeek().split(",")) : null;
        recurringPattern.setDaysOfWeek(daysOfWeek);
        return recurringPattern;
    }

    public AppointmentRecurringPattern cloneAppointmentRecurringPattern(AppointmentRecurringPattern recurringPattern){
        AppointmentRecurringPattern recurringPatternClone = new AppointmentRecurringPattern();
        recurringPatternClone.setType(recurringPattern.getType());
        recurringPatternClone.setId(recurringPattern.getId());
        recurringPatternClone.setEndDate(recurringPattern.getEndDate());
        recurringPatternClone.setDaysOfWeek(recurringPattern.getDaysOfWeek());
        recurringPatternClone.setFrequency(recurringPattern.getFrequency());
        recurringPatternClone.setPeriod(recurringPattern.getPeriod());
        recurringPatternClone.setAppointments(recurringPattern.getAppointments());
        return recurringPatternClone;
    }

}
