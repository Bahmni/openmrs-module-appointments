package org.openmrs.module.appointments.service.impl;


import org.apache.commons.lang.time.DateUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentNumberGenerator;
import org.openmrs.module.appointments.service.AppointmentNumberGeneratorLocator;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberingStrategy;
import org.openmrs.module.appointments.service.RecurringAppointmentNumberingStrategyLocator;
import org.openmrs.module.appointments.util.AppointmentBuilder;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.model.AppointmentStatus.CheckedIn;
import static org.openmrs.module.appointments.model.AppointmentStatus.Requested;
import static org.openmrs.module.appointments.model.AppointmentStatus.Scheduled;

@RunWith(MockitoJUnitRunner.class)
public class AppointmentRecurringPatternServiceImplTest {

    @InjectMocks
    private AppointmentRecurringPatternServiceImpl recurringAppointmentService;

    @Mock
    private AppointmentRecurringPatternDao appointmentRecurringPatternDao;

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private AppointmentStatusChangeValidator statusChangeValidator;

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private Patient patient;

    @Spy
    private List<AppointmentValidator> appointmentValidators = new ArrayList<>();

    @Spy
    private List<AppointmentValidator> editAppointmentValidators = new ArrayList<>();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    AppointmentNumberGenerator appointmentNumberGenerator;

    @Mock
    RecurringAppointmentNumberingStrategy recurringAppointmentNumberingStrategy;

    @Mock
    RecurringAppointmentNumberingStrategyLocator recurringAppointmentNumberingStrategyLocator;

    @Test
    public void shouldSaveRecurringAppointmentsForGivenRecurringPatternAndAppointment() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        List<Appointment> appointments = Collections.singletonList(appointment);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String notes = "Notes";
        String generatedNumber = "TEST123";

        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        // Mock strategy to set the appointment number
        doAnswer(invocation -> {
            List<Appointment> appts = invocation.getArgument(0);
            appts.forEach(a -> a.setAppointmentNumber(generatedNumber));
            return null;
        }).when(recurringAppointmentNumberingStrategy).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));

        //setting up the locators
        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        AppointmentRecurringPattern savedAppointmentRecurringPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        List<Appointment> appointmentsList = new ArrayList<>(savedAppointmentRecurringPattern.getAppointments());
        assertEquals(1, appointmentsList.size());
        verify(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(recurringAppointmentNumberingStrategy).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));
        assertEquals(1, appointmentRecurringPattern.getAppointments().size());
        assertEquals(1, appointmentsList.get(0).getAppointmentAudits().size());
    }

    @Test
    public void shouldUpdateStatusOfScheduledFutureOccurrences() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 0, 0);
        endTimeCalendar.set(year, month, day, 10, 30, 0);
        newStartTimeCalendar.set(year, month, day, 12, 0, 0);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 0);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = getAppointment("uuid1", patient,
                Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = getAppointment("uuid2", patient,
                CheckedIn,
                AppointmentKind.Scheduled,
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment appointmentThree = getAppointment("uuid3", patient,
                Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(appointmentTwo);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateStatusOfRequestedFutureOccurrences() {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 0, 0);
        endTimeCalendar.set(year, month, day, 10, 30, 0);
        newStartTimeCalendar.set(year, month, day, 12, 0, 0);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 0);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = getAppointment("uuid1", patient,
                Requested,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = getAppointment("uuid2", patient,
                CheckedIn,
                AppointmentKind.Scheduled,
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment appointmentThree = getAppointment("uuid3", patient,
                Requested,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(appointmentTwo);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateStatusOfScheduledCurrentAndFutureOccurrences() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        Calendar newStartTimeCalendar = Calendar.getInstance();
        Calendar newEndTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 10, 0, 0);
        endTimeCalendar.set(year, month, day, 10, 30, 0);
        newStartTimeCalendar.set(year, month, day, 12, 0, 0);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 0);
        newEndTimeCalendar.set(Calendar.MILLISECOND, 0);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = getAppointment("uuid1", patient,
                Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = getAppointment("uuid2", patient,
                Scheduled,
                AppointmentKind.Scheduled,
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment appointmentThree = getAppointment("uuid3", patient,
                Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, appointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(appointmentTwo);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), nullable(String.class));
    }

    @Test
    public void shouldSaveAndReturnTheUpdatedRecurringPatternWhenUpdateIsCalled() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        List<Appointment> appointments = Collections.singletonList(appointment);
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String notes = "Notes";
        String generatedNumber = "TEST123";

        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);

        // Mock strategy to set the appointment number
        doAnswer(invocation -> {
            List<Appointment> appts = invocation.getArgument(0);
            appts.forEach(a -> a.setAppointmentNumber(generatedNumber));
            return null;
        }).when(recurringAppointmentNumberingStrategy).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        recurringAppointmentService.update(appointmentRecurringPattern, appointment);

        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(recurringAppointmentNumberingStrategy).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnAppointmentUpdate() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        List<Appointment> appointments = Collections.singletonList(appointment);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String errorMessage = "Appointment cannot be updated without Patient";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper)
                .validate(appointment, editAppointmentValidators);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        recurringAppointmentService.update(appointmentRecurringPattern, appointment);

        verify(appointmentDao, never()).save(any(Appointment.class));
        verify(appointmentServiceHelper,never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentNumberGenerator, never()).generateAppointmentNumber(any());
    }

    @Test
    public void shouldAddAuditAppointmentNumberToOnlyUpdatedAppointments() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment voidedAppointment = new Appointment();
        voidedAppointment.setUuid("voidedApp");
        voidedAppointment.setVoided(true);
        Appointment newAppointment = new Appointment();
        newAppointment.setUuid("newApp");
        List<Appointment> updatedAppointments = Arrays.asList(voidedAppointment, newAppointment);

        //setting up the number generator and strategy locators
        recurringAppointmentService.setAppointmentNumberGeneratorLocator(new AppointmentNumberGeneratorLocatorImpl(new DefaultAppointmentNumberGeneratorImpl()));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(new DefaultRecurringAppointmentNumberingStrategyImpl(
                        new AppointmentNumberGeneratorLocatorImpl(new DefaultAppointmentNumberGeneratorImpl()))));
        String expectedAppointmentNumberPart = new SimpleDateFormat("YYMMddHHmm").format(new Date());
        Appointment appointment = recurringAppointmentService.update(appointmentRecurringPattern, updatedAppointments);

        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper, times(2)).getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentServiceHelper, times(2)).getAppointmentAuditEvent(any(Appointment.class), nullable(String.class));
        updatedAppointments.forEach(app -> assertTrue("Should have generated appointment number", app.getAppointmentNumber().startsWith(expectedAppointmentNumberPart)));
        assertEquals(voidedAppointment.getAppointmentNumber(), newAppointment.getAppointmentNumber());
        assertEquals(newAppointment, appointment);
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnSingleAppointmentUpdate() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment voidedAppointment = new Appointment();
        voidedAppointment.setUuid("voidedApp");
        Appointment newAppointment = new Appointment();
        newAppointment.setUuid("newApp");
        List<Appointment> updatedAppointments = Arrays.asList(voidedAppointment, newAppointment);
        String errorMessage = "Appointment cannot be updated without Patient";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper)
                .validate(any(), anyListOf(AppointmentValidator.class));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        recurringAppointmentService.update(appointmentRecurringPattern, updatedAppointments);

        verify(appointmentDao, never()).save(any(Appointment.class));
        verify(appointmentServiceHelper,never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentNumberGenerator, never()).generateAppointmentNumber(any());
    }


    @Test
    public void shouldAssignSameAppointmentNumberToAllNewRecurringAppointments() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        Appointment appointmentThree = new Appointment();
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo, appointmentThree);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));

        String sharedAppointmentNumber = "TEST123";
        String notes = "Notes";
        AppointmentAudit appointmentAudit = new AppointmentAudit();

        when(appointmentNumberGenerator.generateAppointmentNumber(any(Appointment.class)))
                .thenReturn(sharedAppointmentNumber);

        // Mock strategy to use the mocked generateAppointmentNumber
        doAnswer(invocation -> {
            List<Appointment> appts = invocation.getArgument(0);
            String number = appointmentNumberGenerator.generateAppointmentNumber(appts.get(0));
            appts.forEach(a -> {
                if (a.getAppointmentNumber() == null) {
                    a.setAppointmentNumber(number);
                }
            });
            return null;
        }).when(recurringAppointmentNumberingStrategy).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));

        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(any());
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(any(), anyString());
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        AppointmentRecurringPattern savedPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        assertEquals(sharedAppointmentNumber, appointmentOne.getAppointmentNumber());
        assertEquals(sharedAppointmentNumber, appointmentTwo.getAppointmentNumber());
        assertEquals(sharedAppointmentNumber, appointmentThree.getAppointmentNumber());
        verify(recurringAppointmentNumberingStrategy, times(1)).applyAppointmentNumbers(
                anyListOf(Appointment.class), any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldReuseExistingAppointmentNumberWhenAddingAppointmentsToExistingSeries() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        String existingNumber = "EXISTING123";

        Appointment existingAppointmentOne = new Appointment();
        existingAppointmentOne.setAppointmentNumber(existingNumber);
        Appointment existingAppointmentTwo = new Appointment();
        existingAppointmentTwo.setAppointmentNumber(existingNumber);
        Appointment newAppointment = new Appointment();
        newAppointment.setAppointmentNumber(null);

        List<Appointment> appointments = Arrays.asList(
                existingAppointmentOne, existingAppointmentTwo, newAppointment);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));

        String notes = "Notes";
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(any());
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(any(), anyString());
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(
                        new DefaultRecurringAppointmentNumberingStrategyImpl(
                                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator))));
        AppointmentRecurringPattern savedPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        assertEquals(existingNumber, newAppointment.getAppointmentNumber());
        assertEquals(existingNumber, existingAppointmentOne.getAppointmentNumber());
        assertEquals(existingNumber, existingAppointmentTwo.getAppointmentNumber());
        verify(appointmentNumberGenerator, never()).generateAppointmentNumber(any());
    }

    @Test
    public void shouldPreserveSingleAppointmentNumberWhenAlreadyAssigned() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        String preservedNumber = "PRESERVE123";

        Appointment appointment = new Appointment();
        appointment.setAppointmentNumber(preservedNumber);

        List<Appointment> appointments = Collections.singletonList(appointment);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));

        String notes = "Notes";
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(any());
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(any(), anyString());
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));
        AppointmentRecurringPattern savedPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        assertEquals(preservedNumber, appointment.getAppointmentNumber());
        verify(appointmentNumberGenerator, never()).generateAppointmentNumber(any());
    }

    @Test
    public void shouldHandleNullGeneratorGracefullyForAllAppointments() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointmentOne = new Appointment();
        Appointment appointmentTwo = new Appointment();
        List<Appointment> appointments = Arrays.asList(appointmentOne, appointmentTwo);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));

        String notes = "Notes";
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(any());
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(any(), anyString());
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(null));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(
                        new DefaultRecurringAppointmentNumberingStrategyImpl(
                                new AppointmentNumberGeneratorLocatorImpl(null))));
        AppointmentRecurringPattern savedPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        assertNull(appointmentOne.getAppointmentNumber());
        assertNull(appointmentTwo.getAppointmentNumber());
    }

    @Test(expected = APIException.class)
    public void shouldThrowExceptionForEmptyAppointmentList() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setAppointments(new HashSet<>());

        recurringAppointmentService.setAppointmentNumberGeneratorLocator(
                new AppointmentNumberGeneratorLocatorImpl(appointmentNumberGenerator));
        recurringAppointmentService.setRecurringAppointmentNumberingStrategyLocator(
                new RecurringAppointmentNumberingStrategyLocatorImpl(recurringAppointmentNumberingStrategy));

        recurringAppointmentService.validateAndSave(appointmentRecurringPattern);
    }

    private Appointment getAppointment(String uuid, Patient patient, AppointmentStatus appointmentStatus,
                                       AppointmentKind appointmentKind, Date start, Date end) {
        return new AppointmentBuilder().withUuid(uuid)
                .withPatient(patient)
                .withStatus(appointmentStatus)
                .withAppointmentKind(appointmentKind)
                .withStartDateTime(start)
                .withEndDateTime(end)
                .build();
    }

}
