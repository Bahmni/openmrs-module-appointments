package org.openmrs.module.appointments.web.service.impl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.service.impl.RecurringAppointmentType;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.RecurringPattern;
import org.openmrs.module.appointments.web.mapper.AppointmentMapper;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;
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

    private AppointmentRequest getAppointmentRequest(Date appointmentStartDateTime, Date appointmentEndDateTime) {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setStartDateTime(appointmentStartDateTime);
        appointmentRequest.setEndDateTime(appointmentEndDateTime);
        return appointmentRequest;
    }
    private AppointmentRecurringPattern getAppointmentRecurringPattern(int period, int frequency, Date endDate) {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setPeriod(period);
        appointmentRecurringPattern.setFrequency(frequency);
        appointmentRecurringPattern.setEndDate(endDate);
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        return appointmentRecurringPattern;
    }

    @Test
    public void shouldReturnAppointmentsForGivenRecurringPatternAndAppointmentRequest() {
        Date appointmentStartDateTime = getDate(2019, Calendar.MAY, 13, 16, 00, 00);
        Date appointmentEndDateTime = getDate(2019, Calendar.MAY, 13, 16, 30, 00);
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3,
                2, null);
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.getAppointments(
                appointmentRecurringPattern, appointmentRequest);
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
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 2,
                null);
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.getAppointments(appointmentRecurringPattern,
                appointmentRequest);
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
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 23, 45, 00));
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.getAppointments(appointmentRecurringPattern,
                appointmentRequest);
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
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
        List<Appointment> appointments = dailyRecurringAppointmentsGenerationService.getAppointments(appointmentRecurringPattern, appointmentRequest);
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
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 0,
                getDate(2019, Calendar.MAY, 25, 8, 45, 00));
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setEndDate(getDate(2019, Calendar.MAY, 31, 8, 45, 0));
        recurringPattern.setFrequency(0);
        appointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
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
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, appointmentRequest);
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
        AppointmentRequest appointmentRequest = getAppointmentRequest(appointmentStartDateTime, appointmentEndDateTime);
        AppointmentRecurringPattern appointmentRecurringPattern = getAppointmentRecurringPattern(3, 3,null);
        RecurringPattern recurringPattern = new RecurringPattern();
        recurringPattern.setFrequency(5);
        appointmentRequest.setRecurringPattern(recurringPattern);
        Mockito.when(appointmentMapper.fromRequest(appointmentRequest)).thenAnswer(x -> new Appointment());
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
        List<Appointment> updatedAppointments = dailyRecurringAppointmentsGenerationService.addAppointments(appointmentRecurringPattern, appointmentRequest);
        assertEquals(expectedAppointments.size(), updatedAppointments.size());
        for (int i = 0; i < updatedAppointments.size(); i++) {
            assertEquals(expectedAppointments.get(i).getStartDateTime().toString(),
                    updatedAppointments.get(i).getStartDateTime().toString());
            assertEquals(expectedAppointments.get(i).getEndDateTime().toString(),
                    updatedAppointments.get(i).getEndDateTime().toString());
        }
    }
}