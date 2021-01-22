package org.openmrs.module.appointments.web.service.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.time.DateUtils.addDays;
import static org.apache.commons.lang.time.DateUtils.addHours;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
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
import org.openmrs.module.appointments.web.util.AppointmentBuilder;
import org.openmrs.module.appointments.web.util.RecurringPatternBuilder;

public class WeeklyRecurringAppointmentsGenerationServiceTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private WeeklyRecurringAppointmentsGenerationService weeklyRecurringAppointmentsGenerationService;

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

    private RecurringPattern getRecurringPattern(int period, int frequency, Date endDate,
                                                 List<String> daysOfWeek) {
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setPeriod(period);
        recurringPattern.setFrequency(frequency);
        recurringPattern.setEndDate(endDate);
        recurringPattern.setDaysOfWeek(daysOfWeek);
        recurringPattern.setType("WEEK");
        return recurringPattern;
    }

    private AppointmentRecurringPattern getAppointmentRecurringPattern(int period, int frequency, Date endDate,
                                                                       String daysOfWeek) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setPeriod(period);
        appointmentRecurringPattern.setFrequency(frequency);
        appointmentRecurringPattern.setEndDate(endDate);
        appointmentRecurringPattern.setDaysOfWeek(daysOfWeek);
        appointmentRecurringPattern.setType(RecurringAppointmentType.WEEK);
        return appointmentRecurringPattern;
    }


    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithMultipleDaysInWeek() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 2,
                null, asList("SUNDAY", "WEDNESDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest()))
                .thenAnswer(x -> new Appointment());

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 15, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 15, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithAllPastDaysInWeek() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 17, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 6,
                null, asList("SUNDAY", "TUESDAY", "THURSDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 23, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 23, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 2, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 2, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 4, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 4, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithPastDaySelectedInWeek() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 2,
                null, asList("SUNDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 2, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 2, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnThreeAppointmentsHavingTwoSelectedDaysInWeekWithDayCodesLessThanStartDayCode() {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 15, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 15, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(1, 3,
                null, asList("SUNDAY", "MONDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 25, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 25, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithFutureDaySelectedInWeek() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 4,
                null, asList("FRIDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 17, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 17, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 31, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 31, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 1, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 1, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnThreeAppointmentsHavingTwoSelectedDaysInWeekWithDayCodesGreaterThanStartDayCode() {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 3,
                null, asList("FRIDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 16, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 16, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 17, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 17, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 30, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 30, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithHighPeriodValue() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 18, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 18, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(10, 4,
                null, asList("MONDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 24, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 24, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 6, 29, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 6, 29, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 2, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 2, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithSameStartDaySelectedInWeek() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(2, 2,
                null, asList("MONDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 27, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 27, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 0,
                getDate(2019, Calendar.JUNE, 13, 23, 45, 00),
                asList("SUNDAY", "WEDNESDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 15, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 15, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 5, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 5, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 8, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 8, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 9, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 9, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternForAllDaysOfWeekWithCaseInsensitive() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 7,
                null, asList("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        for (int date = 13; date < 20; date++) {
            Map<String, Date> appointmentInstance = new HashMap<>();
            appointmentInstance.put("startDateTime", getDate(2019, 4, date, 16, 0, 0));
            appointmentInstance.put("endDateTime", getDate(2019, 4, date, 16, 30, 0));
            expectedAppointmentDatesList.add(appointmentInstance);
        }

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternForAllDaysOfWeekWithCaseInsensitiveInvalidDays() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(3, 1,
                null, asList("MISC"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsOnlyBeforeEndDateForGivenRecurringPatternWithEndDate() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 5, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 5, 16, 30, 00);
        Date endDate = getDate(2019, Calendar.JUNE, 16, 16, 00, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(1, 0,
                endDate, asList("SATURDAY", "MONDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 8, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 8, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 10, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 10, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 15, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 15, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsForNextWeekWithOneOccurence() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 31, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 31, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(1, 1,
                null, asList("THURSDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsForNextWeekWithFourOccurences() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 6, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 6, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        RecurringPattern recurringPattern = getRecurringPattern(1, 4,
                null, asList("THURSDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = weeklyRecurringAppointmentsGenerationService.generateAppointments(recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewRecurringAppointmentsForOneMoreWeekWhenFrequencyIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 14, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 14, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 4,
                null, "THURSDAY,FRIDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(6);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(asList("THURSDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();


        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        appointments.add(appointment2);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        appointments.add(appointment4);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        appointmentRecurringPattern.setAppointments(appointments);


        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService
                .addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddTwoNewRecurringAppointmentsToExistingThreeAppointmentsWhenFrequencyIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 30, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 30, 16, 30, 00);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 3,
                null, "FRIDAY,SATURDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(asList("FRIDAY", "SATURDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();

        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 16, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 16, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, 7, 16, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, 7, 16, 16, 30, 0));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 17, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 17, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, 7, 17, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, 7, 17, 16, 30, 0));
        appointments.add(appointment2);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 30, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 30, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, 7, 30, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, 7, 30, 16, 30, 0));
        appointments.add(appointment3);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 31, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 31, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 8, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 8, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        appointmentRecurringPattern.setAppointments(appointments);

        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern,
                recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewRecurringAppointmentsForOneMoreWeekWhenEndDateIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 14, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 14, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                getDate(2019, Calendar.JUNE, 14, 16, 0, 0), "THURSDAY,FRIDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2019, Calendar.JUNE, 24, 16, 0, 0));
        recurringPattern.setFrequency(null);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();


        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        appointments.add(appointment2);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        appointments.add(appointment4);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        appointmentRecurringPattern.setAppointments(appointments);


        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern,
                recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewRecurringAppointmentToExistingThreeAppointmentsWhenEndDateIsIncreased() {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 30, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 30, 16, 30, 00);

        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 0,
                getDate(2019, 7, 30, 16, 0, 0), "FRIDAY,SATURDAY");

        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2019, 8, 12, 16, 0, 0));
        recurringPattern.setFrequency(null);
        recurringPattern.setPeriod(2);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);

        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();

        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 16, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 16, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, 7, 16, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, 7, 16, 16, 30, 0));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 17, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 17, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, 7, 17, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, 7, 17, 16, 30, 0));
        appointments.add(appointment2);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 30, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 30, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, 7, 30, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, 7, 30, 16, 30, 0));
        appointments.add(appointment3);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 7, 31, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 7, 31, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        appointmentRecurringPattern.setAppointments(appointments);

        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern,
                recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }


    @Test
    public void shouldRemoveRecurringAppointmentsFromExistingAppointmentsWhenFrequencyIsDecreased() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +7);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +7);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 5,
                null, days);

        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(4);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Appointment appointment5 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointments.add(appointment2);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", startTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", endTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(startTimeCalendar.getTime());
        appointment3.setEndDateTime(endTimeCalendar.getTime());
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointments.add(appointment4);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment5.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointment5.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +7));
        appointments.add(appointment5);
        appointmentRecurringPattern.setAppointments(appointments);


        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        int removedAppointmentIndex = updatedAppointments.size() - 1;
        assertAppointmentDetails(expectedAppointmentDatesList, updatedAppointments, removedAppointmentIndex);
    }

    @Test
    public void shouldRemoveRecurringAppointmentsWhenEndDateIsDecreasedToFutureDate() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +7);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +7);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +7), days);

        Calendar todayStartTimeCalendar = Calendar.getInstance();
        Calendar todayEndTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);

        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day + 1, hour, minute, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Appointment appointment5 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointments.add(appointment2);

        todayStartTimeCalendar.set(year, month, day, hour + 1, 00, 00);
        todayEndTimeCalendar.set(year, month, day, hour + 1, 30, 00);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", todayStartTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", todayEndTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(todayStartTimeCalendar.getTime());
        appointment3.setEndDateTime(todayEndTimeCalendar.getTime());
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointments.add(appointment4);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment5.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointment5.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +7));
        appointments.add(appointment5);
        appointmentRecurringPattern.setAppointments(appointments);


        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        List<Integer> removedAppointmentIndexes = asList(updatedAppointments.size() - 1, updatedAppointments.size() - 2);
        assertAppointmentDetails(expectedAppointmentDatesList, updatedAppointments, removedAppointmentIndexes);
    }

    @Test
    public void shouldNotRemoveCurrentDayRecurringAppointmentWhenEndDateIsDecreasedToCurrentDateAndAppointmentIsInPastTime() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +6);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +6);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +6), days);

        Calendar todayStartTimeCalendar = Calendar.getInstance();
        Calendar todayEndTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);
        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day, hour, minute + 1, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointments.add(appointment2);
        todayStartTimeCalendar.set(year, month, day, hour - 1, 00, 00);
        todayEndTimeCalendar.set(year, month, day, hour - 1, 30, 00);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", todayStartTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", todayEndTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(todayStartTimeCalendar.getTime());
        appointment3.setEndDateTime(todayEndTimeCalendar.getTime());
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointments.add(appointment4);
        appointmentRecurringPattern.setAppointments(appointments);
        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        int removedAppointmentIndex = updatedAppointments.size() - 1;
        assertAppointmentDetails(expectedAppointmentDatesList, updatedAppointments, removedAppointmentIndex);
    }

    private void assertAppointmentDetails(List<Map<String, Date>> expectedAppointmentDatesList, List<Appointment> updatedAppointments, int removedAppointmentIndex) {
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
            if (i == removedAppointmentIndex)
                assertTrue(updatedAppointments.get(i).getVoided());
            else
                assertFalse(updatedAppointments.get(i).getVoided());
        }
    }

    @Test
    public void shouldRemoveCurrentDayRecurringAppointmentWhenEndDateIsDecreasedToCurrentDateAndAppointmentIsInFutureTime() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +6);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +6);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +6), days);
        Calendar todayStartTimeCalendar = Calendar.getInstance();
        Calendar todayEndTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);

        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day, hour, minute + 1, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointments.add(appointment2);

        todayStartTimeCalendar.set(year, month, day, hour, minute + 2, 00);
        todayEndTimeCalendar.set(year, month, day, hour, minute + 32, 00);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", todayStartTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", todayEndTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(todayStartTimeCalendar.getTime());
        appointment3.setEndDateTime(todayEndTimeCalendar.getTime());
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointments.add(appointment4);
        appointmentRecurringPattern.setAppointments(appointments);
        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        List<Integer> removedAppointmentIndexes = asList(updatedAppointments.size() - 1, updatedAppointments.size() - 2);
        assertAppointmentDetails(expectedAppointmentDatesList, updatedAppointments, removedAppointmentIndexes);
    }

    private void assertAppointmentDetails(List<Map<String, Date>> expectedAppointmentDatesList, List<Appointment> updatedAppointments, List<Integer> removedAppointmentIndexes) {
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
            if (removedAppointmentIndexes.contains(i))
                assertTrue(updatedAppointments.get(i).getVoided());
            else
                assertFalse(updatedAppointments.get(i).getVoided());
        }
    }

    @Test
    public void shouldThrowExceptionWhenEndDateIsDecreasedAndFutureRecurringAppointmentHaveCheckedInStatus() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +7);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +7);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +7), days);

        Calendar todayStartTimeCalendar = Calendar.getInstance();
        Calendar todayEndTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);

        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day, hour, minute + 1, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Appointment appointment5 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setStatus(AppointmentStatus.Completed);
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointment2.setStatus(AppointmentStatus.CheckedIn);
        appointments.add(appointment2);

        todayStartTimeCalendar.set(year, month, day, hour + 1, minute, 00);
        todayEndTimeCalendar.set(year, month, day, hour + 1, minute + 30, 00);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", todayStartTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", todayEndTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(startTimeCalendar.getTime());
        appointment3.setEndDateTime(endTimeCalendar.getTime());
        appointment3.setStatus(AppointmentStatus.Cancelled);
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointment4.setStatus(AppointmentStatus.CheckedIn);
        appointments.add(appointment4);
        appointment5.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointment5.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +7));
        appointment5.setStatus(AppointmentStatus.Scheduled);
        appointments.add(appointment5);
        appointmentRecurringPattern.setAppointments(appointments);

        String error = "Changes cannot be made as the appointments are already Checked-In";
        expectedException.expect(APIException.class);
        expectedException.expectMessage(error);

        weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }

    @Test
    public void shouldThrowExceptionWhenEndDateIsDecreasedAndFutureRecurringAppointmentHaveMissedStatus() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +7);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +7);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +7), days);

        Calendar todayStartTimeCalendar = Calendar.getInstance();
        Calendar todayEndTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);

        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day, hour, minute + 1, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Appointment appointment5 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointmentInstance.put("endDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -7));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setStatus(AppointmentStatus.Completed);
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), -1));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointment2.setStatus(AppointmentStatus.CheckedIn);
        appointments.add(appointment2);

        todayStartTimeCalendar.set(year, month, day, hour + 1, minute, 00);
        todayEndTimeCalendar.set(year, month, day, hour + 1, minute + 30, 00);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", todayStartTimeCalendar.getTime());
        appointmentInstance.put("endDateTime", todayEndTimeCalendar.getTime());
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(startTimeCalendar.getTime());
        appointment3.setEndDateTime(endTimeCalendar.getTime());
        appointment3.setStatus(AppointmentStatus.Cancelled);
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointmentInstance.put("endDateTime", DateUtils.addDays(endTimeCalendar.getTime(), +6));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointment4.setStatus(AppointmentStatus.Missed);
        appointments.add(appointment4);
        appointment5.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +7));
        appointment5.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +7));
        appointment5.setStatus(AppointmentStatus.Scheduled);
        appointments.add(appointment5);
        appointmentRecurringPattern.setAppointments(appointments);

        String error = "Changes cannot be made as the appointments are already Missed";
        expectedException.expect(APIException.class);
        expectedException.expectMessage(error);

        weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
    }
    
    @Test
    public void setAppointments_shouldThrowWhenFrequecyIsDecreasedSuchThatEndDateIsInPast() {
    	TimeZone.setDefault(TimeZone.getTimeZone("IST"));
        Date date = new Date();
        
        RecurringAppointmentRequest recurringRequest = getAppointmentRequest(addDays(date, +7), addHours(addDays(date, +7), +1));
        recurringRequest.setRecurringPattern(new RecurringPatternBuilder().setPeriod(1).setFrequency(2).get());
        
        Mockito.when(appointmentMapper.fromRequest(recurringRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        
        AppointmentRecurringPattern recurringPattern = getAppointmentRecurringPattern(1, 4, null, null);
        recurringPattern.setAppointments(new HashSet<Appointment>() {{
            add(new AppointmentBuilder().withStartDateTime(addDays(date, -7)).withEndDateTime(addHours(addDays(date, -7), +1)).build());
            add(new AppointmentBuilder().withStartDateTime(addDays(date, -1)).withEndDateTime(addHours(addDays(date, -1), +1)).build());
            add(new AppointmentBuilder().withStartDateTime(addDays(date, +0)).withEndDateTime(addHours(addDays(date, +0), +1)).build());
            add(new AppointmentBuilder().withStartDateTime(addDays(date, +6)).withEndDateTime(addHours(addDays(date, +6), +1)).build());
        }});

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Changes cannot be made as the appointments are from past date");

        weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(recurringPattern, recurringRequest);
    }

    @Test
    public void shouldThrowExceptionWhenEndDateIsDecreasedToPastDate() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int dayCode = startTimeCalendar.get(Calendar.DAY_OF_WEEK);
        int previousDayCode = dayCode == 0 ? 7 : dayCode - 1;
        String days = getDayFromDayCode(dayCode) + "," + getDayFromDayCode(previousDayCode);
        Date appointmentStartDateTime = DateUtils.addDays(startTimeCalendar.getTime(), +7);
        Date appointmentEndDateTime = DateUtils.addDays(endTimeCalendar.getTime(), +7);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                DateUtils.addDays(startTimeCalendar.getTime(), +7), days);

        Calendar todayStartTimeCalendar = Calendar.getInstance();
        int year = todayStartTimeCalendar.get(Calendar.YEAR);
        int month = todayStartTimeCalendar.get(Calendar.MONTH);
        int day = todayStartTimeCalendar.get(Calendar.DATE);
        int hour = todayStartTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = todayStartTimeCalendar.get(Calendar.MINUTE);


        RecurringPattern recurringPattern = new RecurringPattern();
        todayStartTimeCalendar.set(year, month, day, hour - 1, minute, 00);
        recurringPattern.setEndDate(todayStartTimeCalendar.getTime());
        recurringPattern.setFrequency(null);
        recurringPattern.setPeriod(1);
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment2 = new Appointment();
        Appointment appointment3 = new Appointment();
        Appointment appointment4 = new Appointment();
        Set<Appointment> appointments = new HashSet<>();
        appointment1.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointment1.setEndDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -7));
        appointments.add(appointment1);
        appointment2.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -1));
        appointment2.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -1));
        appointments.add(appointment2);
        appointment3.setStartDateTime(startTimeCalendar.getTime());
        appointment3.setEndDateTime(endTimeCalendar.getTime());
        appointments.add(appointment3);
        appointment4.setStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), +6));
        appointment4.setEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), +6));
        appointments.add(appointment4);

        appointmentRecurringPattern.setAppointments(appointments);

        String error = "Changes cannot be made as the appointments are from past date";
        expectedException.expect(APIException.class);
        expectedException.expectMessage(error);

        weeklyRecurringAppointmentsGenerationService.removeRecurringAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

    }

    @Test
    public void shouldAddNewRecurringAppointmentsForOneMoreWeekWhenFrequencyIsIncreasedAndOnlyOneWeekDayIsSelected() {
        Date appointmentStartDateTime = getDate(2119, Calendar.JUNE, 14, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2119, Calendar.JUNE, 14, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 2,
                null, "TUESDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(4);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(singletonList("TUESDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        Appointment appointment3 = new Appointment();
        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2119, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2119, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2119, Calendar.JUNE, 6, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2119, Calendar.JUNE, 6, 16, 30, 0));
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2119, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2119, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2119, Calendar.JUNE, 13, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2119, Calendar.JUNE, 13, 16, 30, 0));
        appointments.add(appointment3);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2119, Calendar.JUNE, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2119, Calendar.JUNE, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2119, Calendar.JUNE, 27, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2119, Calendar.JUNE, 27, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentRecurringPattern.setAppointments(appointments);
        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService
                .addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);
        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddNewRecurringAppointmentsForOneMoreWeekWhenFrequencyIsIncreasedWithRemovedAndRelatedAppointments() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 7, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 7, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 2,
                null, "THURSDAY,FRIDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(3);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(asList("THURSDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setUuid("appointment1");
        Appointment appointment2 = new Appointment();
        appointment2.setUuid("appointment2");
        Appointment removedAppointment = new Appointment();
        Appointment relatedAppointment = new Appointment();
        relatedAppointment.setUuid("relatedAppointment");

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        appointment1.setVoided(Boolean.TRUE);
        appointments.add(appointment1);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        relatedAppointment.setStartDateTime(getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        relatedAppointment.setEndDateTime(getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        relatedAppointment.setRelatedAppointment(appointment1);
        appointments.add(relatedAppointment);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        appointments.add(appointment2);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        removedAppointment.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        removedAppointment.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        removedAppointment.setVoided(Boolean.TRUE);
        appointments.add(removedAppointment);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        appointmentRecurringPattern.setAppointments(appointments);

        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService
                .addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddExpectedNumberOfAppointmentsWhenOccurrencesIsReducedToOneAndThenIncrease() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 7, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 7, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1,
                null, "THURSDAY,FRIDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(1);
        recurringPattern.setDaysOfWeek(asList("THURSDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setUuid("appointment1");
        Appointment appointment2 = new Appointment();
        appointment2.setUuid("appointment2");
        Appointment appointment3 = new Appointment();
        appointment2.setUuid("appointment3");
        Appointment appointment4 = new Appointment();
        appointment2.setUuid("appointment4");
        Appointment appointment5 = new Appointment();
        appointment2.setUuid("appointment5");
        Appointment newAppointment2 = new Appointment();
        appointment2.setUuid("newAppointment2");
        Appointment newAppointment3 = new Appointment();
        appointment2.setUuid("newAppointment3");
        Appointment newAppointment4 = new Appointment();
        appointment2.setUuid("newAppointment4");
        Appointment newAppointment5 = new Appointment();
        appointment2.setUuid("newAppointment5");

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        appointments.add(appointment1);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        appointments.add(appointment2);
        appointment2.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        newAppointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        appointments.add(appointment3);
        appointment3.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 14, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 14, 16, 0, 0));
        newAppointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 14, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 20, 16, 30, 0));
        appointments.add(appointment4);
        appointment4.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 20, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 20, 16, 0, 0));
        newAppointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 20, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment5.setStartDateTime(getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointment5.setEndDateTime(getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        appointments.add(appointment5);
        appointment5.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment5.setStartDateTime(getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        newAppointment5.setEndDateTime(getDate(2019, Calendar.JUNE, 21, 16, 30, 0));

        appointmentRecurringPattern.setAppointments(appointments);

        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService
                .addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldAddExpectedNumberOfAppointmentsWhenOccurrencesIsReducedToOneAndThenIncreaseWithPeriodAsTwoWeeks() {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 7, 16, 0, 0);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 7, 16, 30, 0);
        RecurringAppointmentRequest recurringAppointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 1,
                null, "THURSDAY,FRIDAY");
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        recurringPattern.setPeriod(2);
        recurringPattern.setDaysOfWeek(asList("THURSDAY", "FRIDAY"));
        recurringAppointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(recurringAppointmentRequest.getAppointmentRequest())).thenAnswer(x -> new Appointment());
        Appointment appointment1 = new Appointment();
        appointment1.setUuid("appointment1");
        Appointment appointment2 = new Appointment();
        appointment2.setUuid("appointment2");
        Appointment appointment3 = new Appointment();
        appointment2.setUuid("appointment3");
        Appointment appointment4 = new Appointment();
        appointment2.setUuid("appointment4");
        Appointment appointment5 = new Appointment();
        appointment2.setUuid("appointment5");
        Appointment newAppointment2 = new Appointment();
        appointment2.setUuid("newAppointment2");
        Appointment newAppointment3 = new Appointment();
        appointment2.setUuid("newAppointment3");
        Appointment newAppointment4 = new Appointment();
        appointment2.setUuid("newAppointment4");
        Appointment newAppointment5 = new Appointment();
        appointment2.setUuid("newAppointment5");

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Set<Appointment> appointments = new HashSet<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment1.setStartDateTime(getDate(2019, Calendar.JUNE, 7, 16, 0, 0));
        appointment1.setEndDateTime(getDate(2019, Calendar.JUNE, 7, 16, 30, 0));
        appointments.add(appointment1);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        appointments.add(appointment2);
        appointment2.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment2.setStartDateTime(getDate(2019, Calendar.JUNE, 13, 16, 0, 0));
        newAppointment2.setEndDateTime(getDate(2019, Calendar.JUNE, 13, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        appointments.add(appointment3);
        appointment3.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 21, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment3.setStartDateTime(getDate(2019, Calendar.JUNE, 21, 16, 0, 0));
        newAppointment3.setEndDateTime(getDate(2019, Calendar.JUNE, 21, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 27, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 27, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 27, 16, 0, 0));
        appointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 27, 16, 30, 0));
        appointments.add(appointment4);
        appointment4.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 27, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 27, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment4.setStartDateTime(getDate(2019, Calendar.JUNE, 27, 16, 0, 0));
        newAppointment4.setEndDateTime(getDate(2019, Calendar.JUNE, 27, 16, 30, 0));

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JULY, 5, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JULY, 5, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointment5.setStartDateTime(getDate(2019, Calendar.JULY, 5, 16, 0, 0));
        appointment5.setEndDateTime(getDate(2019, Calendar.JULY, 5, 16, 30, 0));
        appointments.add(appointment5);
        appointment5.setVoided(Boolean.TRUE);

        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JULY, 5, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JULY, 5, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        newAppointment5.setStartDateTime(getDate(2019, Calendar.JULY, 5, 16, 0, 0));
        newAppointment5.setEndDateTime(getDate(2019, Calendar.JULY, 5, 16, 30, 0));

        appointmentRecurringPattern.setAppointments(appointments);

        List<Appointment> updatedAppointments = weeklyRecurringAppointmentsGenerationService
                .addAppointments(appointmentRecurringPattern, recurringAppointmentRequest);

        assertEquals(expectedAppointmentDatesList.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }

    private String getDayFromDayCode(Integer dayCode) {
        String day = null;
        switch (dayCode) {
            case 1:
                day = "SUNDAY";
                break;
            case 2:
                day = "MONDAY";
                break;
            case 3:
                day = "TUESDAY";
                break;
            case 4:
                day = "WEDNESDAY";
                break;
            case 5:
                day = "THURSDAY";
                break;
            case 6:
                day = "FRIDAY";
                break;
            case 7:
                day = "SATURDAY";
                break;
            default:
                break;
        }
        return day;
    }
}
