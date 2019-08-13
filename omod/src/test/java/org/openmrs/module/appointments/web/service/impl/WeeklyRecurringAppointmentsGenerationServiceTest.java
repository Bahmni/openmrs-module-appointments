package org.openmrs.module.appointments.web.service.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;
import org.openmrs.module.appointments.web.service.RecurringAppointmentsGenerationService;

import java.text.ParseException;
import java.util.*;

import static org.junit.Assert.*;
import static org.openmrs.module.appointments.web.helper.DateHelper.getDate;

public class WeeklyRecurringAppointmentsGenerationServiceTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private RecurringAppointmentsGenerationService recurringAppointmentsGenerationService;
    private AppointmentMapper appointmentMapperMock;


    @Before
    public void setUp() {
        appointmentMapperMock = Mockito.mock(AppointmentMapper.class);
    }

    private AppointmentRequest getAppointmentRequest(Date appointmentStartDateTime, Date appointmentEndDateTime) {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setStartDateTime(appointmentStartDateTime);
        appointmentRequest.setEndDateTime(appointmentEndDateTime);
        return appointmentRequest;
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
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithMultipleDaysInWeek()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 2,
                null, "SUNDAY,WEDNESDAY,SATURDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

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
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithAllPastDaysInWeek()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 17, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 17, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 6,
                null, "SUNDAY,TUESDAY,THURSDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithPastDaySelectedInWeek()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 2,
                null, "SUNDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 19, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 19, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 5, 2, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 5, 2, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnThreeAppointmentsHavingTwoSelectedDaysInWeekWithDayCodesLessThanStartDayCode()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 15, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 15, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 3,
                null, "SUNDAY,MONDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithFutureDaySelectedInWeek()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 4,
                null, "FRIDAY,SATURDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnThreeAppointmentsHavingTwoSelectedDaysInWeekWithDayCodesGreaterThanStartDayCode()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.AUGUST, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.AUGUST, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 3,
                null, "FRIDAY,SATURDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithHighPeriodValue() throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 18, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 18, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(10, 4,
                null, "MONDAY,FRIDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternWithSameStartDaySelectedInWeek()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(2, 2,
                null, "MONDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 13, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 13, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);
        appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 27, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 27, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternAndAppointmentRequestWithEndDate()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.JUNE, 13, 23, 45, 00),
                "SUNDAY,WEDNESDAY,SATURDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternForAllDaysOfWeekWithCaseInsensitive()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 7,
                null, "sunday,monday,tuesday,wednesday,thursday,friday,saturday");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        for (int date = 13; date < 20; date++) {
            Map<String, Date> appointmentInstance = new HashMap<>();
            appointmentInstance.put("startDateTime", getDate(2019, 4, date, 16, 0, 0));
            appointmentInstance.put("endDateTime", getDate(2019, 4, date, 16, 30, 0));
            expectedAppointmentDatesList.add(appointmentInstance);
        }

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnWeeklyAppointmentsForGivenRecurringPatternForAllDaysOfWeekWithCaseInsensitiveInvalidDays()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 1,
                null, "MISC");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, 4, 18, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, 4, 18, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsOnlyBeforeEndDateForGivenRecurringPatternWithEndDate()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 5, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 5, 16, 30, 00);

        Date endDate = getDate(2019, Calendar.JUNE, 16, 16,00,00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 0,
                endDate, "SATURDAY,MONDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsForNextWeekWithOneOccurence()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 31, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 31, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 1,
                null, "THURSDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

        List<Map<String, Date>> expectedAppointmentDatesList = new ArrayList<>();
        Map<String, Date> appointmentInstance = new HashMap<>();
        appointmentInstance.put("startDateTime", getDate(2019, Calendar.JUNE, 6, 16, 0, 0));
        appointmentInstance.put("endDateTime", getDate(2019, Calendar.JUNE, 6, 16, 30, 0));
        expectedAppointmentDatesList.add(appointmentInstance);

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

    @Test
    public void shouldReturnAppointmentsForNextWeekWithFourOccurences()
            throws ParseException {
        Date appointmentStartDateTime = getDate(2019, Calendar.JUNE, 6, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.JUNE, 6, 16, 30, 00);

        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(1, 4,
                null, "THURSDAY,FRIDAY");
        recurringAppointmentsGenerationService = new WeeklyRecurringAppointmentsGenerationService(appointmentRecurringPattern,
                appointmentRequest, appointmentMapperMock);

        Mockito.when(appointmentMapperMock.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());

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

        List<Appointment> appointments = recurringAppointmentsGenerationService.getAppointments();

        assertEquals(expectedAppointmentDatesList.size(), appointments.size());
        for (int i = 0; i < appointments.size(); i++) {
            assertEquals(expectedAppointmentDatesList.get(i).get("startDateTime").toString(),
                    appointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointmentDatesList.get(i).get("endDateTime").toString(),
                    appointments.get(i).getEndDateTime().toString());
        }
    }

}