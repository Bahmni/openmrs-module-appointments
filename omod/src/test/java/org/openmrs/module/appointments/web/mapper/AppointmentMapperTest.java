package org.openmrs.module.appointments.web.mapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentPriority;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentRecurringPattern;
import org.openmrs.module.appointments.model.AppointmentReason;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentProviderDetail;
import org.openmrs.module.appointments.web.contract.AppointmentQuery;
import org.openmrs.module.appointments.web.contract.AppointmentRequest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.extension.AppointmentResponseExtension;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)
public class AppointmentMapperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private PatientService patientService;

    @Mock
    private LocationService locationService;

    @Mock
    private ProviderService providerService;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Mock
    private AppointmentServiceMapper appointmentServiceMapper;

    @Mock
    private AppointmentsService appointmentsService;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private AppointmentResponseExtension extension;

    @Mock
    private ConceptService conceptService;

    @InjectMocks
    private AppointmentMapper appointmentMapper;

    private Patient patient;
    private AppointmentServiceDefinition service;
    private AppointmentServiceType serviceType;
    private AppointmentServiceType serviceType2;
    private Provider provider;
    private Location location;
    private Concept reasonConcept1;
    private Concept reasonConcept2;

    private static final String PERSON_ATTRIBUTE_TYPE_GLOBAL_PROPERTY = "appointments.customPersonAttributeTypes";

    private static final String PERSON_ATTRIBUTE_TYPE_GLOBAL_PROPERTY_VALUES = "attributeA,attributeB,attributeC,attributeD";


    private AppointmentServiceDefinition service2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        patient = new Patient();
        patient.setUuid("patientUuid");
        PersonName name = new PersonName();
        name.setGivenName("test patient");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(name);
        patient.setNames(personNames);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("GAN230901");
        patient.setIdentifiers(new HashSet<>(Arrays.asList(identifier)));
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        service = new AppointmentServiceDefinition();
        service.setUuid("serviceUuid");

        service2 = new AppointmentServiceDefinition();
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
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("serviceUuid")).thenReturn(service);
        provider = new Provider();
        provider.setUuid("providerUuid");
        when(providerService.getProviderByUuid("providerUuid")).thenReturn(provider);
        location = new Location();
        location.setUuid("locationUuid");
        when(locationService.getLocationByUuid("locationUuid")).thenReturn(location);

        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(PERSON_ATTRIBUTE_TYPE_GLOBAL_PROPERTY)).thenReturn(PERSON_ATTRIBUTE_TYPE_GLOBAL_PROPERTY_VALUES);
        when(Context.getLocale()).thenReturn(java.util.Locale.ENGLISH);

        // Setup test concepts for appointment reasons
        reasonConcept1 = new Concept();
        reasonConcept1.setUuid("concept1Uuid");
        ConceptName conceptName1 = new ConceptName("Follow-up", java.util.Locale.ENGLISH);
        reasonConcept1.setFullySpecifiedName(conceptName1);
        reasonConcept1.addName(conceptName1);

        reasonConcept2 = new Concept();
        reasonConcept2.setUuid("concept2Uuid");
        ConceptName conceptName2 = new ConceptName("Lab Results", java.util.Locale.ENGLISH);
        reasonConcept2.setFullySpecifiedName(conceptName2);
        reasonConcept2.addName(conceptName2);

        // Mock conceptService bean (injected via @Autowired)
        when(conceptService.getConceptByUuid("concept1Uuid")).thenReturn(reasonConcept1);
        when(conceptService.getConceptByUuid("concept2Uuid")).thenReturn(reasonConcept2);
    }

    @Test
    public void shouldGetAppointmentFromPayload() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        assertNotNull(appointment);
        verify(patientService, times(1)).getPatientByUuid(appointmentRequest.getPatientUuid());
        assertEquals(patient, appointment.getPatient());
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(appointmentRequest.getServiceUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());
        verify(providerService, times(1)).getProviderByUuid(appointmentRequest.getProviderUuid());
        assertEquals(provider, appointment.getProviders().iterator().next().getProvider());
        verify(locationService, times(1)).getLocationByUuid(appointmentRequest.getLocationUuid());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentRequest.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentRequest.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentRequest.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(AppointmentStatus.Scheduled, appointment.getStatus());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());
    }

    @Test
    public void shouldNotSearchForExistingAppointmentWhenPayLoadUuidIsNull() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(null);
        appointmentMapper.fromRequest(appointmentRequest);
        verify(appointmentsService, never()).getAppointmentByUuid(anyString());
    }

    @Test
    public void shouldGetExistingAppointmentFromPayload() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(appointmentUuid, appointment.getUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());
        assertEquals(provider, appointment.getProviders().iterator().next().getProvider());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentRequest.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentRequest.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentRequest.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());
        assertNull(appointment.getPriority());
    }

    @Test
    public void shouldCreateAppointmentWithStatusFromPayload() throws ParseException {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setStatus("CheckedIn");

        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        existingAppointment.setStatus(AppointmentStatus.CheckedIn);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        assertNotNull(appointment);
        assertEquals(AppointmentStatus.CheckedIn, appointment.getStatus());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());

    }

    @Test
    public void shouldMapExistingAppointmentWhenPayloadHasAppointmentStatus() throws ParseException {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setStatus("Requested");
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        assertNotNull(appointment);
        assertEquals(AppointmentStatus.Requested, appointment.getStatus());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());

    }

    @Test
    public void shouldMapExistingAppointmentWhenPayloadHasAppointmentPriority() throws ParseException {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setPriority("Routine");
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        assertNotNull(appointment);
        assertEquals(AppointmentPriority.Routine, appointment.getPriority());
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionWhenPayloadHasInvalidAppointmentPriority() throws ParseException {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setPriority("abcd");
        appointmentMapper.fromRequest(appointmentRequest);
    }

    @Test
    public void shouldGetExistingAppointmentBookedAgainstVoidedServiceTypeFromPayload() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        existingAppointment.setServiceType(serviceType2);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setServiceTypeUuid(serviceType2.getUuid());
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(appointmentUuid, appointment.getUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType2, appointment.getServiceType());
        assertEquals(provider, appointment.getProviders().iterator().next().getProvider());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentRequest.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentRequest.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentRequest.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());
    }

    @Test
    public void shouldSetServiceTypeAsNullWhenServiceTypeIsNotThereInPayload() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        existingAppointment.setServiceType(serviceType2);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setServiceTypeUuid(null);
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        verify(appointmentsService, times(1)).getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals(existingAppointment.getUuid(), appointment.getUuid());
        assertNull(appointment.getServiceType());
    }

    @Test
    public void shouldNotChangePatientOnEditAppointment() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setPatientUuid("newPatientUuid");
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);
        verify(patientService, never()).getPatientByUuid(appointmentRequest.getPatientUuid());
        assertEquals(existingAppointment.getPatient(), appointment.getPatient());
    }

    @Test
    public void shouldCreateDefaultResponse() throws Exception {
        Appointment appointment = createAppointment();
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        Map additionalInfo = new HashMap();
        additionalInfo.put("Program Name", "Tuberculosis");
        additionalInfo.put("Last Visit", "02/09/2016");

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);
        when(extension.run(appointment)).thenReturn(additionalInfo);
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
        assertEquals(appointment.getProviders().iterator().next().getProvider().getName(), response.getProviders().iterator().next().getName());
        assertEquals(appointment.getProviders().iterator().next().getProvider().getUuid(), response.getProviders().iterator().next().getUuid());
        assertEquals(appointment.getLocation().getName(), response.getLocation().get("name"));
        assertEquals(appointment.getLocation().getUuid(), response.getLocation().get("uuid"));
        assertEquals(appointment.getStartDateTime(), response.getStartDateTime());
        assertEquals(appointment.getEndDateTime(), response.getEndDateTime());
        assertEquals(appointment.getAppointmentKind(), AppointmentKind.valueOf(response.getAppointmentKind()));
        assertEquals(appointment.getStatus(), AppointmentStatus.valueOf(response.getStatus()));
        assertEquals(appointment.getComments(), response.getComments());
        assertEquals(appointment.getVoided(), response.getVoided());
        verify(extension, times(1)).run(appointment);
        assertEquals(2, response.getAdditionalInfo().keySet().size());
        assertEquals(false, response.getExtensions().get("patientEmailDefined"));
    }


    @Test
    public void shouldCreateDefaultResponseForSingleAppointment() throws Exception {
        Appointment appointment = createAppointment();
        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);
        AppointmentDefaultResponse response = appointmentMapper.constructResponse(appointment);
        assertEquals(appointment.getUuid(), response.getUuid());
        assertEquals(appointment.getAppointmentNumber(), response.getAppointmentNumber());
        assertEquals(appointment.getPatient().getPersonName().getFullName(), response.getPatient().get("name"));
        assertEquals(appointment.getPatient().getUuid(), response.getPatient().get("uuid"));
        assertEquals(serviceDefaultResponse, response.getService());
        assertEquals(appointment.getServiceType().getName(), response.getServiceType().get("name"));
        assertEquals(appointment.getServiceType().getUuid(), response.getServiceType().get("uuid"));
        assertEquals(appointment.getServiceType().getDuration(), response.getServiceType().get("duration"));
        assertEquals(appointment.getProviders().iterator().next().getProvider().getName(), response.getProviders().iterator().next().getName());
        assertEquals(appointment.getProviders().iterator().next().getProvider().getUuid(), response.getProviders().iterator().next().getUuid());
        assertEquals(appointment.getLocation().getName(), response.getLocation().get("name"));
        assertEquals(appointment.getLocation().getUuid(), response.getLocation().get("uuid"));
        assertEquals(appointment.getStartDateTime(), response.getStartDateTime());
        assertEquals(appointment.getEndDateTime(), response.getEndDateTime());
        assertEquals(appointment.getAppointmentKind(), AppointmentKind.valueOf(response.getAppointmentKind()));
        assertEquals(appointment.getStatus(), AppointmentStatus.valueOf(response.getStatus()));
        assertEquals(appointment.getComments(), response.getComments());
        assertEquals(appointment.getVoided(), response.getVoided());
    }

    @Test
    public void shouldCreateDefaultResponseWhenNoExtension() throws Exception {
        Appointment appointment = createAppointment();
        appointmentMapper.appointmentResponseExtension = null;
        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);
        AppointmentDefaultResponse response = appointmentMapper.constructResponse(appointment);
        assertEquals(appointment.getUuid(), response.getUuid());
        assertNull(response.getAdditionalInfo());
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
        assertEquals(appointment.getVoided(), response.getVoided());
    }

    private AppointmentRequest createAppointmentRequest() throws ParseException {
        AppointmentRequest appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatientUuid("patientUuid");
        appointmentRequest.setServiceUuid("serviceUuid");
        appointmentRequest.setServiceTypeUuid("serviceTypeUuid");
        appointmentRequest.setLocationUuid("locationUuid");
        appointmentRequest.setProviderUuid("providerUuid");
        String startDateTime = "2017-03-15T16:57:09.0Z";
        Date startDate = DateUtil.convertToDate(startDateTime, DateUtil.DateFormatType.UTC);
        appointmentRequest.setStartDateTime(startDate);
        String endDateTime = "2017-03-15T17:57:09.0Z";
        Date endDate = DateUtil.convertToDate(endDateTime, DateUtil.DateFormatType.UTC);
        appointmentRequest.setEndDateTime(endDate);
        appointmentRequest.setAppointmentKind("Scheduled");
        appointmentRequest.setComments("Initial Consultation");

        List<AppointmentProviderDetail> providerDetails = new ArrayList<>();
        AppointmentProviderDetail providerDetail = new AppointmentProviderDetail();
        providerDetail.setUuid("providerUuid");
        providerDetail.setResponse("ACCEPTED");
        providerDetails.add(providerDetail);
        appointmentRequest.setProviders(providerDetails);

        return appointmentRequest;
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

        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProvider.setResponse(AppointmentProviderResponse.ACCEPTED);

        Set<AppointmentProvider> appProviders = new HashSet<>();
        appProviders.add(appointmentProvider);
        appointment.setProviders(appProviders);

        appointment.getProviders().add(appointmentProvider);

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
        appointmentQuery.setAppointmentKind("Scheduled");
        appointmentQuery.setServiceTypeUuid("serviceTypeUuid");
        Appointment appointment = appointmentMapper.mapQueryToAppointment(appointmentQuery);

        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setUuid("serviceUuid");
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("someServiceUuid")).thenReturn(appointmentServiceDefinition);
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

    @Test
    public void shouldMapAppointmentProvider() {
        AppointmentProviderDetail providerDetail = new AppointmentProviderDetail();
        providerDetail.setResponse("ACCEPTED");
        providerDetail.setUuid("providerUuid");

        Provider provider = new Provider();
        provider.setUuid("providerUuid");
        when(providerService.getProviderByUuid("providerUuid")).thenReturn(provider);

        AppointmentProvider appointmentProvider = appointmentMapper.mapAppointmentProvider(providerDetail);
        assertEquals(AppointmentProviderResponse.ACCEPTED, appointmentProvider.getResponse());
    }

    @Test
    public void shouldMapResponses() {
        assertEquals(AppointmentProviderResponse.ACCEPTED, appointmentMapper.mapProviderResponse("ACCEPTED"));
        assertEquals(AppointmentProviderResponse.AWAITING, appointmentMapper.mapProviderResponse("AWAITING"));
        assertEquals(AppointmentProviderResponse.CANCELLED, appointmentMapper.mapProviderResponse("CANCELLED"));
        assertEquals(AppointmentProviderResponse.REJECTED, appointmentMapper.mapProviderResponse("REJECTED"));
        assertEquals(AppointmentProviderResponse.TENTATIVE, appointmentMapper.mapProviderResponse("TENTATIVE"));
    }


    @Test
    public void shouldMapProviderDetailsForAppointment() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        assertNotNull(appointment);
        verify(patientService, times(1)).getPatientByUuid(appointmentRequest.getPatientUuid());
        assertEquals(this.patient, appointment.getPatient());
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(appointmentRequest.getServiceUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());

        AppointmentProvider appointmentProvider = appointment.getProviders().iterator().next();
        assertEquals(provider.getUuid(), appointmentProvider.getProvider().getUuid());
        assertEquals(AppointmentProviderResponse.ACCEPTED, appointmentProvider.getResponse());
    }

    @Test
    public void shouldCancelProvidersOfExistingAppointment() throws Exception {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        existingAppointment.setServiceType(serviceType2);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);

        AppointmentRequest appointmentRequest = createAppointmentRequest();
        //removing existing providers
        appointmentRequest.setProviders(Collections.EMPTY_LIST);
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setServiceTypeUuid(serviceType2.getUuid());

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        AppointmentProvider appointmentProvider = appointment.getProviders().iterator().next();
        assertEquals(provider.getUuid(), appointmentProvider.getProvider().getUuid());
        assertEquals(AppointmentProviderResponse.CANCELLED, appointmentProvider.getResponse());

    }

    @Test
    public void shouldReturnEmptyListWhenProvidersAreNullForAnAppointment() throws ParseException {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment appointment = createAppointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);
        appointment.setProvider(null);
        appointment.setProviders(null);

        AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);

        assertEquals(Collections.EMPTY_LIST, appointmentDefaultResponse.getProviders());
    }

    @Test
    public void shouldReturnEmptyListWhenProvidersListIsEmptyForAnAppointment() throws ParseException {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment appointment = createAppointment();
        appointment.setUuid(appointmentUuid);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(appointment);
        appointment.setProvider(null);
        appointment.setProviders(Collections.EMPTY_SET);

        AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);

        assertEquals(Collections.EMPTY_LIST, appointmentDefaultResponse.getProviders());
    }



    @Test
    public void shouldChangeTheResponseAndVoidedDataWhenProviderIsVoidedAndAddedAgain() throws ParseException {
        String appointmentUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);
        AppointmentProvider appointmentProvider = new AppointmentProvider();
        appointmentProvider.setProvider(provider);
        appointmentProvider.setResponse(AppointmentProviderResponse.ACCEPTED);
        Set<AppointmentProvider> appProviders = new HashSet<>();
        appProviders.add(appointmentProvider);
        existingAppointment.setProviders(appProviders);
        appointmentProvider.setVoided(true);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        assertEquals(((AppointmentProvider)appointment.getProviders().toArray()[0]).getVoided(), false);
        assertEquals(((AppointmentProvider)appointment.getProviders().toArray()[0]).getResponse(),
                AppointmentProviderResponse.ACCEPTED);
        assertEquals(((AppointmentProvider)appointment.getProviders().toArray()[0]).getVoidReason(), null);
    }

    @Test
    public void shouldSetIsRecurringToTrueInResponseIfAppointmentHasRecurringPattern() throws ParseException {
        Appointment appointment = createAppointment();
        appointment.setAppointmentRecurringPattern(new AppointmentRecurringPattern());

        AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);

        assertTrue(appointmentDefaultResponse.getRecurring());
    }

    @Test
    public void shouldSetIsRecurringToFalseInResponseIfAppointmentDoesNotHaveRecurringPattern() throws ParseException {
        Appointment appointment = createAppointment();

        AppointmentDefaultResponse appointmentDefaultResponse = appointmentMapper.constructResponse(appointment);

        assertFalse(appointmentDefaultResponse.getRecurring());
    }

    @Test
    public void shouldReturnClonedAppointmentFromRequest() throws ParseException {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        Appointment appointment = appointmentMapper.fromRequestClonedAppointment(appointmentRequest);
        assertNotNull(appointment);
        verify(patientService, times(1)).getPatientByUuid(appointmentRequest.getPatientUuid());
        assertEquals(patient, appointment.getPatient());
        verify(appointmentServiceDefinitionService, times(1)).getAppointmentServiceByUuid(appointmentRequest.getServiceUuid());
        assertEquals(service, appointment.getService());
        assertEquals(serviceType, appointment.getServiceType());
        verify(providerService, times(1)).getProviderByUuid(appointmentRequest.getProviderUuid());
        assertEquals(provider, appointment.getProviders().iterator().next().getProvider());
        verify(locationService, times(1)).getLocationByUuid(appointmentRequest.getLocationUuid());
        assertEquals(location, appointment.getLocation());
        assertEquals(appointmentRequest.getStartDateTime(), appointment.getStartDateTime());
        assertEquals(appointmentRequest.getEndDateTime(), appointment.getEndDateTime());
        assertEquals(AppointmentKind.valueOf(appointmentRequest.getAppointmentKind()), appointment.getAppointmentKind());
        assertEquals(AppointmentStatus.Scheduled, appointment.getStatus());
        assertEquals(appointmentRequest.getComments(), appointment.getComments());
    }

    @Test
    public void shouldNotFailIfPatientHasMultipleIdentifiersOfTheSameIdentifierType() throws Exception {
        Appointment appointment = createAppointment();

        Set<PatientIdentifier> identifiers = new HashSet<>();
        PatientIdentifierType type = new PatientIdentifierType();
        type.setId(1);
        type.setName("Basic Identifier Type");
        PatientIdentifier identifier1 = new PatientIdentifier();
        identifier1.setIdentifierType(type);
        identifier1.setIdentifier("identifier1");
        identifiers.add(identifier1);

        PatientIdentifier identifier2 = new PatientIdentifier();
        identifier2.setIdentifierType(type);
        identifier2.setIdentifier("identifier2");
        identifiers.add(identifier2);

        PatientIdentifier identifier3 = new PatientIdentifier();
        identifier3.setIdentifierType(type);
        identifier3.setIdentifier("identifier3");
        identifiers.add(identifier3);

        appointment.getPatient().setIdentifiers(identifiers);

        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);

        List<AppointmentDefaultResponse> appointmentDefaultResponse = appointmentMapper.constructResponse(appointmentList);
        AppointmentDefaultResponse response = appointmentDefaultResponse.get(0);
        assertEquals(appointment.getUuid(), response.getUuid());

        List<String> basicIdentifiers = Arrays.asList(response.getPatient().get("BasicIdentifierType").toString().split(","));
        assertTrue(basicIdentifiers.contains("identifier1"));
        assertTrue(basicIdentifiers.contains("identifier2"));
        assertTrue(basicIdentifiers.contains("identifier3"));
    }

    @Test
    public void shouldMapAppointmentReasonsFromRequest() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        List<String> reasonUuids = Arrays.asList("concept1Uuid", "concept2Uuid");
        appointmentRequest.setReasonConceptUuids(reasonUuids);

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        assertNotNull(appointment.getReasons());
        assertEquals(2, appointment.getReasons().size());
        assertTrue(appointment.getReasons().stream().anyMatch(r -> r.getConcept().getUuid().equals("concept1Uuid")));
        assertTrue(appointment.getReasons().stream().anyMatch(r -> r.getConcept().getUuid().equals("concept2Uuid")));
        verify(conceptService, times(1)).getConceptByUuid("concept1Uuid");
        verify(conceptService, times(1)).getConceptByUuid("concept2Uuid");
    }

    @Test
    public void shouldHandleNullReasonsInRequest() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setReasonConceptUuids(null);

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        assertNotNull(appointment);
        verify(conceptService, never()).getConceptByUuid(anyString());
    }

    @Test
    public void shouldHandleEmptyReasonsInRequest() throws Exception {
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setReasonConceptUuids(new ArrayList<>());

        Appointment appointment = appointmentMapper.fromRequest(appointmentRequest);

        assertNotNull(appointment);
        verify(conceptService, never()).getConceptByUuid(anyString());
    }

    @Test
    public void shouldIncludeReasonsInResponse() throws Exception {
        Appointment appointment = createAppointment();

        // Add reasons to appointment
        Set<AppointmentReason> reasons = new HashSet<>();
        AppointmentReason reason1 = new AppointmentReason();
        reason1.setConcept(reasonConcept1);
        reason1.setVoided(false);
        reasons.add(reason1);

        AppointmentReason reason2 = new AppointmentReason();
        reason2.setConcept(reasonConcept2);
        reason2.setVoided(false);
        reasons.add(reason2);

        appointment.setReasons(reasons);

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);

        AppointmentDefaultResponse response = appointmentMapper.constructResponse(appointment);

        assertNotNull(response.getReasons());
        assertEquals(2, response.getReasons().size());
        assertTrue(response.getReasons().stream().anyMatch(r -> r.getConceptUuid().equals("concept1Uuid")));
        assertTrue(response.getReasons().stream().anyMatch(r -> r.getConceptUuid().equals("concept2Uuid")));
        assertTrue(response.getReasons().stream().anyMatch(r -> r.getName().equals("Follow-up")));
        assertTrue(response.getReasons().stream().anyMatch(r -> r.getName().equals("Lab Results")));
    }

    @Test
    public void shouldReturnEmptyListWhenNoReasonsInResponse() throws Exception {
        Appointment appointment = createAppointment();
        appointment.setReasons(null);

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);

        AppointmentDefaultResponse response = appointmentMapper.constructResponse(appointment);

        assertNotNull(response.getReasons());
        assertEquals(0, response.getReasons().size());
    }

    @Test
    public void shouldFilterOutVoidedReasonsInResponse() throws Exception {
        Appointment appointment = createAppointment();

        Set<AppointmentReason> reasons = new HashSet<>();
        AppointmentReason reason1 = new AppointmentReason();
        reason1.setConcept(reasonConcept1);
        reason1.setVoided(false);
        reasons.add(reason1);

        AppointmentReason reason2 = new AppointmentReason();
        reason2.setConcept(reasonConcept2);
        reason2.setVoided(true); // This should be filtered out
        reasons.add(reason2);

        appointment.setReasons(reasons);

        AppointmentServiceDefaultResponse serviceDefaultResponse = new AppointmentServiceDefaultResponse();
        when(appointmentServiceMapper.constructDefaultResponse(service)).thenReturn(serviceDefaultResponse);

        AppointmentDefaultResponse response = appointmentMapper.constructResponse(appointment);

        assertNotNull(response.getReasons());
        assertEquals(1, response.getReasons().size());
        assertEquals("concept1Uuid", response.getReasons().get(0).getConceptUuid());
        assertEquals("Follow-up", response.getReasons().get(0).getName());
    }

    @Test
    public void shouldVoidRemovedReasonsWhenUpdatingAppointment() throws Exception {
        String appointmentUuid = "appointmentUuid";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);

        // Existing appointment has two reasons
        Set<AppointmentReason> existingReasons = new HashSet<>();
        AppointmentReason reason1 = new AppointmentReason();
        reason1.setConcept(reasonConcept1);
        reason1.setVoided(false);
        existingReasons.add(reason1);

        AppointmentReason reason2 = new AppointmentReason();
        reason2.setConcept(reasonConcept2);
        reason2.setVoided(false);
        existingReasons.add(reason2);

        existingAppointment.setReasons(existingReasons);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);

        // Update request only includes concept1, so concept2 should be voided
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setReasonConceptUuids(Arrays.asList("concept1Uuid"));

        Appointment updatedAppointment = appointmentMapper.fromRequest(appointmentRequest);

        // Should have reason1 active and reason2 voided
        long voidedCount = updatedAppointment.getReasons().stream()
                .filter(r -> r.getConcept().getUuid().equals("concept2Uuid") && r.getVoided())
                .count();
        assertEquals(1, voidedCount);
    }

    @Test
    public void shouldNotAddDuplicateReasonsWhenUpdatingAppointment() throws Exception {
        String appointmentUuid = "appointmentUuid";
        Appointment existingAppointment = createAppointment();
        existingAppointment.setUuid(appointmentUuid);

        // Existing appointment already has reason1
        Set<AppointmentReason> existingReasons = new HashSet<>();
        AppointmentReason reason1 = new AppointmentReason();
        reason1.setConcept(reasonConcept1);
        reason1.setVoided(false);
        existingReasons.add(reason1);

        existingAppointment.setReasons(existingReasons);
        when(appointmentsService.getAppointmentByUuid(appointmentUuid)).thenReturn(existingAppointment);

        // Update request includes concept1 again - should not duplicate
        AppointmentRequest appointmentRequest = createAppointmentRequest();
        appointmentRequest.setUuid(appointmentUuid);
        appointmentRequest.setReasonConceptUuids(Arrays.asList("concept1Uuid"));

        Appointment updatedAppointment = appointmentMapper.fromRequest(appointmentRequest);

        // Should still have only 1 reason
        long concept1Count = updatedAppointment.getReasons().stream()
                .filter(r -> r.getConcept().getUuid().equals("concept1Uuid"))
                .count();
        assertEquals(1, concept1Count);
    }
}