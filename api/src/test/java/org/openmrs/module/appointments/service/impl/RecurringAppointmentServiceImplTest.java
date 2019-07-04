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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecurringAppointmentServiceImplTest {

    @InjectMocks
    private RecurringAppointmentServiceImpl recurringAppointmentService;

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

        doNothing().when(appointmentServiceHelper).validate(appointment, appointmentValidators);
        String notes = "Notes";
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        List<Appointment> appointmentsList = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern, appointments);

        assertEquals(1, appointmentsList.size());
        verify(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).validate(appointment, appointmentValidators);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentServiceHelper).checkAndAssignAppointmentNumber(appointment);
        assertEquals(1, appointmentRecurringPattern.getAppointments().size());
        assertEquals(1, appointmentsList.get(0).getAppointmentAudits().size());
    }

    @Test
    public void shouldReturnNullWhenAppointsAreNull() {
        List<Appointment> appointmentsList = recurringAppointmentService.validateAndSave(
                mock(AppointmentRecurringPattern.class), Collections.emptyList());
        assertEquals(0, appointmentsList.size());
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldReturnEmptyAppointmentsWithoutSavingWhenAppointmentsAreEmpty() {
        List<Appointment> appointmentsList = recurringAppointmentService.validateAndSave(
                mock(AppointmentRecurringPattern.class), null);
        assertNull(appointmentsList);
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
    }

    @Test
    public void shouldThrowExceptionIfAppointmentValidationFails() throws IOException {
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        String errorMessage = "Appointment cannot be created without Patient";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper).validate(any(Appointment.class), anyListOf(AppointmentValidator.class));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);
        recurringAppointmentService.validateAndSave(appointmentRecurringPattern,
                Collections.singletonList(new Appointment()));
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
        verify(appointmentServiceHelper, never()).validate(any(), any());
        verify(appointmentServiceHelper, never()).getAppointmentAsJsonString(any());
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(), any());
        verify(appointmentServiceHelper, never()).checkAndAssignAppointmentNumber(any());
    }

    @Test
    public void shouldUpdateStartTimeAndEndTimeForPendingOccurrences() throws IOException {
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
        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(2, appointmentList.size());
        assertEquals(newStartTimeCalendar.getTime(), appointmentList.get(0).getStartDateTime());
        assertEquals(newEndTimeCalendar.getTime(), appointmentList.get(0).getEndDateTime());
        assertEquals(DateUtils.addDays(newStartTimeCalendar.getTime(), 2), appointmentList.get(1).getStartDateTime());
        assertEquals(DateUtils.addDays(newEndTimeCalendar.getTime(), 2), appointmentList.get(1).getEndDateTime());
        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class),any(String.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    @Test
    public void shouldThrowExceptionWhenThereIsErrorWhileValidatingBeforeUpdate() throws IOException {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        patient.setUuid("p1");
        appointment.setPatient(patient);
        appointment.setService(null);
        appointment.setUuid("1");
        String errorMessage = "Appointment cannot be updated without Service";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper).validate(any(Appointment.class), anyListOf(AppointmentValidator.class));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);

        recurringAppointmentService.validateAndUpdate(appointment, "");

        verify(appointmentServiceHelper, never()).getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentDao, never()).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateStartTimeAndEndTimeOnlyForScheduledPendingOccurrences() throws IOException {
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
        Appointment appointmentOne = new AppointmentBuilder()
                .withUuid("uuid1")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(AppointmentStatus.Cancelled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), 2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), 2))
                .build();
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        List<String> errors = new ArrayList<>();
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(1, appointmentList.size());
        assertEquals(newStartTimeCalendar.getTime(), appointmentList.get(0).getStartDateTime());
        assertEquals(newEndTimeCalendar.getTime(), appointmentList.get(0).getEndDateTime());
        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(1))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(1))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(1)).save(any(Appointment.class));
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
                AppointmentStatus.Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = getAppointment("uuid2", patient,
                AppointmentStatus.CheckedIn,
                AppointmentKind.Scheduled,
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment appointmentThree = getAppointment("uuid3", patient,
                AppointmentStatus.Scheduled,
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
        appointmentAudit.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", new Date(), "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
        verify(appointmentServiceHelper, times(1)).getAppointmentAuditEvent(appointmentThree, null);
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
                AppointmentStatus.Scheduled,
                AppointmentKind.Scheduled,
                DateUtils.addDays(startTimeCalendar.getTime(), -2),
                DateUtils.addDays(endTimeCalendar.getTime(), -2));
        Appointment appointmentTwo = getAppointment("uuid2", patient,
                AppointmentStatus.Scheduled,
                AppointmentKind.Scheduled,
                startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        appointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment appointmentThree = getAppointment("uuid3", patient,
                AppointmentStatus.Scheduled,
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
        appointmentAudit.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointmentTwo, null)).thenReturn(appointmentAudit);

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", new Date(), "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class),any(String.class));
        verify(appointmentServiceHelper, times(2)).getAppointmentAsJsonString(any(Appointment.class));
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
