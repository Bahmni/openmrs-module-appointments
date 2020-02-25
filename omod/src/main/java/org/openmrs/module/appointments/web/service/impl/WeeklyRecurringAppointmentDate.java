package org.openmrs.module.appointments.web.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.RecurringPattern;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;

public class WeeklyRecurringAppointmentDate {

    private static final int PREVIOUS_DAY_AMOUNT = -1;


    static List<Pair<Date, Date>> getAppointmentDates(Date endDate, Date appointmentStartDate, Date appointmentEndDate,
                                                      List<Integer> selectedDayCodes, int period) {
        List<Pair<Date, Date>> appointmentDates = new ArrayList<>();
        Date currentAppointmentDate = appointmentStartDate;
        Calendar startCalendar = DateUtil.getCalendar(appointmentStartDate);
        Calendar endCalendar = DateUtil.getCalendar(appointmentEndDate);

        while (!currentAppointmentDate.after(endDate)) {
            appointmentDates.addAll(getAppointmentDatesForAWeek(endDate, startCalendar, endCalendar, selectedDayCodes));
            moveCalendarToWeeklyPeriod(startCalendar, period);
            moveCalendarToWeeklyPeriod(endCalendar, period);
            currentAppointmentDate = startCalendar.getTime();
        }
        return appointmentDates;
    }

    public static Date getEndDate(RecurringPattern recurringPattern, Date startDateTime) {
        final Date endDate = recurringPattern.getEndDate();
        if (endDate != null) {
            return endDate;
        }
        Integer frequency = recurringPattern.getFrequency();
        final int period = recurringPattern.getPeriod();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDateTime);
        List<Integer> selectedDaysPerWeek = getSortedSelectedDayCodes(calendar.get(DAY_OF_WEEK),
                getSelectedDayCodes(recurringPattern.getDaysOfWeek()));

        final int numberOfDaysSelected = selectedDaysPerWeek.size();

        int timesOfPeriod = frequency / numberOfDaysSelected;

        calendar.add(Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK * period * timesOfPeriod);

        int remainingFrequency = frequency % numberOfDaysSelected;
        if (remainingFrequency > 0) {
            moveCalendarToRemainingFrequency(remainingFrequency, calendar, selectedDaysPerWeek);
        } else {
            // moves calendar a day before to keep end date in pattern
            calendar.add(DATE, PREVIOUS_DAY_AMOUNT);
        }
        return calendar.getTime();
    }

    private static void moveCalendarToRemainingFrequency(Integer remainingFrequency, Calendar calendar, List<Integer> selectedDaysPerWeek) {
        final Integer dayCodeOfLastOccurrence = getDayCodeOfLastOccurrence(remainingFrequency, selectedDaysPerWeek);
        while (calendar.get(DAY_OF_WEEK) != dayCodeOfLastOccurrence) {
            calendar.add(DATE, 1);
        }
    }

    private static Integer getDayCodeOfLastOccurrence(Integer currentFrequency, List<Integer> selectedDaysPerWeek) {
        return selectedDaysPerWeek.get(currentFrequency - 1);
    }

    private static void moveCalendarToWeeklyPeriod(Calendar calendar, int period) {
        calendar.add(DATE, getDateAmountToMoveCalendar(period));
    }

    static List<Integer> getSelectedDayCodes(List<String> selectedDays) {
        return selectedDays.stream().map(WeeklyRecurringAppointmentDate::getDayCodeFromDay).sorted()
                .collect(Collectors.toList());
    }

    static List<Pair<Date, Date>> getNewAppointmentDates(List<Appointment> activeAppointments, List<Pair<Date, Date>> appointmentStartAndEndDates) {
        List<Pair<Date, Date>> originalAppointmentDates = new ArrayList<>();
        activeAppointments.forEach(activeAppointment -> {
            for (Pair<Date, Date> appointmentStartAndEndDate : appointmentStartAndEndDates) {
                final Appointment relatedAppointment = activeAppointment.getRelatedAppointment();
                if (relatedAppointment != null && areAppointmentDatesSame(relatedAppointment, appointmentStartAndEndDate)) {
                    originalAppointmentDates.add(appointmentStartAndEndDate);
                    break;
                } else if (areAppointmentDatesSame(activeAppointment, appointmentStartAndEndDate)) {
                    originalAppointmentDates.add(appointmentStartAndEndDate);
                    break;
                }
            }

        });
        final List<Pair<Date, Date>> appointmentDates = new ArrayList<>(appointmentStartAndEndDates);
        appointmentDates.removeAll(originalAppointmentDates);
        return appointmentDates;
    }

    private static boolean areAppointmentDatesSame(Appointment appointment, Pair<Date, Date> appointmentStartAndEndDate) {
        return new Date(appointment.getStartDateTime().getTime()).equals(appointmentStartAndEndDate.getLeft())
                && new Date(appointment.getEndDateTime().getTime()).equals(appointmentStartAndEndDate.getRight());
    }

    private static int getDateAmountToMoveCalendar(int period) {
        return DAY_OF_WEEK * period;
    }

    private static List<Pair<Date, Date>> getAppointmentDatesForAWeek(Date endDate, Calendar startCalendar, Calendar endCalendar,
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

    private static void updateCalendarInstancesToNextSelectedDay(Calendar startCalender, Calendar endCalender, int dayCode) {
        int startDayCode = startCalender.get(DAY_OF_WEEK);
        int daysToBeAdded = dayCode - startDayCode;
        if (dayCode < startDayCode)
            daysToBeAdded += DAY_OF_WEEK;
        startCalender.add(DAY_OF_WEEK, daysToBeAdded);
        endCalender.add(DAY_OF_WEEK, daysToBeAdded);
    }

    private static List<Integer> getSortedSelectedDayCodes(int startDayCode, List<Integer> selectedDayCodes) {
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

    private static int getDayCodeFromDay(String day) {
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

    static Appointment getFirstOriginalAppointmentInThePattern(List<Appointment> activeAppointments, List<Appointment> removedAppointments) {
        List<Appointment> originalAppointmentsInThePattern = new ArrayList<>();
        final List<Appointment> activeAndRemovedAppointments = new ArrayList<>(activeAppointments);
        activeAndRemovedAppointments.addAll(removedAppointments);
        activeAndRemovedAppointments.forEach(appointment -> {
            final Appointment relatedAppointment = appointment.getRelatedAppointment();
            originalAppointmentsInThePattern.add(relatedAppointment == null ? appointment : relatedAppointment);
        });

        originalAppointmentsInThePattern.sort(Comparator.comparing(Appointment::getDateFromStartDateTime));
        return originalAppointmentsInThePattern.get(0);
    }
}
