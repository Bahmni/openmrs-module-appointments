package org.openmrs.module.appointments.web.service.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringAppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;
public class DailyRecurringAppointmentsGenerationServiceTest {

    @InjectMocks
    private DailyRecurringAppointmentsGenerationService dailyRecurringAppointmentsGenerationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AppointmentMapper appointmentMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private RecurringAppointmentRequest getAppointmentRequest(Date appointmentStartDateTime, Date appointmentEndDateTime) {
        RecurringAppointmentRequest recurringAppointmentRequest = new RecurringAppointmentRequest();
        recurringAppointmentRequest.setAppointmentRequest(new AppointmentRequest());
        recurringAppointmentRequest.getAppointmentRequest().setStartDateTime(appointmentStartDateTime);
        recurringAppointmentRequest.getAppointmentRequest().setEndDateTime(appointmentEndDateTime);
        return recurringAppointmentRequest;
    }

    private AppointmentRecurringPattern getAppointmentRecurringPattern(int period, int frequency, Date endDate) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setPeriod(period);
        appointmentRecurringPattern.setFrequency(frequency);
        appointmentRecurringPattern.setEndDate(endDate);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        return appointmentRecurringPattern;
    }

    private RecurringPattern getRecurringPattern(int period, int frequency, Date endDate) {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setPeriod(period);
        recurringPattern.setFrequency(frequency);
        recurringPattern.setEndDate(endDate);
        recurringPattern.setType("DAY");
        return recurringPattern;
    }

    @Test
    public void shouldReturnAppointmentsForGivenRecurringPatternAndAppointmentRequest() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 2, null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(2, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 00, 00).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 16, 16, 30, 00).toString(),
                appointments.get(1).getEndDateTime().toString());
    }
    @Test
    public void shouldReturnAppointmentsAcrossDaysForGivenRecurringPatternAndAppointmentRequest() throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 23, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 00, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 2, null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(2, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 23, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 20, 0, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
    }
    @Test
    public void shouldReturnAppointmentsWithSlotStartsAndEndsOnDifferentDatesForGivenRecurringPatternWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 23, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 00, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 23, 45, 00));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(4, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 23, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 20, 0, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 23, 45, 0).toString(),
                appointments.get(2).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 23, 0, 15, 0).toString(),
                appointments.get(2).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 23, 45, 0).toString(),
                appointments.get(3).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 26, 0, 15, 0).toString(),
                appointments.get(3).getEndDateTime().toString());
    }
    @Test
    public void shouldReturnAppointmentsForGivenRecurringPatternWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 16, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 16, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(4, appointments.size());
        assertEquals(appointmentStartDateTime.toString(), appointments.get(0).getStartDateTime().toString());
        assertEquals(appointmentEndDateTime.toString(), appointments.get(0).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 8, 45, 0).toString(),
                appointments.get(1).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 19, 9, 15, 0).toString(),
                appointments.get(1).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 8, 45, 0).toString(),
                appointments.get(2).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 22, 9, 15, 0).toString(),
                appointments.get(2).getEndDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 8, 45, 0).toString(),
                appointments.get(3).getStartDateTime().toString());
        assertEquals(getDate(2019, Calendar.MAY, 25, 9, 15, 0).toString(),
                appointments.get(3).getEndDateTime().toString());
    }
    @Test
    public void shouldAddNewAppointmentsForGivenRecurringPatternWhenEndDateIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 25, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 25, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2019, Calendar.MAY, 31, 8, 45, 0));
        recurringPattern.setFrequency(0);
        recurringPattern.setPeriod(3);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2019, Calendar.MAY, 19, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.MAY, 19, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2019, Calendar.MAY, 22, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.MAY, 22, 9, 15, 0));
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2019, Calendar.MAY, 25, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.MAY, 25, 9, 15, 0));
        Appointment appointment4 = new Appointment();
        appointment4.setStartDateTime(getDate(2019, Calendar.MAY, 28, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.MAY, 28, 9, 15, 0));
        Appointment appointment5 = new Appointment();
        appointment5.setStartDateTime(getDate(2019, Calendar.MAY, 31, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2019, Calendar.MAY, 31, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, appointment3, appointment4, appointment5);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }
    @Test
    public void shouldAddNewAppointmentsForGivenRecurringPatternWhenFrequencyIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 25, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 25, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 3,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(3);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2019, Calendar.MAY, 19, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.MAY, 19, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2019, Calendar.MAY, 22, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.MAY, 22, 9, 15, 0));
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2019, Calendar.MAY, 25, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.MAY, 25, 9, 15, 0));
        Appointment appointment4 = new Appointment();
        appointment4.setStartDateTime(getDate(2019, Calendar.MAY, 28, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.MAY, 28, 9, 15, 0));
        Appointment appointment5 = new Appointment();
        appointment5.setStartDateTime(getDate(2019, Calendar.MAY, 31, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2019, Calendar.MAY, 31, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, appointment3, appointment4, appointment5);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsForGivenRecurringPatternWhenEndDateIsIncreasedWithVoidedAndRelatedAppointments() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 25, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 25, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2019, Calendar.MAY, 31, 8, 45, 0));
        recurringPattern.setFrequency(0);
        recurringPattern.setPeriod(3);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setUuid("appointment1");
        appointment1.setStartDateTime(getDate(2019, Calendar.MAY, 19, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.MAY, 19, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setUuid("appointment2");
        appointment2.setStartDateTime(getDate(2019, Calendar.MAY, 22, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.MAY, 22, 9, 15, 0));
        Appointment appointment3 = new Appointment();
        appointment3.setUuid("appointment3");
        appointment3.setStartDateTime(getDate(2019, Calendar.MAY, 25, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.MAY, 25, 9, 15, 0));
        appointment3.setVoided(Boolean.TRUE);
        Appointment appointment4 = new Appointment();
        appointment4.setUuid("appointment4");
        appointment4.setStartDateTime(getDate(2019, Calendar.MAY, 27, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.MAY, 27, 9, 15, 0));
        Appointment appointment5 = new Appointment();
        appointment5.setUuid("appointment5");
        appointment5.setStartDateTime(getDate(2019, Calendar.MAY, 30, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2019, Calendar.MAY, 30, 9, 15, 0));
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setUuid("relatedAppointment");
        relatedAppointment.setStartDateTime(getDate(2019, Calendar.MAY, 24, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2019, Calendar.MAY, 24, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment3);
        Appointment removedAppointment = new Appointment();
        removedAppointment.setUuid("removedAppointment");
        removedAppointment.setStartDateTime(getDate(2019, Calendar.MAY, 23, 8, 45, 0));
        removedAppointment.setEndDateTime(getDate(2019, Calendar.MAY, 23, 9, 15, 0));
        removedAppointment.setVoided(Boolean.TRUE);

        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment, removedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, removedAppointment, relatedAppointment, appointment3, appointment4, appointment5);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsFromLastActiveDateWhenNoOfOccurencesIsIncreasedFromAnUpdatedAppointmentWhichIsNotTheLastActiveAppointment() {
        Date appointmentStartDateTime = getDate(2020, Calendar.JANUARY, 02, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.JANUARY, 02, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 3,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        appointment2.setVoided(Boolean.TRUE);
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.JANUARY, 02, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.JANUARY, 02, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment2);

        Appointment appointment4 = new Appointment();
        appointment4.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 04, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 04, 9, 15, 0));
        Appointment appointment5 = new Appointment();
        appointment5.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 05, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 05, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(relatedAppointment, appointment1, appointment2, appointment3, appointment4, appointment5);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsFromUpdatedAppointmentDateWhenUpdatedAppointmentIsTheOnlyActiveAppointmentAndThenIncreaseNumberOfOccurrences() {
        Date appointmentStartDateTime = getDate(2020, Calendar.JANUARY, 01, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.JANUARY, 01, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 0));
        appointment1.setVoided(Boolean.TRUE);
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        appointment2.setVoided(Boolean.TRUE);
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        appointment3.setVoided(Boolean.TRUE);
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.JANUARY, 01, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.JANUARY, 01, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment2);

        Appointment newAppointment1 = new Appointment();
        newAppointment1.setStartDateTime(getDate(2020, Calendar.JANUARY, 02, 8, 45, 0));
        newAppointment1.setEndDateTime(getDate(2020, Calendar.JANUARY, 02, 9, 15, 0));
        Appointment newAppointment2 = new Appointment();
        newAppointment2.setStartDateTime(getDate(2020, Calendar.JANUARY, 03, 8, 45, 0));
        newAppointment2.setEndDateTime(getDate(2020, Calendar.JANUARY, 03, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(relatedAppointment, newAppointment1, newAppointment2, appointment1, appointment2, appointment3);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsFromLastActiveAppointmentDateIrrespectiveOfTheFutureDateAppointmentsWhichWereRemoved() {
        Date appointmentStartDateTime = getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        appointment2.setVoided(Boolean.TRUE);
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        appointment3.setVoided(Boolean.TRUE);
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.MARCH, 01, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.MARCH, 01, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment2);
        relatedAppointment.setVoided(Boolean.TRUE);

        Appointment newAppointment1 = new Appointment();
        newAppointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        newAppointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        Appointment newAppointment2 = new Appointment();
        newAppointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        newAppointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, newAppointment1, appointment3, newAppointment2, relatedAppointment);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsFromUpdatedAppointmentWhenUpdatedAppointmentIsTheLastActiveAppointmentAndNoOfOccurencesIsIncreased() {
        Date appointmentStartDateTime = getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 3,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        appointment2.setVoided(Boolean.TRUE);
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.MARCH, 01, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.MARCH, 01, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment2);

        Appointment newAppointment1 = new Appointment();
        newAppointment1.setStartDateTime(getDate(2020, Calendar.MARCH, 02, 8, 45, 0));
        newAppointment1.setEndDateTime(getDate(2020, Calendar.MARCH, 02, 9, 15, 0));
        Appointment newAppointment2 = new Appointment();
        newAppointment2.setStartDateTime(getDate(2020, Calendar.MARCH, 03, 8, 45, 0));
        newAppointment2.setEndDateTime(getDate(2020, Calendar.MARCH, 03, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment, newAppointment1, newAppointment2);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewAppointmentsWithPeriodAppliedFromLastActiveDate() {
        Date appointmentStartDateTime = getDate(2020, Calendar.FEBRUARY, 1, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.FEBRUARY, 1, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 3,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(2);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 1, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 1, 9, 15, 0));
        Appointment appointment2 = new Appointment();
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 3, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 3, 9, 15, 0));
        appointment2.setVoided(Boolean.TRUE);
        Appointment appointment3 = new Appointment();
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 5, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 5, 9, 15, 0));
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 6, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 6, 9, 15, 0));
        relatedAppointment.setRelatedAppointment(appointment2);

        Appointment newAppointment1 = new Appointment();
        newAppointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 8, 8, 45, 0));
        newAppointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 8, 9, 15, 0));
        Appointment newAppointment2 = new Appointment();
        newAppointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 10, 8, 45, 0));
        newAppointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 10, 9, 15, 0));
        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment, newAppointment1, newAppointment2);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Ignore
    public void shouldThrowExceptionWhenFrequencyIsDecreasedAndFutureRecurringAppointmentHaveMissedStatus() {
        Date appointmentStartDateTime = getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 5, null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Appointment appointment5 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 01, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 01, 9, 15, 0));
        appointment1.setStatus(AppointmentStatus.Completed);
        appointments.add(appointment1);
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 02, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 02, 9, 15, 0));
        appointment2.setStatus(AppointmentStatus.CheckedIn);
        appointments.add(appointment2);

        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 03, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 03, 9, 15, 0));
        appointment3.setStatus(AppointmentStatus.Cancelled);
        appointments.add(appointment3);
        appointment4.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 04, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 04, 9, 15, 0));
        appointment4.setStatus(AppointmentStatus.Missed);
        appointments.add(appointment4);
        appointment5.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 05, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 05, 9, 15, 0));
        appointment5.setStatus(AppointmentStatus.Scheduled);
        appointments.add(appointment5);
        appointmentRecurringPattern.setAppointments(appointments);

        String error = "Changes cannot be made as the appointments are already Missed";
        expectedException.expect(APIException.class);
        expectedException.expectMessage(error);

        dailyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldNotAddNewAppointmentForGivenRecurringPatternWhenUpdatedEndDateIsBeforeLastActiveAppointmentDate() {
        Date appointmentStartDateTime = getDate(2020, Calendar.FEBRUARY, 4, 8, 45, 00);
        Date appointmentEndDateTime = getDate(2020, Calendar.FEBRUARY, 4, 9, 15, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                getDate(2020, Calendar.FEBRUARY, 5, 8, 45, 00));
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2020, Calendar.FEBRUARY, 7, 8, 45, 0));
        recurringPattern.setFrequency(0);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setUuid("appointment1");
        appointment1.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 1, 8, 45, 0));
        appointment1.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 1, 9, 15, 0));
        appointment1.setAppointmentRecurringPattern(appointmentRecurringPattern);
        Appointment appointment2 = new Appointment();
        appointment2.setUuid("appointment2");
        appointment2.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 2, 8, 45, 0));
        appointment2.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 2, 9, 15, 0));
        appointment2.setAppointmentRecurringPattern(appointmentRecurringPattern);
        Appointment appointment3 = new Appointment();
        appointment3.setUuid("appointment3");
        appointment3.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 3, 8, 45, 0));
        appointment3.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 3, 9, 15, 0));
        appointment3.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointment3.setVoided(Boolean.TRUE);
        Appointment appointment4 = new Appointment();
        appointment4.setUuid("appointment4");
        appointment4.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 4, 8, 45, 0));
        appointment4.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 4, 9, 15, 0));
        appointment4.setAppointmentRecurringPattern(appointmentRecurringPattern);
        Appointment appointment5 = new Appointment();
        appointment5.setUuid("appointment5");
        appointment5.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 5, 8, 45, 0));
        appointment5.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 5, 9, 15, 0));
        appointment5.setAppointmentRecurringPattern(appointmentRecurringPattern);
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setUuid("relatedAppointment");
        relatedAppointment.setStartDateTime(getDate(2020, Calendar.FEBRUARY, 8, 8, 45, 0));
        relatedAppointment.setEndDateTime(getDate(2020, Calendar.FEBRUARY, 8, 9, 15, 0));
        relatedAppointment.setAppointmentRecurringPattern(appointmentRecurringPattern);
        relatedAppointment.setRelatedAppointment(appointment3);

        List<Appointment> existingAppointments = Arrays.asList(appointment1, appointment2, appointment3, relatedAppointment, appointment4, appointment5);
        List<Appointment> expectedAppointments = Arrays.asList(appointment1, appointment2, appointment3, appointment4, appointment5, relatedAppointment);
        appointmentRecurringPattern.setAppointments(existingAppointments.stream().collect(Collectors.toSet()));
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
            assertEquals(getDate(2020, Calendar.FEBRUARY, 7, 8, 45, 0),
                    updatedAppointments.get(i).getAppointmentRecurringPattern().getEndDate());

        }
    }
}
