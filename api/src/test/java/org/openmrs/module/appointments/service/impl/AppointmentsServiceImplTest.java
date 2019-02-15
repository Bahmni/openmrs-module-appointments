package org.openmrs.module.appointments.service.impl;

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
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.*;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.validator.AppointmentStatusChangeValidator;
import org.openmrs.module.appointments.validator.AppointmentValidator;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

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

    @InjectMocks
    private AppointmentsServiceImpl appointmentsService;

    private String exceptionCode = "error.privilegesRequired";

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        appointmentValidators.add(appointmentValidator);
        statusChangeValidators.add(statusChangeValidator);
        setValuesForMemberFields(appointmentsService, "appointmentValidators", appointmentValidators);
        setValuesForMemberFields(appointmentsService, "statusChangeValidators", statusChangeValidators);
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
        appointmentsService.validateAndSave(appointment);
        verify(appointmentDao, times(1)).save(appointment);
    }

    @Test
    public void shouldCreateAuditEventOnSaveAppointment() throws ParseException {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        appointment.setPatient(patient);
        AppointmentServiceDefinition service = new AppointmentServiceDefinition();
        appointment.setService(service);
        AppointmentServiceType serviceType = new AppointmentServiceType();
        appointment.setServiceType(serviceType);
        Date startDateTime = DateUtil.convertToDate("2108-08-15T10:00:00.0Z", DateUtil.DateFormatType.UTC);
        Date endDateTime = DateUtil.convertToDate("2108-08-15T10:30:00.0Z", DateUtil.DateFormatType.UTC);
        appointment.setStartDateTime(startDateTime);
        appointment.setEndDateTime(endDateTime);
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointmentsService.validateAndSave(appointment);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(appointmentAuditDao, times(1)).save(captor.capture());
        List<AppointmentAudit> auditEvents = captor.getAllValues();
        assertEquals(appointment.getStatus(), auditEvents.get(0).getStatus());
        assertEquals(appointment, auditEvents.get(0).getAppointment());
        String notes = "{\"serviceTypeUuid\":\"" + serviceType.getUuid() + "\",\"startDateTime\":\"" + startDateTime.toInstant().toString()
                + "\",\"locationUuid\":null,\"appointmentKind\":\"Scheduled\",\"providerUuid\":null,\"endDateTime\":\"" + endDateTime.toInstant().toString()
                + "\",\"serviceUuid\":\"" + service.getUuid() + "\",\"appointmentNotes\":null}";
        assertEquals(notes, auditEvents.get(0).getNotes());
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
    public void shouldRunDefaultAppointmentValidatorsOnSave() {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointment.setService(new AppointmentServiceDefinition());
        appointment.setStartDateTime(new Date());
        appointment.setEndDateTime(new Date());
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointmentsService.validateAndSave(appointment);
        verify(appointmentValidator, times(1)).validate(any(Appointment.class), anyListOf(String.class));
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnAppointmentSave() {
        String errorMessage = "Appointment cannot be created without Patient";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            List<String> errors = (List) args[1];
            errors.add(errorMessage);
            return null;
        }).when(appointmentValidator).validate(any(Appointment.class), anyListOf(String.class));

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
        verify(statusChangeValidator, times(1)).validate(any(Appointment.class), any(AppointmentStatus.class), anyListOf(String.class));
    }

    @Test
    public void shouldThrowExceptionIfValidationFailsOnStatusChange() {
        String errorMessage = "Appointment status cannot be changed from Completed to Missed";
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            List<String> errors = (List) args[2];
            errors.add(errorMessage);
            return null;
        }).when(statusChangeValidator).validate(any(Appointment.class), any(AppointmentStatus.class), anyListOf(String.class));

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Completed);
        expectedException.expect(APIException.class);
        expectedException.expectMessage(errorMessage);
        appointmentsService.changeStatus(appointment, "Missed", null);
        verify(appointmentAuditDao, never()).save(any(AppointmentAudit.class));
    }

    @Test
    public void shouldCreateAuditEventOnStatusChangeWithOutDate() {
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        appointmentsService.changeStatus(appointment, "CheckedIn", null);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(appointmentAuditDao, times(1)).save(captor.capture());
        List<AppointmentAudit> auditEvents = captor.getAllValues();
        assertEquals(appointment.getStatus(), auditEvents.get(0).getStatus());
        assertEquals(appointment, auditEvents.get(0).getAppointment());
        assertEquals(null, auditEvents.get(0).getNotes());
    }

    @Test
    public void shouldCreateAuditEventOnStatusChangeWithDate() throws ParseException {
        Date onDate = DateUtil.convertToDate("2108-08-15T00:00:00.0Z", DateUtil.DateFormatType.UTC);
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.Scheduled);
        appointmentsService.changeStatus(appointment, "CheckedIn", onDate);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(appointmentAuditDao, times(1)).save(captor.capture());
        List<AppointmentAudit> auditEvents = captor.getAllValues();
        assertEquals(appointment.getStatus(), auditEvents.get(0).getStatus());
        assertEquals(appointment, auditEvents.get(0).getAppointment());
        assertEquals(onDate.toInstant().toString(), auditEvents.get(0).getNotes());
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
        appointmentAudit.setNotes("2108-08-15T11:30:00.0Z");
        when(appointmentAuditDao.getPriorStatusChangeEvent(appointment)).thenReturn(appointmentAudit);
        appointmentsService.undoStatusChange(appointment);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(appointmentAuditDao, times(1)).save(captor.capture());
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
        appointmentsService.changeStatus(appointment, null, null);
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
        Set<AppointmentProvider> appointmentProviders = new HashSet<>(Arrays.asList(appointmentProvider));
        when(appointment.getProviders()).thenReturn(appointmentProviders);
        when(appointment.getProvidersWithResponse(AppointmentProviderResponse.ACCEPTED)).thenReturn(appointmentProviders);
    }

}
