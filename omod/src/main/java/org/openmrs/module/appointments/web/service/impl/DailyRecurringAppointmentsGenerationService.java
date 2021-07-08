package org.openmrs.module.appointments.web.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.service.AbstractRecurringAppointmentsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Component
@Qualifier("dailyRecurringAppointmentsGenerationService")
public class DailyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsService {

    @Override
    public List<Appointment> generateAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        Date endDate = getEndDate(recurringPattern.getPeriod(), recurringPattern.getFrequency(), recurringPattern.getEndDate(), recurringAppointmentRequest);
        List<Pair<Date, Date>> appointmentDates = getAppointmentDates(endDate,
                DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime()),
                DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime()),
                recurringAppointmentRequest);
        return createAppointments(appointmentDates, recurringAppointmentRequest.getAppointmentRequest());
    }

    private Date getEndDate(int period, Integer frequency, Date endDate, RecurringAppointmentRequest recurringAppointmentRequest) {
        if (endDate != null) {
            return endDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        calendar.add(Calendar.DAY_OF_MONTH, period * (frequency - 1));
        return calendar.getTime();
    }

    private List<Pair<Date, Date>> getAppointmentDates(Date endDate, Calendar startCalendar, Calendar endCalendar, RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Pair<Date, Date>> appointmentDates = new ArrayList<>();
        Date currentAppointmentDate = recurringAppointmentRequest.getAppointmentRequest().getStartDateTime();
        while (!currentAppointmentDate.after(endDate)) {
            appointmentDates.add(new ImmutablePair<>(startCalendar.getTime(), endCalendar.getTime()));
            startCalendar.add(Calendar.DAY_OF_YEAR, recurringAppointmentRequest.getRecurringPattern().getPeriod());
            endCalendar.add(Calendar.DAY_OF_YEAR, recurringAppointmentRequest.getRecurringPattern().getPeriod());
            currentAppointmentDate = startCalendar.getTime();
        }
        return appointmentDates;
    }

    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             RecurringAppointmentRequest recurringAppointmentRequest) {
        List<Appointment> activeAppointments = new ArrayList<>(appointmentRecurringPattern.getActiveAppointments());
        List<Appointment> appointments = new ArrayList<>(activeAppointments);
        appointments.sort(Comparator.comparing(Appointment::getDateFromStartDateTime));
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(appointments.size() - 1).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(appointments.size() - 1).getEndDateTime());
        if (appointmentRecurringPattern.getEndDate() == null)
            appointmentRecurringPattern.setFrequency(
                    recurringAppointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1);
        else appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());
        Date endDate = getEndDate(appointmentRecurringPattern.getPeriod(), appointmentRecurringPattern.getFrequency(),
                appointmentRecurringPattern.getEndDate(), recurringAppointmentRequest);
        Calendar startCalendar = DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        Calendar endCalendar = DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());
        startCalendar.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        endCalendar.add(Calendar.DAY_OF_YEAR, appointmentRecurringPattern.getPeriod());
        String uuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        recurringAppointmentRequest.getAppointmentRequest().setUuid(null);
        appointments.addAll(createAppointments(getAppointmentDates(endDate, startCalendar, endCalendar, recurringAppointmentRequest),
                recurringAppointmentRequest.getAppointmentRequest()));
        recurringAppointmentRequest.getAppointmentRequest().setUuid(uuid);
        addRemovedAndRelatedAppointments(appointments, appointmentRecurringPattern);
        return sort(appointments);
    }
}
