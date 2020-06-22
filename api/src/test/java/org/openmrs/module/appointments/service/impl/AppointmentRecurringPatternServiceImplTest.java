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
import org.openmrs.module.appointments.util.AppointmentBuilder;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
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

    @Test
    public void shouldSaveRecurringAppointmentsForGivenRecurringPatternAndAppointment() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        List<Appointment> appointments = Collections.singletonList(appointment);
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String notes = "Notes";
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        AppointmentRecurringPattern savedAppointmentRecurringPattern = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        List<Appointment> appointmentsList = new ArrayList<>(savedAppointmentRecurringPattern.getAppointments());
        assertEquals(1, appointmentsList.size());
        verify(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentServiceHelper).checkAndAssignAppointmentNumber(appointment);
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
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
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
        doNothing().when(appointmentServiceHelper).validate(appointmentTwo, editAppointmentValidators);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

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
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
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
        doNothing().when(appointmentServiceHelper).validate(appointmentTwo, editAppointmentValidators);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

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
        startTimeCalendar.set(year, month, day, 10, 00, 00);
        endTimeCalendar.set(year, month, day, 10, 30, 00);
        newStartTimeCalendar.set(year, month, day, 12, 00, 00);
        newStartTimeCalendar.set(Calendar.MILLISECOND, 0);
        newEndTimeCalendar.set(year, month, day, 12, 30, 00);
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
        doNothing().when(appointmentServiceHelper).validate(appointmentTwo, editAppointmentValidators);

        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            return null;
        }).when(statusChangeValidator).validate(any(Appointment.class), any(AppointmentStatus.class), anyListOf(String.class));

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointmentTwo);
        appointmentAudit.setStatus(CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
    }

    @Test
    public void shouldSaveAndReturnTheUpdatedRecurringPatternWhenUpdateIsCalled() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        Appointment appointment = new Appointment();
        List<Appointment> appointments = Collections.singletonList(appointment);
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String notes = "Notes";
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        recurringAppointmentService.update(appointmentRecurringPattern, appointment);

        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentServiceHelper).checkAndAssignAppointmentNumber(appointment);
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

        recurringAppointmentService.update(appointmentRecurringPattern, appointment);

        verify(appointmentDao, never()).save(any(Appointment.class));
        verify(appointmentServiceHelper,never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentServiceHelper, never()).checkAndAssignAppointmentNumber(any());
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

        Appointment appointment = recurringAppointmentService.update(appointmentRecurringPattern, updatedAppointments);

        verify(appointmentRecurringPatternDao, times(1)).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper, times(2)).getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentServiceHelper, times(2)).getAppointmentAuditEvent(any(Appointment.class), anyString());
        verify(appointmentServiceHelper, times(2)).checkAndAssignAppointmentNumber(any(Appointment.class));
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

        recurringAppointmentService.update(appointmentRecurringPattern, updatedAppointments);

        verify(appointmentDao, never()).save(any(Appointment.class));
        verify(appointmentServiceHelper,never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentServiceHelper, never()).checkAndAssignAppointmentNumber(any());
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
