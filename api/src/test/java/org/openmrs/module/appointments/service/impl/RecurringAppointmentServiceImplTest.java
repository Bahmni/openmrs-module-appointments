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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentRecurringPatternDao;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
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
import static org.openmrs.module.appointments.model.AppointmentProviderResponse.*;
import static org.openmrs.module.appointments.model.AppointmentStatus.Cancelled;
import static org.openmrs.module.appointments.model.AppointmentStatus.CheckedIn;
import static org.openmrs.module.appointments.model.AppointmentStatus.Completed;
import static org.openmrs.module.appointments.model.AppointmentStatus.Scheduled;

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
        appointmentRecurringPattern.setAppointments(new HashSet<>(appointments));
        String notes = "Notes";
        doReturn(notes).when(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        doReturn(appointmentAudit).when(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        doNothing().when(appointmentRecurringPatternDao).save(appointmentRecurringPattern);

        List<Appointment> appointmentsList = recurringAppointmentService
                .validateAndSave(appointmentRecurringPattern);

        assertEquals(1, appointmentsList.size());
        verify(appointmentRecurringPatternDao).save(appointmentRecurringPattern);
        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentServiceHelper).checkAndAssignAppointmentNumber(appointment);
        assertEquals(1, appointmentRecurringPattern.getAppointments().size());
        assertEquals(1, appointmentsList.get(0).getAppointmentAudits().size());
    }

    @Test
    public void shouldReturnEmptyAppointmentsWithoutSavingWhenAppointmentsAreEmpty() {
        AppointmentRecurringPattern appointmentRecurringPattern = mock(AppointmentRecurringPattern.class);
        when(appointmentRecurringPattern.getAppointments()).thenReturn(new HashSet<>());
        when(appointmentRecurringPattern.isAppointmentsEmptyOrNull()).thenReturn(true);
        List<Appointment> appointmentsList = recurringAppointmentService.validateAndSave(appointmentRecurringPattern);
        assertEquals(0, appointmentsList.size());
        verify(appointmentRecurringPatternDao, never()).save(any(AppointmentRecurringPattern.class));
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
                .withStatus(Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(Scheduled)
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
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
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
                .withStatus(Completed)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(DateUtils.addDays(startTimeCalendar.getTime(), -2))
                .withEndDateTime(DateUtils.addDays(endTimeCalendar.getTime(), -2))
                .build();
        Appointment oldAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(startTimeCalendar.getTime())
                .withEndDateTime(endTimeCalendar.getTime())
                .build();
        oldAppointmentTwo.setAppointmentAudits(new HashSet<>());
        Appointment newAppointmentTwo = new AppointmentBuilder()
                .withUuid("uuid2")
                .withPatient(patient)
                .withStatus(Scheduled)
                .withAppointmentKind(AppointmentKind.Scheduled)
                .withStartDateTime(newStartTimeCalendar.getTime())
                .withEndDateTime(newEndTimeCalendar.getTime())
                .build();
        Appointment appointmentThree = new AppointmentBuilder()
                .withUuid("uuid3")
                .withPatient(patient)
                .withStatus(Cancelled)
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

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", new Date(), "");
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

        recurringAppointmentService.changeStatus(appointmentTwo, "Cancelled", new Date(), "");
        verify(appointmentDao, times(2)).save(any(Appointment.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
    }

    @Test
    public void shouldUpdateMetadataForAllPendingRecurringAppointments() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("serviceUuid");
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setUuid("serviceTypeUuid");
        Location location = new Location();
        location.setUuid("locationUuid");
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, appointmentServiceDefinition, appointmentServiceType,
                location, appointmentProviders, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(2, appointmentList.size());

        assertEquals(appointmentServiceDefinition, appointmentList.get(0).getService());
        assertEquals(appointmentServiceDefinition, appointmentList.get(1).getService());
        assertEquals(appointmentServiceType, appointmentList.get(0).getServiceType());
        assertEquals(appointmentServiceType, appointmentList.get(1).getServiceType());
        assertEquals(location, appointmentList.get(0).getLocation());
        assertEquals(location, appointmentList.get(1).getLocation());
        AppointmentProvider pendingAppOneAppProvider = (AppointmentProvider) appointmentList.get(0).getProviders().toArray()[0];
        AppointmentProvider pendingAppTwoAppProvider = (AppointmentProvider) appointmentList.get(1).getProviders().toArray()[0];
        assertEquals(new Integer(101), pendingAppOneAppProvider.getProvider().getProviderId());
        assertEquals(new Integer(101), pendingAppTwoAppProvider.getProvider().getProviderId());
        assertEquals(newComments, appointmentList.get(0).getComments());
        assertEquals(newComments, appointmentList.get(1).getComments());

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateMetadataForThatAppointmentWhenNoPendingRecurringAppointments() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Appointment appointmentOne = createAppointment("uuid1", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("serviceUuid");
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setUuid("serviceTypeUuid");
        Location location = new Location();
        location.setUuid("locationUuid");
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, appointmentServiceDefinition, appointmentServiceType,
                location, appointmentProviders, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
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

        assertEquals(3, appointmentList.size());

        assertEquals(appointmentServiceDefinition, appointmentList.get(0).getService());
        assertEquals(appointmentServiceDefinition, appointmentList.get(1).getService());
        assertEquals(appointmentServiceDefinition, appointmentList.get(2).getService());
        assertEquals(appointmentServiceType, appointmentList.get(0).getServiceType());
        assertEquals(appointmentServiceType, appointmentList.get(1).getServiceType());
        assertEquals(appointmentServiceType, appointmentList.get(2).getServiceType());
        assertEquals(location, appointmentList.get(0).getLocation());
        assertEquals(location, appointmentList.get(1).getLocation());
        assertEquals(location, appointmentList.get(2).getLocation());
        AppointmentProvider pendingAppOneAppProvider = (AppointmentProvider) appointmentList.get(0).getProviders().toArray()[0];
        AppointmentProvider pendingAppTwoAppProvider = (AppointmentProvider) appointmentList.get(1).getProviders().toArray()[0];
        AppointmentProvider pendingAppThreeAppProvider = (AppointmentProvider) appointmentList.get(2).getProviders().toArray()[0];
        assertEquals(new Integer(101), pendingAppOneAppProvider.getProvider().getProviderId());
        assertEquals(new Integer(101), pendingAppTwoAppProvider.getProvider().getProviderId());
        assertEquals(new Integer(101), pendingAppThreeAppProvider.getProvider().getProviderId());
        assertEquals(newComments, appointmentList.get(0).getComments());
        assertEquals(newComments, appointmentList.get(1).getComments());
        assertEquals(newComments, appointmentList.get(2).getComments());

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(3)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateNewProviderForAllPendingOccurrances() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(new Provider(101));
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));
        Appointment appointmentOne = createAppointment("uuid1", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));

        AppointmentProvider newAppointmentProvider = new AppointmentProvider();
        newAppointmentProvider.setProvider(new Provider(102));
        HashSet<AppointmentProvider> newAppointmentProviders = new HashSet<>(Collections.singleton(newAppointmentProvider));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), newAppointmentProviders, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(3, appointmentList.size());

        assertEquals(2, appointmentList.get(0).getProviders().size());
        assertEquals(2, appointmentList.get(1).getProviders().size());
        assertEquals(2, appointmentList.get(2).getProviders().size());

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(3)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateNewProviderAlongTheOldOneForAllPendingOccurrances() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        Provider providerOne = new Provider(101);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(providerOne);
        HashSet<AppointmentProvider> appointmentProviders = new HashSet<>(Collections.singleton(appointmentProvider));
        Appointment appointmentOne = createAppointment("uuid1", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProviders, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));

        AppointmentProvider newAppointmentProviderOne = new AppointmentProvider();
        newAppointmentProviderOne.setProvider(providerOne);
        AppointmentProvider newAppointmentProviderTwo = new AppointmentProvider();
        newAppointmentProviderTwo.setProvider(new Provider(102));
        HashSet<AppointmentProvider> newAppointmentProviders = new HashSet<>(Arrays.asList(newAppointmentProviderOne, newAppointmentProviderTwo));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), newAppointmentProviders, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(3, appointmentList.size());

        assertEquals(2, appointmentList.get(0).getProviders().size());
        assertEquals(2, appointmentList.get(1).getProviders().size());
        assertEquals(2, appointmentList.get(2).getProviders().size());

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(3))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(3)).save(any(Appointment.class));
    }

    @Test
    public void shouldUpdateNewMultipleProvidersForAllPendingOccurrances() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProviderOne = new AppointmentProvider();
        appointmentProviderOne.setProvider(new Provider(101));
        HashSet<AppointmentProvider> appointmentProvidersTwo = new HashSet<>(Collections.singletonList(appointmentProviderOne));
        HashSet<AppointmentProvider> appointmentProvidersThree = new HashSet<>(Collections.singletonList(appointmentProviderOne));
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersTwo, "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersThree, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        AppointmentProvider newAppointmentProviderOne = new AppointmentProvider();
        newAppointmentProviderOne.setProvider(new Provider(102));
        AppointmentProvider newAppointmentProviderTwo = new AppointmentProvider();
        newAppointmentProviderTwo.setProvider(new Provider(103));
        HashSet<AppointmentProvider> newAppointmentProviders = new HashSet<>(Arrays.asList(newAppointmentProviderOne,
                newAppointmentProviderTwo));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), newAppointmentProviders, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(2, appointmentList.size());
        assertEquals(3, appointmentList.get(0).getProviders().size());
        assertEquals(3, appointmentList.get(1).getProviders().size());

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    @Test
    public void shouldSetAllProvidersToCancelWhenAllProvidersRemoved() throws IOException {
        Calendar startTimeCalendar = Calendar.getInstance();
        Calendar endTimeCalendar = Calendar.getInstance();
        int year = startTimeCalendar.get(Calendar.YEAR);
        int month = startTimeCalendar.get(Calendar.MONTH);
        int day = startTimeCalendar.get(Calendar.DATE);
        startTimeCalendar.set(year, month, day, 18, 00, 00);
        endTimeCalendar.set(year, month, day, 18, 30, 00);
        AppointmentRecurringPattern appointmentRecurringPattern = new AppointmentRecurringPattern();
        appointmentRecurringPattern.setType(RecurringAppointmentType.DAY);
        appointmentRecurringPattern.setFrequency(3);
        appointmentRecurringPattern.setPeriod(2);
        AppointmentProvider appointmentProviderOne = new AppointmentProvider();
        appointmentProviderOne.setProvider(new Provider(101));
        AppointmentProvider appointmentProviderTwo = new AppointmentProvider();
        appointmentProviderTwo.setProvider(new Provider(102));
        AppointmentProvider appointmentProviderThree = new AppointmentProvider();
        appointmentProviderThree.setProvider(new Provider(103));
        HashSet<AppointmentProvider> appointmentProvidersTwo = new HashSet<>(Arrays.asList(appointmentProviderOne,appointmentProviderTwo,appointmentProviderThree));
        HashSet<AppointmentProvider> appointmentProvidersThree = new HashSet<>(Collections.singletonList(appointmentProviderOne));
        Appointment appointmentOne = createAppointment("uuid1", patient, Completed,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), new HashSet<>(), "appOne",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        Appointment oldAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersTwo, "oldAppTwo", startTimeCalendar.getTime(),
                endTimeCalendar.getTime());
        Appointment appointmentThree = createAppointment("uuid3", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), appointmentProvidersThree, "appThree",
                DateUtils.addDays(startTimeCalendar.getTime(), 2),
                DateUtils.addDays(endTimeCalendar.getTime(), 2));
        String newComments = "newComments";
        Appointment newAppointmentTwo = createAppointment("uuid2", patient, Scheduled,
                AppointmentKind.Scheduled, new AppointmentServiceDefinition(), new AppointmentServiceType(),
                new Location(), null, newComments, startTimeCalendar.getTime(), endTimeCalendar.getTime());
        appointmentThree.setAppointmentAudits(new HashSet<>());
        Set<Appointment> appointments = new HashSet<>(Arrays.asList(appointmentOne, oldAppointmentTwo, appointmentThree));
        appointmentRecurringPattern.setAppointments(appointments);
        appointmentOne.setAppointmentRecurringPattern(appointmentRecurringPattern);
        oldAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        appointmentThree.setAppointmentRecurringPattern(appointmentRecurringPattern);
        newAppointmentTwo.setAppointmentRecurringPattern(appointmentRecurringPattern);
        when(appointmentDao.getAppointmentByUuid(anyString())).thenReturn(oldAppointmentTwo);
        doNothing().when(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);

        List<Appointment> appointmentList = recurringAppointmentService.validateAndUpdate(newAppointmentTwo, "");

        assertEquals(2, appointmentList.size());
        assertEquals(3, appointmentList.get(0).getProviders().size());
        assertEquals(1, appointmentList.get(1).getProviders().size());

        assertEquals(3, appointmentList.get(0).getProviders()
                .stream()
                .filter(provider -> provider.getResponse().equals(CANCELLED)).toArray().length);

        assertEquals(1, appointmentList.get(1).getProviders()
                .stream()
                .filter(provider -> provider.getResponse().equals(CANCELLED)).toArray().length);

        verify(appointmentServiceHelper).validate(oldAppointmentTwo, editAppointmentValidators);
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentServiceHelper, times(2))
                .getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentDao).getAppointmentByUuid(anyString());
        verify(appointmentDao, times(2)).save(any(Appointment.class));
    }

    private Appointment createAppointment(String uuid, Patient patient, AppointmentStatus appointmentStatus,
                                          AppointmentKind appointmentKind,
                                          AppointmentServiceDefinition appointmentServiceDefinition,
                                          AppointmentServiceType appointmentServiceType,
                                          Location location, Set<AppointmentProvider> appointmentProviders,
                                          String comments, Date startDateTime, Date endDateTime) {
        return new AppointmentBuilder()
                .withUuid(uuid)
                .withPatient(patient)
                .withStatus(appointmentStatus)
                .withAppointmentKind(appointmentKind)
                .withService(appointmentServiceDefinition)
                .withServiceType(appointmentServiceType)
                .withLocation(location)
                .withProviders(appointmentProviders)
                .withComments(comments)
                .withStartDateTime(startDateTime)
                .withEndDateTime(endDateTime)
                .build();
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
