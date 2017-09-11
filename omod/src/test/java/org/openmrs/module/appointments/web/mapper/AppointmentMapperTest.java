package org.openmrs.module.appointments.web.mapper;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentPayload;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
public class AppointmentMapperTest {
    
    @Mock
    private PatientService patientService;
    
    @Mock
    private LocationService locationService;
    
    @Mock
    private ProviderService providerService;
    
    @Mock
    private AppointmentServiceService appointmentServiceService;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AppointmentAuditDao auditDao;

    @InjectMocks
    private AppointmentMapper appointmentMapper;

    private Patient patient;
    private AppointmentService service;
    private AppointmentServiceType serviceType;
    private AppointmentServiceType serviceType2;
    private Provider provider;
    private Location location;

    private AppointmentService service2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        patient = new Patient();
        patient.setUuid("patientUuid");
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        service = new AppointmentService();
        service.setUuid("serviceUuid");

        service2 = new AppointmentService();
        service2.setUuid("service2Uuid");

        Speciality speciality = new Speciality();
        speciality.setName("Cardiology");
        speciality.setUuid("specialityUuid");
        service.setSpeciality(speciality);
        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceType = new AppointmentServiceType();
        serviceType2 = new AppointmentServiceType();
        serviceType.setName("Type1");
        serviceType.setUuid("serviceTypeUuid");
        serviceType.setDuration(10);
        serviceType2.setName("Type2");
        serviceType2.setUuid("serviceType2Uuid");
        serviceType2.setDuration(20);
        serviceType2.setVoided(true);
        serviceTypes.add(serviceType);
        serviceTypes.add(serviceType2);
        service.setServiceTypes(serviceTypes);
        when(appointmentServiceService.getAppointmentServiceByUuid("serviceUuid")).thenReturn(service);
        provider = new Provider();
        provider.setUuid("providerUuid");
        when(providerService.getProviderByUuid("providerUuid")).thenReturn(provider);
        location = new Location();
        location.setUuid("locationUuid");
        when(locationService.getLocationByUuid("locationUuid")).thenReturn(location);
    }

    @Test
    public void shouldGetAppointmentFromPayload() throws Exception {
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        assertNotNull(appointment);
        verify(patientService, times(1)).getPatientByUuid(appointmentPayload.getPatientUuid());
        assertEquals(patient, appointment.getPatient());
        verify(appointmentServiceService, times(1)).getAppointmentServiceByUuid(appointmentPayload.getServiceUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());
        verify(providerService, times(1)).getProviderByUuid(appointmentPayload.getProviderUuid());
        assertEquals(provider, appointment.getProvider());
        verify(locationService, times(1)).getLocationByUuid(appointmentPayload.getLocationUuid());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentPayload.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentPayload.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentPayload.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(AppointmentStatus.Scheduled, appointment.getStatus());
        assertEquals(appointmentPayload.getComments(), appointment.getComments());
    }

    @Test
    public void shouldNotSearchForExistingAppointmentWhenPayLoadUuidIsNull() throws Exception {
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentPayload.setUuid(null);
        appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(appointmentsService, never()).getAppointmentByUuid(anyString());
    }

    @Test
    public void shouldGetExistingAppointmentFromPayload() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentPayload.setUuid(appointmentUuid);
        Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(appointmentUuid, appointment.getUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());
        assertEquals(provider, appointment.getProvider());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentPayload.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentPayload.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentPayload.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(appointmentPayload.getComments(), appointment.getComments());
    }

    @Test
    public void shouldGetExistingAppointmentBookedAgainstVoidedServiceTypeFromPayload() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        existingAppointment.setServiceType(serviceType2);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentPayload.setUuid(appointmentUuid);
        appointmentPayload.setServiceTypeUuid(serviceType2.getUuid());
        Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(appointmentUuid, appointment.getUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType2, appointment.getServiceType());
        assertEquals(provider, appointment.getProvider());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentPayload.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentPayload.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentPayload.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(appointmentPayload.getComments(), appointment.getComments());
    }

    @Test
    public void shouldCreateAuditEventOnEditAppointment() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
	    String existingStartDateTime = existingAppointment.getStartDateTime().toInstant().toString();
	    String existingEndDateTime = existingAppointment.getEndDateTime().toInstant().toString();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentPayload appointmentPayload = new AppointmentPayload();
        appointmentPayload.setUuid(appointmentUuid);
        appointmentPayload.setComments("Secondary Consultation");
        appointmentPayload.setServiceUuid("service2Uuid");
        String startDateTime = "2017-03-16T16:57:09.0Z";
        Date startDate = DateUtil.convertToDate(startDateTime, DateUtil.DateFormatType.UTC);
        appointmentPayload.setStartDateTime(startDate);
        String endDateTime = "2017-03-16T17:57:09.0Z";
        Date endDate = DateUtil.convertToDate(endDateTime, DateUtil.DateFormatType.UTC);
        appointmentPayload.setEndDateTime(endDate);
        appointmentPayload.setAppointmentKind("WalkIn");
        appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        ArgumentCaptor<AppointmentAudit> captor = ArgumentCaptor.forClass(AppointmentAudit.class);
        verify(auditDao, times(1)).save(captor.capture());
        List<AppointmentAudit> auditEvents = captor.getAllValues();
        assertEquals(existingAppointment.getStatus(), auditEvents.get(0).getStatus());
        String message = "{\"serviceTypeUuid\":\"serviceTypeUuid\",\"startDateTime\":\"" + existingStartDateTime + "\",\"locationUuid\":\"locationUuid\",\"appointmentKind\":\"Scheduled\",\"providerUuid\":\"providerUuid\",\"endDateTime\":\"" + existingEndDateTime + "\",\"serviceUuid\":\"serviceUuid\",\"appointmentNotes\":\"Initial Consultation\"} to {\"serviceTypeUuid\":null,\"startDateTime\":\"" + startDate.toInstant().toString() + "\",\"locationUuid\":null,\"appointmentKind\":\"WalkIn\",\"providerUuid\":null,\"endDateTime\":\"" + endDate.toInstant().toString() + "\",\"serviceUuid\":\"service2Uuid\",\"appointmentNotes\":\"Secondary Consultation\"}";
        assertEquals(message, auditEvents.get(0).getNotes());
        assertEquals(existingAppointment,auditEvents.get(0).getAppointment());
    }

    @Test
    public void shouldNotCreateAuditEventOnEditAppointmentIfThereIsNoChange() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentPayload.setUuid(appointmentUuid);
        appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(auditDao, never()).save(any(AppointmentAudit.class));
    }

    @Test
    public void shouldNotCreateAuditEventForNewAppointment() throws Exception {
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(auditDao, never()).save(any(AppointmentAudit.class));
    }

    @Test
    public void shouldNotChangePatientOnEditAppointment() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentPayload appointmentPayload = createAppointmentPayload();
        appointmentPayload.setUuid(appointmentUuid);
        appointmentPayload.setPatientUuid("newPatientUuid");
        Appointment appointment = appointmentMapper.getAppointmentFromPayload(appointmentPayload);
        verify(patientService, never()).getPatientByUuid(appointmentPayload.getPatientUuid());
        assertEquals(existingAppointment.getPatient(), appointment.getPatient());
    }
    
    @Test
    public void shouldCreateDefaultResponse() throws Exception {
        Appointment appointment = createAppointment();
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);
        List<AppointmentDefaultResponse> appointmentDefaultResponse = appointmentMapper.constructResponse(appointmentList);
        AppointmentDefaultResponse response = appointmentDefaultResponse.get(0);
        assertEquals(appointment.getUuid(), response.getUuid());
        assertEquals(appointment.getAppointmentNumber(), response.getAppointmentNumber());
        assertEquals(appointment.getPatient().getPersonName().getFullName(), response.getPatient().get("name"));
        assertEquals(appointment.getPatient().getUuid(), response.getPatient().get("uuid"));
        assertEquals(serviceDefaultResponse, response.getService());
        assertEquals(appointment.getServiceType().getName(), response.getServiceType().get("name"));
        assertEquals(appointment.getServiceType().getUuid(), response.getServiceType().get("uuid"));
        assertEquals(appointment.getServiceType().getDuration(), response.getServiceType().get("duration"));
        assertEquals(appointment.getProvider().getName(), response.getProvider().get("name"));
        assertEquals(appointment.getProvider().getUuid(), response.getProvider().get("uuid"));
        assertEquals(appointment.getLocation().getName(), response.getLocation().get("name"));
        assertEquals(appointment.getLocation().getUuid(), response.getLocation().get("uuid"));
        assertEquals(appointment.getStartDateTime(), response.getStartDateTime());
        assertEquals(appointment.getEndDateTime(), response.getEndDateTime());
        assertEquals(appointment.getAppointmentKind(), AppointmentKind.valueOf(response.getAppointmentKind()));
        assertEquals(appointment.getStatus(), AppointmentStatus.valueOf(response.getStatus()));
        assertEquals(appointment.getComments(), response.getComments());
    }
    
    @Test
    public void shouldReturnNullIfNoProviderInDefaultResponse() throws Exception {
        Appointment appointment = createAppointment();
        appointment.getService().setSpeciality(null);
        appointment.setServiceType(null);
        appointment.setProvider(null);
        appointment.setLocation(null);
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);


        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);

        List<AppointmentDefaultResponse> appointmentDefaultResponse = appointmentMapper.constructResponse(appointmentList);
        AppointmentDefaultResponse response = appointmentDefaultResponse.get(0);
        assertEquals(appointment.getUuid(), response.getUuid());
        assertNull(response.getAppointmentNumber());
        assertEquals(appointment.getPatient().getPersonName().getFullName(), response.getPatient().get("name"));
        assertEquals(appointment.getPatient().getUuid(), response.getPatient().get("uuid"));
        assertEquals(serviceDefaultResponse, response.getService());

        assertNull(response.getServiceType());
        assertNull(response.getProvider());
        assertNull(response.getLocation());
        assertEquals(appointment.getStartDateTime(), response.getStartDateTime());
        assertEquals(appointment.getEndDateTime(), response.getEndDateTime());
        assertEquals(appointment.getAppointmentKind(), AppointmentKind.valueOf(response.getAppointmentKind()));
        assertEquals(appointment.getStatus(), AppointmentStatus.valueOf(response.getStatus()));
        assertEquals(appointment.getComments(), response.getComments());
    }
    
    private AppointmentPayload createAppointmentPayload() throws ParseException {
        AppointmentPayload appointmentPayload = new AppointmentPayload();
        appointmentPayload.setPatientUuid("patientUuid");
        appointmentPayload.setServiceUuid("serviceUuid");
        appointmentPayload.setServiceTypeUuid("serviceTypeUuid");
        appointmentPayload.setLocationUuid("locationUuid");
        appointmentPayload.setProviderUuid("providerUuid");
        String startDateTime = "2017-03-15T16:57:09.0Z";
        Date startDate = DateUtil.convertToDate(startDateTime, DateUtil.DateFormatType.UTC);
        appointmentPayload.setStartDateTime(startDate);
        String endDateTime = "2017-03-15T17:57:09.0Z";
        Date endDate = DateUtil.convertToDate(endDateTime, DateUtil.DateFormatType.UTC);
        appointmentPayload.setEndDateTime(endDate);
        appointmentPayload.setAppointmentKind("Scheduled");
        appointmentPayload.setComments("Initial Consultation");
        
        return appointmentPayload;
    }
    
    private Appointment createAppointment() throws ParseException {
        Appointment appointment = new Appointment();
        PersonName name = new PersonName();
        name.setGivenName("test patient");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(name);
        Patient patient = new Patient();
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("GAN230901");
        patient.setNames(personNames);
        patient.setIdentifiers(Collections.singleton(identifier));
        appointment.setUuid("appointmentUuid");
        appointment.setPatient(patient);
        appointment.setService(service);
        appointment.setServiceType(serviceType);
        appointment.setLocation(location);
        appointment.setProvider(provider);
        String startDateTime = "2017-03-15T16:57:09.0Z";
        Date startDate = DateUtil.convertToDate(startDateTime, DateUtil.DateFormatType.UTC);
        appointment.setStartDateTime(startDate);
        String endDateTime = "2017-03-15T17:57:09.0Z";
        Date endDate = DateUtil.convertToDate(endDateTime, DateUtil.DateFormatType.UTC);
        appointment.setEndDateTime(endDate);
        appointment.setAppointmentKind(AppointmentKind.Scheduled);
        appointment.setStatus(AppointmentStatus.Scheduled);
        appointment.setComments("Initial Consultation");
        return appointment;
    }

    @Test
    public void shouldMapAppointmentQueryToAppointment() {
        AppointmentQuery appointmentQuery = new AppointmentQuery();
        appointmentQuery.setServiceUuid("someServiceUuid");
        appointmentQuery.setProviderUuid("providerUuid");
        appointmentQuery.setPatientUuid("patientUuid");
        appointmentQuery.setLocationUuid("locationUuid");
        appointmentQuery.setStatus("Completed");
        Appointment appointment = appointmentMapper.mapQueryToAppointment(appointmentQuery);

        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("serviceUuid");
        when(appointmentServiceService.getAppointmentServiceByUuid("someServiceUuid")).thenReturn(appointmentService);
        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);

        Provider provider = new Provider();
        provider.setUuid("providerUuid");
        when(providerService.getProviderByUuid("providerUuid")).thenReturn(provider);

        Location location = new Location();
        location.setUuid("locationUuid");
        when(locationService.getLocationByUuid("locationUuid")).thenReturn(location);

        assertEquals("locationUuid", appointment.getLocation().getUuid());
        assertEquals("patientUuid", appointment.getPatient().getUuid());
        assertEquals("providerUuid", appointment.getProvider().getUuid());
        assertEquals("Completed", appointment.getStatus().toString());
    }
}
