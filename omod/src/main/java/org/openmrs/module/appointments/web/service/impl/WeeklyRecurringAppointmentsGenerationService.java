package org.openmrs.module.appointments.web.service.impl;

import liquibase.sqlgenerator.core.SelectSequencesGeneratorDB2;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("weeklyRecurringAppointmentsGenerationService")
public class WeeklyRecurringAppointmentsGenerationService extends AbstractRecurringAppointmentsService {

    private RecurringAppointmentRequest recurringAppointmentRequest;

    @Override
    public List<Appointment> generateAppointments(RecurringAppointmentRequest recurringAppointmentRequest) {
        this.recurringAppointmentRequest = recurringAppointmentRequest;
        RecurringPattern recurringPattern = recurringAppointmentRequest.getRecurringPattern();
        List<Integer> selectedDayCodes = getSelectedDayCodes(recurringPattern.getDaysOfWeek());
        Date endDate = getEndDate(recurringPattern.getPeriod(), recurringPattern.getFrequency(),
                recurringPattern.getEndDate(), selectedDayCodes, false);
        List<Pair<Date, Date>> appointmentDates = getAppointmentDates(endDate,
                DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime()),
                DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime()),
                selectedDayCodes);
        List<Appointment> appointments = createAppointments(appointmentDates, recurringAppointmentRequest.getAppointmentRequest());
        return sort(appointments);
    }

    public Date getEndDate(int period, Integer frequency, Date endDate, List<Integer> selectedDayCodes, Boolean isEdit) {
        if (endDate != null) {
            return endDate;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        int dayCode = isEdit ? getDayCodeIfItsARelatedAppointment(selectedDayCodes, calendar.get(Calendar.DAY_OF_WEEK)) : calendar.get(Calendar.DAY_OF_WEEK);
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(dayCode, selectedDayCodes);
        int daysToBeAdded = 0;
        if (frequency % selectedDaysPerWeek.size() != 0) {
            daysToBeAdded = 1;
            int selectedDayCode = selectedDaysPerWeek.get(frequency % selectedDaysPerWeek.size() - 1);
            if (dayCode > selectedDayCode) {
                daysToBeAdded += Calendar.DAY_OF_WEEK - (dayCode - selectedDayCode);
            } else {
                daysToBeAdded += selectedDayCode - dayCode;
            }
        }
        calendar.add(Calendar.DAY_OF_WEEK, (Calendar.DAY_OF_WEEK * period *
                (frequency / selectedDaysPerWeek.size()) - 1) + daysToBeAdded);
        return calendar.getTime();
    }

    private List<Pair<Date, Date>> getAppointmentDates(Date endDate, Calendar startCalender, Calendar endCalender,
                                                       List<Integer> selectedDayCodes) {
        List<Pair<Date, Date>> appointmentDates = new ArrayList<>();
        Date currentAppointmentDate = startCalender.getTime();
        while (!currentAppointmentDate.after(endDate)) {
            appointmentDates.addAll(getAppointmentDatesForAWeek(endDate, startCalender, endCalender, selectedDayCodes));
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (recurringAppointmentRequest.getRecurringPattern().getPeriod()));
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (recurringAppointmentRequest.getRecurringPattern().getPeriod()));
            currentAppointmentDate = startCalender.getTime();
        }
        return appointmentDates;
    }

    private List<Pair<Date, Date>> getAppointmentDatesForAWeek(Date endDate, Calendar startCalendar, Calendar endCalendar,
                                                               List<Integer> selectedDayCodes) {
        List<Pair<Date, Date>> appointmentDates = new ArrayList<>();
        for (int dayCode : selectedDayCodes) {
            Calendar startCalenderInstance = Calendar.getInstance();
            Calendar endCalenderInstance = Calendar.getInstance();
            startCalenderInstance.setTime(startCalendar.getTime());
            endCalenderInstance.setTime(endCalendar.getTime());
            updateCalendarInstancesToNextSelectedDay(startCalenderInstance, endCalenderInstance, dayCode);
            if (startCalenderInstance.getTime().after(endDate)) {
                continue;
            }
            appointmentDates.add(new ImmutablePair<>(startCalenderInstance.getTime(), endCalenderInstance.getTime()));
        }
        return appointmentDates;
    }

    private List<Integer> getSortedSelectedDayCodes(int startDayCode, List<Integer> selectedDayCodes) {
        List<Integer> selectedDaysPerWeek = new ArrayList<>();
        for (int dayCode : selectedDayCodes) {
            if (dayCode >= startDayCode) selectedDaysPerWeek.add(dayCode);
        }
        for (int dayCode : selectedDayCodes) {
            if (dayCode < startDayCode) {
                selectedDaysPerWeek.add(dayCode);
            } else break;
        }
        return selectedDaysPerWeek;
    }

    @Override
    public List<Appointment> addAppointments(AppointmentRecurringPattern appointmentRecurringPattern,
                                             RecurringAppointmentRequest recurringAppointmentRequest) {
        List<String> daysOfWeek = Arrays.stream(appointmentRecurringPattern.getDaysOfWeek().split(",")).collect(Collectors.toList());
        List<Integer> selectedDayCodes = getSelectedDayCodes(daysOfWeek);
        List<Appointment> activeAppointments = new ArrayList<>(appointmentRecurringPattern.getActiveAppointments());
        List<Appointment> relatedAppointments = new ArrayList<>(appointmentRecurringPattern.getRelatedAppointments());
        List<Appointment> removedAppointments = new ArrayList<>(appointmentRecurringPattern.getRemovedAppointments());
        List<Appointment> appointments = new ArrayList<>(activeAppointments);
        Collections.sort(appointments, Comparator.comparing(Appointment::getDateFromStartDateTime));

        this.recurringAppointmentRequest = recurringAppointmentRequest;
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointments.get(appointments.size() - 1).getStartDateTime());
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointments.get(appointments.size() - 1).getEndDateTime());

        if (appointmentRecurringPattern.getEndDate() == null)
            appointmentRecurringPattern.setFrequency(recurringAppointmentRequest.getRecurringPattern().getFrequency() - appointmentRecurringPattern.getFrequency() + 1);
        else appointmentRecurringPattern.setEndDate(recurringAppointmentRequest.getRecurringPattern().getEndDate());

        Date endDate = getEndDate(appointmentRecurringPattern.getPeriod(), appointmentRecurringPattern.getFrequency(),
                appointmentRecurringPattern.getEndDate(), selectedDayCodes, true);

        Calendar startCalender = DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getStartDateTime());
        Calendar endCalender = DateUtil.getCalendar(recurringAppointmentRequest.getAppointmentRequest().getEndDateTime());

        int lastAppointmentDayCode = getDayCodeIfItsARelatedAppointment(selectedDayCodes, startCalender.get(Calendar.DAY_OF_WEEK));
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(lastAppointmentDayCode, selectedDayCodes);
        List<Integer> updatedSelectedDayCodes = new ArrayList<>();
        updatedSelectedDayCodes = selectedDayCodes.get(selectedDayCodes.size() - 1).equals(lastAppointmentDayCode) ?
                updatedSelectedDayCodes : getSelectedDayCodesForExtend(selectedDaysPerWeek, lastAppointmentDayCode);
        int dayCode = selectedDaysPerWeek.size() > 1 ? selectedDaysPerWeek.get(1) : 0;
        updateCalendarInstancesToNextSelectedDay(startCalender, endCalender,dayCode);
        String uuid = recurringAppointmentRequest.getAppointmentRequest().getUuid();
        recurringAppointmentRequest.getAppointmentRequest().setUuid(null);
        if (updatedSelectedDayCodes.size() != 0) {
            appointments.addAll(createAppointments(getAppointmentDatesForAWeek(endDate, startCalender, endCalender, updatedSelectedDayCodes),
                    recurringAppointmentRequest.getAppointmentRequest()));
            startCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
            endCalender.add(Calendar.DATE, Calendar.DAY_OF_WEEK * (appointmentRecurringPattern.getPeriod()) - 1);
        }
        appointments.addAll(createAppointments(getAppointmentDates(endDate, startCalender, endCalender, selectedDayCodes),
                recurringAppointmentRequest.getAppointmentRequest()));
        recurringAppointmentRequest.getAppointmentRequest().setUuid(uuid);
        appointments.addAll(removedAppointments);
        appointments.addAll(relatedAppointments);
        return sort(appointments);
    }

    private void updateCalendarInstancesToNextSelectedDay(Calendar startCalender, Calendar endCalender, int dayCode) {
        int startDayCode = startCalender.get(Calendar.DAY_OF_WEEK);
        int daysToBeAdded = dayCode - startDayCode;
        if (dayCode < startDayCode)
            daysToBeAdded += Calendar.DAY_OF_WEEK;
        startCalender.add(Calendar.DAY_OF_WEEK, daysToBeAdded);
        endCalender.add(Calendar.DAY_OF_WEEK, daysToBeAdded);
    }
    private int getDayCodeIfItsARelatedAppointment(List<Integer> selectedDayCodes, int dayCode){
        while(!selectedDayCodes.contains(dayCode)) {
            dayCode = dayCode == 0 ?  7 : dayCode - 1 ;
        }
        return dayCode;
    }
    private List<Integer> getSelectedDayCodesForExtend(List<Integer> selectedDaysPerWeek, int dayCode) {
        int dayCodeIndex = 0;
        List<Integer> updatedSelectedDayCodes = new ArrayList<>();
        for (int i = 0; i < selectedDaysPerWeek.size(); i++) {
            if (dayCode == selectedDaysPerWeek.get(i)) {
                dayCodeIndex = i;
                break;
            }
        }
        for (int i = dayCodeIndex + 1; i < selectedDaysPerWeek.size(); i++) {
            updatedSelectedDayCodes.add(selectedDaysPerWeek.get(i));
        }
        return updatedSelectedDayCodes;
    }

    private List<Integer> getSelectedDayCodes(List<String> selectedDays) {
        return selectedDays.stream().map(this::getDayCodeFromDay).sorted().collect(Collectors.toList());
    }

    private int getDayCodeFromDay(String day) {
        int dayCode = 0;
        switch (day.toUpperCase()) {
            case "SUNDAY":
                dayCode = Calendar.SUNDAY;
                break;
            case "MONDAY":
                dayCode = Calendar.MONDAY;
                break;
            case "TUESDAY":
                dayCode = Calendar.TUESDAY;
                break;
            case "WEDNESDAY":
                dayCode = Calendar.WEDNESDAY;
                break;
            case "THURSDAY":
                dayCode = Calendar.THURSDAY;
                break;
            case "FRIDAY":
                dayCode = Calendar.FRIDAY;
                break;
            case "SATURDAY":
                dayCode = Calendar.SATURDAY;
                break;
            default:
                break;
        }
        return dayCode;
    }
}
