package org.openmrs.module.appointments.web.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getAppointmentDates;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getEndDate;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getFirstOriginalAppointmentInThePattern;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getNewAppointmentDates;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getSelectedDayCodes;

@Component
@Qualifier("weeklyRecurringAppointmentsGenerationService")
public class WeeklyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsService {

    @Override
    public List<Appointment> generateAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        List<Integer> selectedDayCodes = getSelectedDayCodes(recurringPattern.getDaysOfWeek());
        final AppointmentRequest appointmentRequest = recurringAppointmentRequest.getAppointmentRequest();
        Date endDate = getEndDate(recurringPattern, appointmentRequest.getStartDateTime());
        List<Pair<Date, Date>> appointmentDates = getAppointmentDates(endDate, appointmentRequest.getStartDateTime(),
                appointmentRequest.getEndDateTime(), selectedDayCodes, recurringAppointmentRequest.getRecurringPattern().getPeriod());
        List<Appointment> appointments = createAppointments(appointmentDates, appointmentRequest);
        return sort(appointments);
    }

    @Override
    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             RecurringAppointmentRequest recurringAppointmentRequest) {
        final RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        if (appointmentRecurringPattern.getEndDate() == null) {
            appointmentRecurringPattern.setFrequency(recurringPattern
                    .getFrequency() - appointmentRecurringPattern.getFrequency() + 1);
        } else appointmentRecurringPattern.setEndDate(recurringPattern.getEndDate());

        List<Appointment> activeAppointments = new ArrayList<>(appointmentRecurringPattern.getActiveAppointments());

        final Appointment originalFirstAppointmentInThePattern = getFirstOriginalAppointmentInThePattern(activeAppointments,
                new ArrayList<>(appointmentRecurringPattern.getRemovedAppointments()));
        final Date startDateTime = originalFirstAppointmentInThePattern.getStartDateTime();
        final Date endDateTime = originalFirstAppointmentInThePattern.getEndDateTime();

        Date endDate = getEndDate(recurringPattern, startDateTime);

        List<String> daysOfWeek = stream(appointmentRecurringPattern.getDaysOfWeek().split(",")).collect(toList());
        List<Integer> selectedDayCodes = getSelectedDayCodes(daysOfWeek);

        final List<Pair<Date, Date>> appointmentStartAndEndDates = getAppointmentDates(endDate, startDateTime,
                endDateTime, selectedDayCodes, recurringPattern.getPeriod());
        final List<Pair<Date, Date>> newAppointmentDates = getNewAppointmentDates(activeAppointments, appointmentStartAndEndDates);

        String uuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        recurringAppointmentRequest.getAppointmentRequest().setUuid(null);

        final List<Appointment> newAppointments = createAppointments(newAppointmentDates, recurringAppointmentRequest.getAppointmentRequest());

        recurringAppointmentRequest.getAppointmentRequest().setUuid(uuid);
        activeAppointments.addAll(newAppointments);
        addRemovedAndRelatedAppointments(activeAppointments, appointmentRecurringPattern);
        return sort(activeAppointments);
    }

}
