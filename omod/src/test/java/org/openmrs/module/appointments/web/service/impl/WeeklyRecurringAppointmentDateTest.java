package org.openmrs.module.appointments.web.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.util.RecurringPatternBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Calendar.APRIL;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.SATURDAY;
import static org.junit.Assert.assertEquals;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getAppointmentDates;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getEndDate;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getFirstOriginalAppointmentInThePattern;
import static org.openmrs.module.appointments.web.service.impl.WeeklyRecurringAppointmentDate.getNewAppointmentDates;

public class WeeklyRecurringAppointmentDateTest {

    private static final String WEEK = "week";

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndEvenPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 21, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndEvenPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, APRIL, 2, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndOddPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, APRIL, 4, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndOddPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, APRIL, 23, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndEvenPeriodAndRecurrenceStartDayFallsBeforeSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 16, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndEvenPeriodAndRecurrenceStartDayFallsBeforeSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 28, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndOddPeriodAndRecurrenceStartDayFallsBeforeSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 30, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndOddPeriodAndRecurrenceStartDayFallsBeforeSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, APRIL, 18, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndEvenPeriodAndRecurrenceStartDayFallsAfterSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "TUESDAY", "WEDNESDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 22, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 10, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndEvenPeriodAndRecurrenceStartDayFallsAfterSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "TUESDAY", "WEDNESDAY"))
                .get();

        final Date startDate = getDate(2020, FEBRUARY, 22, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 20, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenOddFrequencyAndOddPeriodAndRecurrenceStartDayFallsAfterSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(5)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "TUESDAY", "WEDNESDAY"))
                .get();


        final Date startDate = getDate(2020, FEBRUARY, 22, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 17, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForGivenEvenFrequencyAndOddPeriodAndRecurrenceStartDayFallsAfterSelectedDays() {

        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(6)
                .setPeriod(3).setType(WEEK).setDaysOfWeek(asList("MONDAY", "TUESDAY", "WEDNESDAY"))
                .get();


        final Date startDate = getDate(2020, FEBRUARY, 22, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, APRIL, 3, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForWhenFrequencyIsOneAndPeriodIsOne() {
        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(1)
                .setPeriod(1).setType(WEEK).setDaysOfWeek(asList("MONDAY", "TUESDAY", "WEDNESDAY"))
                .get();


        final Date startDate = getDate(2020, FEBRUARY, 22, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, FEBRUARY, 24, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForWhenFrequencyIsFourteenAndPeriodIsOne() {
        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(14)
                .setPeriod(1).setType(WEEK).setDaysOfWeek(asList("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
                        "THURSDAY,", "FRIDAY", "SATURDAY")).get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, FEBRUARY, 29, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnEndDateForWhenFrequencyIsFourteenAndPeriodIsTwo() {
        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(14)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY",
                        "THURSDAY,", "FRIDAY", "SATURDAY")).get();

        final Date startDate = getDate(2020, FEBRUARY, 16, 14, 0, 0);

        final Date endDate = getEndDate(recurringPattern, startDate);

        assertEquals(getDate(2020, MARCH, 14, 14, 0, 0), endDate);
    }

    @Test
    public void shouldReturnAppointmentsForGivenOddFrequencyAndEvenPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {
        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(3)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date appointmentStartDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);
        final Date appointmentEndDate = getDate(2020, FEBRUARY, 21, 14, 30, 0);
        final Date endDate = getEndDate(recurringPattern, appointmentStartDate);

        final List<Pair<Date, Date>> appointmentDates = getAppointmentDates(endDate, appointmentStartDate, appointmentEndDate, asList(MONDAY, SATURDAY), 2);

        assertEquals(3, appointmentDates.size());

        appointmentDates.sort(Comparator.comparing(Pair::getLeft));
        assertEquals(getDate(2020, FEBRUARY, 22, 14, 0, 0), appointmentDates.get(0).getLeft());
        assertEquals(getDate(2020, FEBRUARY, 24, 14, 0, 0), appointmentDates.get(1).getLeft());
        assertEquals(getDate(2020, MARCH, 7, 14, 0, 0), appointmentDates.get(2).getLeft());
    }

    @Test
    public void shouldReturnAppointmentsForGivenEvenFrequencyAndEvenPeriodAndRecurrenceStartDayFallBetweenSelectedDays() {
        final RecurringPattern recurringPattern = new RecurringPatternBuilder().setFrequency(4)
                .setPeriod(2).setType(WEEK).setDaysOfWeek(asList("MONDAY", "SATURDAY"))
                .get();

        final Date appointmentStartDate = getDate(2020, FEBRUARY, 21, 14, 0, 0);
        final Date appointmentEndDate = getDate(2020, FEBRUARY, 21, 14, 30, 0);
        final Date endDate = getEndDate(recurringPattern, appointmentStartDate);

        final List<Pair<Date, Date>> appointmentDates = getAppointmentDates(endDate, appointmentStartDate, appointmentEndDate, asList(MONDAY, SATURDAY), 2);

        assertEquals(4, appointmentDates.size());

        appointmentDates.sort(Comparator.comparing(Pair::getLeft));
        assertEquals(getDate(2020, FEBRUARY, 22, 14, 0, 0), appointmentDates.get(0).getLeft());
        assertEquals(getDate(2020, FEBRUARY, 24, 14, 0, 0), appointmentDates.get(1).getLeft());
        assertEquals(getDate(2020, MARCH, 7, 14, 0, 0), appointmentDates.get(2).getLeft());
        assertEquals(getDate(2020, MARCH, 9, 14, 0, 0), appointmentDates.get(3).getLeft());
    }

    @Test
    public void shouldReturnTwoAppointmentDatesFromGivenActiveAppointmentsAndAppointmentStartAndEndDates() {
        ArrayList<Appointment> activeAppointments = new ArrayList<>();

        final Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, FEBRUARY, 17, 14, 0, 0));
        appointment1.setEndDateTime(getDate(2020, FEBRUARY, 17, 14, 30, 0));
        final Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, FEBRUARY, 21, 14, 0, 0));
        appointment2.setEndDateTime(getDate(2020, FEBRUARY, 21, 14, 30, 0));
        final Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, FEBRUARY, 24, 14, 0, 0));
        appointment3.setEndDateTime(getDate(2020, FEBRUARY, 24, 14, 30, 0));

        activeAppointments.add(appointment1);
        activeAppointments.add(appointment2);
        activeAppointments.add(appointment3);

        final List<Pair<Date, Date>> appointmentStartAndEndDates = new ArrayList<>();

        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 17, 14, 0, 0),
                getDate(2020, FEBRUARY, 17, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 21, 14, 0, 0),
                getDate(2020, FEBRUARY, 21, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 24, 14, 0, 0),
                getDate(2020, FEBRUARY, 24, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 28, 14, 0, 0),
                getDate(2020, FEBRUARY, 28, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, MARCH, 2, 14, 0, 0),
                getDate(2020, MARCH, 2, 14, 30, 0)));

        final List<Pair<Date, Date>> newAppointmentDates = getNewAppointmentDates(activeAppointments, appointmentStartAndEndDates);

        assertEquals(2, newAppointmentDates.size());
        assertEquals(getDate(2020, FEBRUARY, 28, 14, 0, 0), newAppointmentDates.get(0).getLeft());
        assertEquals(getDate(2020, MARCH, 2, 14, 0, 0), newAppointmentDates.get(1).getLeft());

    }

    @Test
    public void shouldReturnTwoAppointmentDatesFromGivenActiveAppointmentsHavingOneRelatedAppointment() {
        ArrayList<Appointment> activeAppointments = new ArrayList<>();

        final Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, FEBRUARY, 21, 14, 0, 0));
        relatedAppointment.setEndDateTime(getDate(2020, FEBRUARY, 21, 14, 30, 0));

        final Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, FEBRUARY, 17, 14, 0, 0));
        appointment1.setEndDateTime(getDate(2020, FEBRUARY, 17, 14, 30, 0));
        final Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, FEBRUARY, 24, 14, 0, 0));
        appointment2.setEndDateTime(getDate(2020, FEBRUARY, 24, 14, 30, 0));
        final Appointment movedAppointment = new Appointment();
        movedAppointment.setStartDateTime(getDate(2020, FEBRUARY, 28, 14, 0, 0));
        movedAppointment.setEndDateTime(getDate(2020, FEBRUARY, 28, 14, 30, 0));
        movedAppointment.setRelatedAppointment(relatedAppointment);

        activeAppointments.add(appointment1);
        activeAppointments.add(appointment2);
        activeAppointments.add(movedAppointment);

        final List<Pair<Date, Date>> appointmentStartAndEndDates = new ArrayList<>();

        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 17, 14, 0, 0),
                getDate(2020, FEBRUARY, 17, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 21, 14, 0, 0),
                getDate(2020, FEBRUARY, 21, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 24, 14, 0, 0),
                getDate(2020, FEBRUARY, 24, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, FEBRUARY, 28, 14, 0, 0),
                getDate(2020, FEBRUARY, 28, 14, 30, 0)));
        appointmentStartAndEndDates.add(new ImmutablePair<>(getDate(2020, MARCH, 2, 14, 0, 0),
                getDate(2020, MARCH, 2, 14, 30, 0)));

        final List<Pair<Date, Date>> newAppointmentDates = getNewAppointmentDates(activeAppointments, appointmentStartAndEndDates);

        assertEquals(2, newAppointmentDates.size());
        assertEquals(getDate(2020, FEBRUARY, 28, 14, 0, 0), newAppointmentDates.get(0).getLeft());
        assertEquals(getDate(2020, MARCH, 2, 14, 0, 0), newAppointmentDates.get(1).getLeft());

    }

    @Test
    public void shouldReturnOriginalFirstAppointmentEvenAfterItIsRemoved() {
        ArrayList<Appointment> activeAppointments = new ArrayList<>();
        ArrayList<Appointment> removedAppointments = new ArrayList<>();

        final Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, MARCH, 2, 14, 0, 0));
        appointment1.setEndDateTime(getDate(2020, MARCH, 2, 14, 30, 0));
        final Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, MARCH, 8, 14, 0, 0));
        appointment2.setEndDateTime(getDate(2020, MARCH, 8, 14, 30, 0));

        activeAppointments.add(appointment2);
        removedAppointments.add(appointment1);

        final Appointment firstOriginalAppointmentInThePattern = getFirstOriginalAppointmentInThePattern(activeAppointments, removedAppointments);

        assertEquals(appointment1.getStartDateTime(), firstOriginalAppointmentInThePattern.getStartDateTime());

    }

    @Test
    public void shouldReturnOriginalFirstAppointmentIfItHasARelatedAppointmentAndRemoved() {
        ArrayList<Appointment> activeAppointments = new ArrayList<>();
        ArrayList<Appointment> removedAppointments = new ArrayList<>();

        final Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, MARCH, 2, 14, 0, 0));
        relatedAppointment.setEndDateTime(getDate(2020, MARCH, 2, 14, 30, 0));

        final Appointment movedAndRemovedAppointment = new Appointment();
        movedAndRemovedAppointment.setStartDateTime(getDate(2020, MARCH, 16, 14, 0, 0));
        movedAndRemovedAppointment.setEndDateTime(getDate(2020, MARCH, 16, 14, 30, 0));
        movedAndRemovedAppointment.setRelatedAppointment(relatedAppointment);
        final Appointment activeApoointment = new Appointment();
        activeApoointment.setStartDateTime(getDate(2020, MARCH, 8, 14, 0, 0));
        activeApoointment.setEndDateTime(getDate(2020, MARCH, 8, 14, 30, 0));

        activeAppointments.add(activeApoointment);
        removedAppointments.add(movedAndRemovedAppointment);

        final Appointment firstOriginalAppointmentInThePattern = getFirstOriginalAppointmentInThePattern(activeAppointments, removedAppointments);

        assertEquals(relatedAppointment.getStartDateTime(), firstOriginalAppointmentInThePattern.getStartDateTime());
    }
}
