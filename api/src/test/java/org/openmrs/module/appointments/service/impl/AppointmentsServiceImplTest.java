package org.openmrs.module.appointments.service.impl;

import org.bahmni.module.email.notification.service.EmailNotificationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.conflicts.AppointmentConflict;
import org.openmrs.module.appointments.conflicts.impl.AppointmentServiceUnavailabilityConflict;
import org.openmrs.module.appointments.conflicts.impl.PatientDoubleBookingConflict;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.event.TeleconsultationAppointmentSavedEvent;
import org.openmrs.module.appointments.helper.AppointmentServiceHelper;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentSearchRequest;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.appointments.constants.PrivilegeConstants.MANAGE_OWN_APPOINTMENTS;
import static org.openmrs.module.appointments.constants.PrivilegeConstants.RESET_APPOINTMENT_STATUS;
import static org.openmrs.module.appointments.helper.DateHelper.getDate;
import static org.openmrs.module.appointments.model.AppointmentConflictType.PATIENT_DOUBLE_BOOKING;
import static org.openmrs.module.appointments.model.AppointmentConflictType.SERVICE_UNAVAILABLE;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AppointmentsServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AppointmentDao appointmentDao;

    @Mock
    private AppointmentValidator appointmentValidator;

    @Mock
    private AppointmentStatusChangeValidator statusChangeValidator;

    @Spy
    private List<AppointmentStatusChangeValidator> statusChangeValidators = new ArrayList<>();

    @Spy
    private List<AppointmentValidator> appointmentValidators = new ArrayList<>();

    @Spy
    private List<AppointmentConflict> appointmentConflicts = new ArrayList<>();

    @Mock
    private AppointmentServiceHelper appointmentServiceHelper;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Mock
    private AppointmentAuditDao appointmentAuditDao;

    @Mock
    private User user;

    @Mock
    private Appointment appointment;

    @Mock
    private Provider provider;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private AppointmentServiceUnavailabilityConflict appointmentServiceUnavailabilityConflict;

    @Mock
    private PatientDoubleBookingConflict patientDoubleBookingConflict;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AppointmentsServiceImpl appointmentsService;

    private String exceptionCode = "error.privilegesRequired";

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        appointmentValidators.add(appointmentValidator);
        statusChangeValidators.add(statusChangeValidator);
        appointmentConflicts.add(appointmentServiceUnavailabilityConflict);
        appointmentConflicts.add(patientDoubleBookingConflict);
        setValuesForMemberFields(appointmentsService, "appointmentValidators", appointmentValidators);
        setValuesForMemberFields(appointmentsService, "statusChangeValidators", statusChangeValidators);
        setValuesForMemberFields(appointmentsService, "appointmentConflicts", appointmentConflicts);
    }

    public static void setValuesForMemberFields(Object classInstance, String fieldName, Object valueForMemberField)
            throws NoSuchFieldException, IllegalAccessException {
        setField(classInstance, valueForMemberField, classInstance.getClass().getDeclaredField(fieldName));
    }

    private static void setField(Object classInstance, Object valueForMemberField, Field field)
            throws IllegalAccessException {
        field.setAccessible(true);
        field.set(classInstance, valueForMemberField);
    }

    @Test
    public void testCreateAppointment() {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointment.setAppointmentAudits(new HashSet<>());
        appointmentsService.validateAndSave(appointment);
        verify(appointmentDao, times(1)).save(appointment);
    }

    @Test
    public void shouldCallCreateAuditEventOnSaveOfAppointment() throws IOException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentAudits(mock(HashSet.class));
        String notes = "";
        AppointmentAudit appointmentAuditMock = mock(AppointmentAudit.class);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes))
                .thenReturn(appointmentAuditMock);
        when(appointmentServiceHelper.getAppointmentAsJsonString(appointment)).thenReturn(notes);

        appointmentsService.validateAndSave(appointment);

        verify(appointmentServiceHelper, times(1)).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentDao, times(1)).save(appointment);
        verify(appointmentAuditDao, times(0)).save(appointmentAuditMock);
    }

    @Test
    public void shouldPublishTeleconsultationAppointmentSavedEvent() {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointment.setAppointmentAudits(new HashSet<>());
        appointment.setTeleconsultation(true);
        appointmentsService.validateAndSave(appointment);
        verify(applicationEventPublisher, times(1)).
                publishEvent(any(TeleconsultationAppointmentSavedEvent.class));
    }

    @Test
    public void shouldNotPublishTeleconsultationAppointmentSavedEventIfNotTeleconsultation() {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointment.setAppointmentAudits(new HashSet<>());
        appointment.setTeleconsultation(false);
        appointmentsService.validateAndSave(appointment);
        verify(applicationEventPublisher, times(0)).
                publishEvent(any(TeleconsultationAppointmentSavedEvent.class));
    }

    @Test
    public void testGetAllAppointments() {
        appointmentsService.getAllAppointments(null);
        verify(appointmentDao, times(1)).getAllAppointments(null);
    }

    @Test
    public void shouldNotGetAppointmentsWithVoidedService() {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        AppointmentServiceDefinition appointmentServiceDefinition1 = new AppointmentServiceDefinition();
        appointmentServiceDefinition1.setVoided(true);
        appointment1.setService(appointmentServiceDefinition1);
        Appointment appointment2 = new Appointment();
        appointment2.setService(new AppointmentServiceDefinition());
        appointments.add(appointment1);
        appointments.add(appointment2);
        when(appointmentDao.getAllAppointments(null)).thenReturn(appointments);
        List<Appointment> appointmentList = appointmentsService.getAllAppointments(null);
        verify(appointmentDao, times(1)).getAllAppointments(null);
        assertEquals(appointmentList.size(), 1);
    }

    @Test
    public void shouldNotGetAppointmentsWithVoidedServiceType() {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        AppointmentServiceDefinition appointmentServiceDefinition1 = new AppointmentServiceDefinition();
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setVoided(true);
        appointmentServiceDefinition1.setServiceTypes(Collections.singleton(appointmentServiceType1));
        appointment1.setService(appointmentServiceDefinition1);
        appointment1.setServiceType(appointmentServiceType1);
        appointments.add(appointment1);
        Appointment appointment2 = new Appointment();
        appointment2.setService(new AppointmentServiceDefinition());
        appointments.add(appointment2);
        when(appointmentDao.getAllAppointments(null)).thenReturn(appointments);
        List<Appointment> appointmentList = appointmentsService.getAllAppointments(null);
        verify(appointmentDao, times(1)).getAllAppointments(null);
        assertEquals(appointmentList.size(), 1);
    }

    @Test
    public void shouldGetAllFutureAppointmentsForTheGivenAppointmentService() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        when(appointmentDao.getAllFutureAppointmentsForService(appointmentServiceDefinition)).thenReturn(new ArrayList<>());

        appointmentsService.getAllFutureAppointmentsForService(appointmentServiceDefinition);

        verify(appointmentDao, times(1)).getAllFutureAppointmentsForService(appointmentServiceDefinition);
    }

    @Test
    public void shouldGetAllFutureAppointmentsForTheGivenAppointmentServiceType() {
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setUuid("typeUuid");
        when(appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType)).thenReturn(new ArrayList<>());

        appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType);

        verify(appointmentDao, times(1)).getAllFutureAppointmentsForServiceType(appointmentServiceType);
    }

    @Test
    public void shouldGetAppointmentsForAService() throws ParseException {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("uuid");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = simpleDateFormat.parse("2017-08-08");
        Date endDate = simpleDateFormat.parse("2017-08-09");

        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment = new Appointment();
        appointment.setId(2);
        appointment.setUuid("someUuid");

        when(appointmentDao.getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, null)).thenReturn(appointments);

        appointmentsService.getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, null);
        verify(appointmentDao, times(1)).getAppointmentsForService(appointmentServiceDefinition, startDate, endDate, null);
    }

    @Test
    public void shouldSearchForAnAppointment() {
        Appointment appointment = new Appointment();
        appointment.setUuid("Uuid");
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        when(appointmentDao.search(appointment)).thenReturn(appointmentList);
        appointmentsService.search(appointment);
        verify(appointmentDao, times(1)).search(appointment);
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnAppointmentSave() {
        String errorMessage = "Appointment cannot be created without Patient";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper)
                .validate(any(Appointment.class), anyListOf(AppointmentValidator.class));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);
        appointmentsService.validateAndSave(new Appointment());
        verify(appointmentDao, never()).save(any(Appointment.class));
    }

    @Test
    public void shouldRunDefaultValidatorOnStatusChange() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        appointmentsService.changeStatus(appointment, "CheckedIn", null);
        verify(appointmentServiceHelper, times(1))
                .validateStatusChangeAndGetErrors(any(Appointment.class),
                        any(AppointmentStatus.class),
                        anyListOf(AppointmentStatusChangeValidator.class));
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnStatusChange() {
        String errorMessage = "Appointment status cannot be changed from Completed to Missed";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper)
                .validateStatusChangeAndGetErrors(any(Appointment.class),
                        any(AppointmentStatus.class),
                        anyListOf(AppointmentStatusChangeValidator.class));
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);
        appointmentsService.changeStatus(appointment, "Missed", null);
        verify(appointmentAuditDao, times(1)).save(any(AppointmentAudit.class));
    }

    @Test
    public void shouldCreateAuditEventOnStatusChangeWithOutDate() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setNotes(null);
        appointmentAudit.setAppointment(appointment);
        appointmentAudit.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, null)).thenReturn(appointmentAudit);

        appointmentsService.changeStatus(appointment, "CheckedIn", null);

        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, null);
        verify(appointmentAuditDao, times(1)).save(appointmentAudit);
    }

    @Test
    public void shouldCreateAuditEventOnStatusChangeWithDateAsNotes() throws ParseException {
        Date onDate = DateUtil.convertToDate("2108-08-15T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        AppointmentAudit appointmentAudit = mock(AppointmentAudit.class);
        String notes = onDate.toInstant().toString();
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, notes)).thenReturn(appointmentAudit);

        appointmentsService.changeStatus(appointment, "CheckedIn", onDate);

        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        verify(appointmentAuditDao, times(1)).save(appointmentAudit);
    }

    @Test
    public void shouldCallAppointmentDaoOnce() {
        appointmentsService.getAllAppointmentsInDateRange(null, null);
        verify(appointmentDao, times(1)).getAllAppointmentsInDateRange(null, null);
    }

    @Test
    public void shouldGetAllNonVoidedAppointmentsForAGivenDateRange() throws ParseException {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        AppointmentServiceDefinition appointmentServiceDefinition1 = new AppointmentServiceDefinition();
        appointmentServiceDefinition1.setVoided(true);
        appointment1.setService(appointmentServiceDefinition1);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        appointment1.setStartDateTime(new SimpleDateFormat("yyyy-MM-dd").parse(yesterday.toString()));
        Appointment appointment2 = new Appointment();
        appointment2.setService(new AppointmentServiceDefinition());
        appointment1.setStartDateTime(new Date());
        appointments.add(appointment1);
        appointments.add(appointment2);
        when(appointmentDao.getAllAppointmentsInDateRange(null, null)).thenReturn(appointments);
        List<Appointment> appointmentList = appointmentsService.getAllAppointmentsInDateRange(null, null);
        verify(appointmentDao, times(1)).getAllAppointmentsInDateRange(null, null);
        assertEquals(appointmentList.size(), 1);
    }

    @Test
    public void shouldGetAppointmentByUuid() {
        String appointmentUuid = "appointmentUuid";
        appointmentsService.getAppointmentByUuid(appointmentUuid);
        verify(appointmentDao, times(1)).getAppointmentByUuid(appointmentUuid);
    }

    @Test
    public void shouldUndoStatusChange() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);
        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setAppointment(appointment);
        appointmentAudit.setStatus(AppointmentStatus.CheckedIn);
        appointmentAudit.setNotes("2108-08-15T11:30:00.0Z");
        when(appointmentAuditDao.getPriorStatusChangeEvent(appointment)).thenReturn(appointmentAudit);
        appointmentsService.undoStatusChange(appointment);
        verify(appointmentAuditDao, times(1)).getPriorStatusChangeEvent(appointment);
        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentDao, times(1)).save(captor.capture());
        assertEquals(appointmentAudit.getStatus(), captor.getValue().getStatus());
    }

    @Test
    public void shouldCreateStatusChangeAuditEventOnUndoStatusChange() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);

        AppointmentAudit appointmentAudit = new AppointmentAudit();
        appointmentAudit.setAppointment(appointment);
        appointmentAudit.setStatus(AppointmentStatus.CheckedIn);
        String notes = "2108-08-15T11:30:00.0Z";
        appointmentAudit.setNotes(notes);
        when(appointmentAuditDao.getPriorStatusChangeEvent(appointment)).thenReturn(appointmentAudit);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, appointmentAudit.getNotes()))
                .thenReturn(appointmentAudit);
        appointmentsService.undoStatusChange(appointment);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(appointmentAuditDao, times(1)).save(captor.capture());
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, notes);
        AppointmentAudit savedEvent = captor.getValue();
        assertEquals(appointmentAudit.getNotes(), savedEvent.getNotes());
        assertEquals(appointment.getStatus(), savedEvent.getStatus());
        assertEquals(appointment, savedEvent.getAppointment());
    }

    @Test
    public void shouldThrowExceptionWhenThereIsNoPriorStatusChangeExists() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        when(appointmentAuditDao.getPriorStatusChangeEvent(appointment)).thenReturn(null);
        expectedException.expect(APIException.class);
        expectedException.expectMessage("No status change actions to undo");
        appointmentsService.undoStatusChange(appointment);
        verify(appointmentAuditDao, times(0)).getPriorStatusChangeEvent(appointment);
        verify(appointmentDao, times(0)).save(appointment);
    }

    @Test
    public void shouldThrowExceptionOnAppointmentSaveIfUserHasOnlyOwnPrivilegeAndProviderAndUserIsNotTheSamePerson() {
        setupForOwnPrivilegeAccess(exceptionCode);
        expectedException.expect(APIAuthenticationException.class);
        appointmentsService.validateAndSave(appointment);
    }

    @Test
    public void shouldThrowExceptionOnAppointmentStatusChangeIfUserHasOnlyOwnPrivilegeAndProviderAndUserIsNotTheSamePerson() {
        setupForOwnPrivilegeAccess(exceptionCode);
        expectedException.expect(APIAuthenticationException.class);
        appointmentsService.changeStatus(appointment, AppointmentStatus.Scheduled.name(), null);
    }

    @Test
    public void shouldThrowExceptionOnAppointmentUndoStatusChangeIfUserHasOnlyOwnPrivilegeAndProviderAndUserIsNotTheSamePerson() {
        setupForOwnPrivilegeAccess(exceptionCode);
        expectedException.expect(APIAuthenticationException.class);
        appointmentsService.undoStatusChange(appointment);
    }

    private void setupForOwnPrivilegeAccess(String exceptionCode) {
        String exceptionMessage = "Exception message";
        when(Context.hasPrivilege("manageOwnAppointments")).thenReturn(true);
        when(messageSourceService.getMessage(exceptionCode, null, null)).thenReturn(exceptionMessage);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(user.getPerson()).thenReturn(new Person());
        when(Context.getAuthenticatedUser()).thenReturn(user);
        when(provider.getPerson()).thenReturn(new Person());
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProvider.setResponse(AppointmentProviderResponse.ACCEPTED);
        Set<AppointmentProvider> appointmentProviders = new HashSet<>(asList(appointmentProvider));
        when(appointment.getProviders()).thenReturn(appointmentProviders);
        when(appointment.getProvidersWithResponse(AppointmentProviderResponse.ACCEPTED)).thenReturn(appointmentProviders);
    }

    @Test
    public void shouldReturnAppointmentsByCallingSearchMethodInAppointmentDaoWithAppointmentSearchParameters() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = Date.from(Instant.now());
        Date endDate = Date.from(Instant.now());
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(endDate);
        ArrayList<Appointment> expectedAppointments = new ArrayList<>();
        when(appointmentDao.search(appointmentSearchRequest)).thenReturn(expectedAppointments);

        List<Appointment> actualAppointments = appointmentsService.search(appointmentSearchRequest);

        verify(appointmentDao, times(1)).search(appointmentSearchRequest);
        assertEquals(expectedAppointments, actualAppointments);
    }

    @Test
    public void shouldNotCallSearchMethodInAppointmentDaoAndReturnNullWhenStartDateIsNull() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date endDate = Date.from(Instant.now());
        appointmentSearchRequest.setStartDate(null);
        appointmentSearchRequest.setEndDate(endDate);

        List<Appointment> actualAppointments = appointmentsService.search(appointmentSearchRequest);

        verify(appointmentDao, never()).search(appointmentSearchRequest);
        assertNull(actualAppointments);
    }

    @Test
    public void shouldCallSearchMethodInAppointmentDaoWhenEndDateIsNull() {
        AppointmentSearchRequest appointmentSearchRequest = new AppointmentSearchRequest();
        Date startDate = Date.from(Instant.now());
        appointmentSearchRequest.setStartDate(startDate);
        appointmentSearchRequest.setEndDate(null);

        ArrayList<Appointment> expectedAppointments = new ArrayList<>();
        when(appointmentDao.search(appointmentSearchRequest)).thenReturn(expectedAppointments);
        List<Appointment> actualAppointments = appointmentsService.search(appointmentSearchRequest);

        verify(appointmentDao, times(1)).search(appointmentSearchRequest);
        assertEquals(expectedAppointments, actualAppointments);
    }

    @Test
    public void shouldThrowExceptionWhenUserWithoutResetAppointmentStatusPrivilegeTriesToFromMissedToScheduled() {
        String exceptionMessage = "exception message";
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Missed);
        when(Context.hasPrivilege(RESET_APPOINTMENT_STATUS)).thenReturn(false);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        when(messageSourceService.getMessage(any(), any(), any())).thenReturn(exceptionMessage);

        try {
            expectedException.expect(APIAuthenticationException.class);
            expectedException.expectMessage(exceptionMessage);
            appointmentsService.changeStatus(appointment, "Scheduled", null);
        } finally {
            verify(messageSourceService).getMessage(exceptionCode, new Object[]{RESET_APPOINTMENT_STATUS}, null);
            verifyStatic();
            Context.hasPrivilege(RESET_APPOINTMENT_STATUS);
        }
    }

    @Test
    public void shouldCallSaveMethodFromDaoAndGetAppointmentAuditFromServiceHelper() throws IOException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentAudits(new HashSet<>());
        String anyString = any(String.class);
        when(appointmentServiceHelper.getAppointmentAsJsonString(appointment)).thenReturn(anyString);
        AppointmentAudit appointmentAudit = mock(AppointmentAudit.class);
        when(appointmentServiceHelper.getAppointmentAuditEvent(appointment, anyString)).thenReturn(appointmentAudit);

        Appointment actual = appointmentsService.validateAndSave(appointment);

        verify(appointmentServiceHelper).getAppointmentAsJsonString(appointment);
        verify(appointmentServiceHelper).getAppointmentAuditEvent(appointment, anyString);
        verify(appointmentDao).save(appointment);
        assertEquals(1, actual.getAppointmentAudits().size());
        assertEquals(appointment, actual);
    }

    @Test
    public void shouldThrowExceptionWhenThereIsErrorWhileValidatingBeforeUpdate() throws IOException {
        appointment.setService(null);
        String errorMessage = "Appointment cannot be updated without Service";
        doThrow(new APIException(errorMessage)).when(appointmentServiceHelper).validate(any(Appointment.class), anyListOf(AppointmentValidator.class));
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);

        appointmentsService.validateAndSave(appointment);

        verify(appointmentServiceHelper, never()).getAppointmentAsJsonString(any(Appointment.class));
        verify(appointmentServiceHelper, never()).getAppointmentAuditEvent(any(Appointment.class), any(String.class));
        verify(appointmentDao, never()).save(any(Appointment.class));
    }

    @Test
    public void shouldReturnConflictingAppointmentsForSingleAppointment() {
        Appointment appointment = new Appointment();
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentServiceUnavailabilityConflict.getConflicts(appointments)).thenReturn(Collections.emptyList());
        when(appointmentServiceUnavailabilityConflict.getType()).thenReturn(SERVICE_UNAVAILABLE);
        when(patientDoubleBookingConflict.getConflicts(appointments)).thenReturn(appointments);
        when(patientDoubleBookingConflict.getType()).thenReturn(PATIENT_DOUBLE_BOOKING);

        Map<Enum, List<Appointment>> response = appointmentsService.getAppointmentConflicts(appointment);
        for (AppointmentConflict appointmentConflict : appointmentConflicts) {
            verify(appointmentConflict).getConflicts(appointments);
        }
        verify(appointmentServiceUnavailabilityConflict, never()).getType();
        assertTrue(response.containsKey(PATIENT_DOUBLE_BOOKING));
        assertEquals(1, response.get(PATIENT_DOUBLE_BOOKING).size());
        assertFalse(response.containsKey(SERVICE_UNAVAILABLE));
    }

    @Test
    public void shouldReturnConflictingAppointmentsForMultipleAppointment() {
        Appointment appointmentOne = new Appointment();
        appointmentOne.setStartDateTime(getDate(2017, 10,10,10,10,10));
        Appointment appointmentTwo = new Appointment();
        appointmentTwo.setStartDateTime(DateUtil.getStartOfDay());
        Appointment appointmentThree = new Appointment();
        appointmentThree.setStartDateTime(DateUtil.getStartOfDay());
        Appointment appointmentFour = new Appointment();
        appointmentFour.setStartDateTime(DateUtil.getStartOfDay());
        appointmentFour.setVoided(true);


        List<Appointment> filteredAppointments = asList(appointmentTwo, appointmentThree);
        when(appointmentServiceUnavailabilityConflict.getConflicts(filteredAppointments)).thenReturn(asList(appointmentTwo, appointmentThree));
        when(appointmentServiceUnavailabilityConflict.getType()).thenReturn(SERVICE_UNAVAILABLE);
        when(patientDoubleBookingConflict.getConflicts(filteredAppointments)).thenReturn(asList(mock(Appointment.class)));
        when(patientDoubleBookingConflict.getType()).thenReturn(PATIENT_DOUBLE_BOOKING);

        Map<Enum, List<Appointment>> response = appointmentsService.getAppointmentsConflicts(asList(appointmentOne, appointmentTwo, appointmentThree, appointmentFour));

        for (AppointmentConflict appointmentConflict : appointmentConflicts) {
            verify(appointmentConflict).getConflicts(filteredAppointments);
            verify(appointmentConflict).getType();
        }
        assertTrue(response.containsKey(PATIENT_DOUBLE_BOOKING));
        assertTrue(response.containsKey(SERVICE_UNAVAILABLE));
        assertEquals(2, response.get(SERVICE_UNAVAILABLE).size());
        assertEquals(1, response.get(PATIENT_DOUBLE_BOOKING).size());
    }

    @Test
    public void shouldNeverCallGetConflictsForEmptyList() {
        Appointment appointmentOne = new Appointment();
        appointmentOne.setVoided(true);
        appointmentOne.setStartDateTime(DateUtil.getStartOfDay());
        List<Appointment> appointments = Collections.singletonList(appointmentOne);

        Map<Enum, List<Appointment>> response = appointmentsService.getAppointmentsConflicts(appointments);

        for (AppointmentConflict appointmentConflict : appointmentConflicts) {
            verify(appointmentConflict, never()).getConflicts(any());
        }
        assertEquals(0, response.size());
    }

    @Test
    public void shouldUpdateProviderResponseForGivenAppointment() {
        Person person = new Person();
        when(user.getPerson()).thenReturn(person);
        when(Context.getAuthenticatedUser()).thenReturn(user);

        Provider provider = new Provider();
        provider.setPerson(person);
        provider.setUuid("provider-uuid");
        Appointment appointment = new Appointment();

        AppointmentProvider existingProvider = new AppointmentProvider();
        existingProvider.setResponse(AppointmentProviderResponse.AWAITING);
        existingProvider.setProvider(provider);
        existingProvider.setAppointment(appointment);
        appointment.setProviders(new HashSet<>(asList(existingProvider)));

        AppointmentProvider providerRequest = new AppointmentProvider();
        providerRequest.setProvider(provider);
        providerRequest.setAppointment(appointment);
        providerRequest.setResponse(AppointmentProviderResponse.ACCEPTED);

        appointmentsService.updateAppointmentProviderResponse(providerRequest);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentDao, times(1)).save(appointmentArgumentCaptor.capture());

        Appointment savedAppointment = appointmentArgumentCaptor.getValue();
        assertEquals(AppointmentProviderResponse.ACCEPTED, savedAppointment.getProviders().iterator().next().getResponse());
    }

    @Test
    public void shouldUpdateAppointmentStatusInCaseOfRequestedAppointment() {
        when(Context.hasPrivilege(MANAGE_OWN_APPOINTMENTS)).thenReturn(true);

        Person person = new Person();
        when(user.getPerson()).thenReturn(person);
        when(Context.getAuthenticatedUser()).thenReturn(user);

        Provider provider = new Provider();
        provider.setPerson(person);
        provider.setUuid("provider-uuid");

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Requested);

        AppointmentProvider existingProvider = new AppointmentProvider();
        existingProvider.setResponse(AppointmentProviderResponse.AWAITING);
        existingProvider.setProvider(provider);
        existingProvider.setAppointment(appointment);
        appointment.setProviders(new HashSet<>(asList(existingProvider)));

        AppointmentProvider providerRequest = new AppointmentProvider();
        providerRequest.setProvider(provider);
        providerRequest.setAppointment(appointment);
        providerRequest.setResponse(AppointmentProviderResponse.ACCEPTED);

        appointmentsService.updateAppointmentProviderResponse(providerRequest);

        ArgumentCaptor<Appointment> appointmentArgumentCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentDao, times(1)).save(appointmentArgumentCaptor.capture());

        Appointment savedAppointment = appointmentArgumentCaptor.getValue();
        assertEquals(AppointmentStatus.Scheduled, savedAppointment.getStatus());
        assertEquals(AppointmentProviderResponse.ACCEPTED, savedAppointment.getProviders().iterator().next().getResponse());
    }

    @Test
    public void shouldThrowErrorWhenProviderIsNotPartOfAppointment() {
        expectedException.expect(APIException.class);
        expectedException.expectMessage("Provider is not part of Appointment");

        Provider provider1 = new Provider();
        provider1.setUuid("provider-uuid1");

        Provider provider2 = new Provider();
        provider1.setUuid("provider-uuid2");

        Appointment appointment = new Appointment();
        AppointmentProvider existingProvider = new AppointmentProvider();
        existingProvider.setResponse(AppointmentProviderResponse.AWAITING);
        existingProvider.setProvider(provider1);
        existingProvider.setAppointment(appointment);
        appointment.setProviders(new HashSet<>(asList(existingProvider)));

        AppointmentProvider providerRequest = new AppointmentProvider();
        providerRequest.setProvider(provider2);
        providerRequest.setAppointment(appointment);
        providerRequest.setResponse(AppointmentProviderResponse.ACCEPTED);

        appointmentsService.updateAppointmentProviderResponse(providerRequest);
    }

    @Test
    public void shouldThrowErrorWhenTryingToChangeProviderResponseForOtherProvider() {
        expectedException.expect(APIAuthenticationException.class);
        expectedException.expectMessage("Cannot change Provider Response for other providers");

        when(Context.hasPrivilege(MANAGE_OWN_APPOINTMENTS)).thenReturn(true);
        when(user.getPerson()).thenReturn(new Person());
        when(Context.getAuthenticatedUser()).thenReturn(user);

        Provider provider = new Provider();
        provider.setPerson(new Person());
        provider.setUuid("provider-uuid");

        Appointment appointment = new Appointment();

        AppointmentProvider existingProvider = new AppointmentProvider();
        existingProvider.setResponse(AppointmentProviderResponse.AWAITING);
        existingProvider.setProvider(provider);
        existingProvider.setAppointment(appointment);
        appointment.setProviders(new HashSet<>(asList(existingProvider)));

        AppointmentProvider providerRequest = new AppointmentProvider();
        providerRequest.setProvider(provider);
        providerRequest.setAppointment(appointment);
        providerRequest.setResponse(AppointmentProviderResponse.ACCEPTED);

        appointmentsService.updateAppointmentProviderResponse(providerRequest);
    }
}
