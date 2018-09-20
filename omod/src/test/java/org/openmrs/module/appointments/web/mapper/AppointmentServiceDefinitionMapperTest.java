package org.openmrs.module.appointments.web.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AppointmentServiceDefinitionMapperTest {
    @Mock
    private LocationService locationService;

    @Mock
    private SpecialityService specialityService;

    @Mock
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @InjectMocks
    private AppointmentServiceMapper appointmentServiceMapper;

    private Location location;

    private Speciality speciality;
    private User authenticatedUser;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        authenticatedUser = new User(8);
        PowerMockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    public void shouldGetAppointmentServiceFromPayloadWithoutWeeklyAvailability() throws Exception {
        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceDescription.getName());
        assertEquals(appointmentServiceDefinition.getDurationMins(), appointmentServiceDescription.getDurationMins());
        assertEquals(appointmentServiceDefinition.getStartTime(), appointmentServiceDescription.getStartTime());
        assertEquals(appointmentServiceDefinition.getEndTime(), appointmentServiceDescription.getEndTime());
        assertEquals(appointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceDescription.getMaxAppointmentsLimit());
        assertEquals(0, appointmentServiceDefinition.getWeeklyAvailability().size());
    }

    @Test
    public void shouldGetAppointmentServiceFromPayload() throws Exception {
        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        ServiceWeeklyAvailabilityDescription mondayMorning = createServiceWeeklyAvailabilityDescription(DayOfWeek.MONDAY,Time.valueOf("09:00:00"),Time.valueOf("13:00:00"),20,null, false);
        appointmentServiceDescription.setWeeklyAvailability(Collections.singletonList(mondayMorning));
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceDescription.getName());
        assertEquals(appointmentServiceDefinition.getDurationMins(), appointmentServiceDescription.getDurationMins());
        assertEquals(appointmentServiceDefinition.getStartTime(), appointmentServiceDescription.getStartTime());
        assertEquals(appointmentServiceDefinition.getEndTime(), appointmentServiceDescription.getEndTime());
        assertEquals(appointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceDescription.getMaxAppointmentsLimit());
        assertEquals(location, appointmentServiceDefinition.getLocation());
        assertEquals(speciality, appointmentServiceDefinition.getSpeciality());
        assertNotNull(appointmentServiceDefinition.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(appointmentServiceDefinition.getWeeklyAvailability());
        assertEquals(1, availabilityList.size());
        assertEquals(mondayMorning.getDayOfWeek(), availabilityList.get(0).getDayOfWeek());
        assertEquals(mondayMorning.getStartTime(), availabilityList.get(0).getStartTime());
        assertEquals(mondayMorning.getEndTime(), availabilityList.get(0).getEndTime());
        assertEquals(mondayMorning.getMaxAppointmentsLimit(), availabilityList.get(0).getMaxAppointmentsLimit());
    }

    @Test
    public void shouldGetUpdatedAppointmentServiceFromPayloadIfExisting() throws Exception {
        AppointmentServiceDefinition existingAppointmentServiceDefinition = new AppointmentServiceDefinition();
        existingAppointmentServiceDefinition.setName("Chemotherapy");
        existingAppointmentServiceDefinition.setUuid("Uuid");
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentServiceDefinition);
        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setUuid("Uuid");
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(existingAppointmentServiceDefinition.getUuid(), appointmentServiceDefinition.getUuid());
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceDescription.getName());
        assertEquals(appointmentServiceDefinition.getDurationMins(), appointmentServiceDescription.getDurationMins());
        assertEquals(appointmentServiceDefinition.getStartTime(), appointmentServiceDescription.getStartTime());
        assertEquals(appointmentServiceDefinition.getEndTime(), appointmentServiceDescription.getEndTime());
        assertEquals(appointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceDescription.getMaxAppointmentsLimit());
        assertEquals(0, appointmentServiceDefinition.getWeeklyAvailability().size());
    }

    @Test
    public void shouldGetUpdatedAppointmentServiceFromPayloadIfExistingWithWeeklyAvailability() throws Exception {
        AppointmentServiceDefinition existingAppointmentServiceDefinition = new AppointmentServiceDefinition();
        existingAppointmentServiceDefinition.setName("Chemotherapy");
        existingAppointmentServiceDefinition.setUuid("Uuid");
        ServiceWeeklyAvailability monday = new ServiceWeeklyAvailability();
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        String availabilityUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        monday.setUuid(availabilityUuid);
        HashSet<ServiceWeeklyAvailability> existingAvailabilityList = new HashSet<>();
        existingAvailabilityList.add(monday);
        existingAppointmentServiceDefinition.setWeeklyAvailability(existingAvailabilityList);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentServiceDefinition);

        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setUuid("Uuid");
        List<ServiceWeeklyAvailabilityDescription> weeklyAvailability = new ArrayList<>();
        ServiceWeeklyAvailabilityDescription mondayPayload = createServiceWeeklyAvailabilityDescription(DayOfWeek.MONDAY,Time.valueOf("09:00:00"),Time.valueOf("13:00:00"),20,availabilityUuid, false);
        ServiceWeeklyAvailabilityDescription saturdayPayload = createServiceWeeklyAvailabilityDescription(DayOfWeek.SATURDAY, Time.valueOf("10:00:00"), Time.valueOf("16:00:00"),10, null, false);
        weeklyAvailability.add(mondayPayload);
        weeklyAvailability.add(saturdayPayload);
        appointmentServiceDescription.setWeeklyAvailability(weeklyAvailability);
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);

        AppointmentServiceDefinition updatedAppointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(updatedAppointmentServiceDefinition.getUuid(), existingAppointmentServiceDefinition.getUuid());
        assertEquals(updatedAppointmentServiceDefinition.getName(), appointmentServiceDescription.getName());
        assertEquals(updatedAppointmentServiceDefinition.getDurationMins(), appointmentServiceDescription.getDurationMins());
        assertEquals(updatedAppointmentServiceDefinition.getStartTime(), appointmentServiceDescription.getStartTime());
        assertEquals(updatedAppointmentServiceDefinition.getEndTime(), appointmentServiceDescription.getEndTime());
        assertEquals(updatedAppointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceDescription.getMaxAppointmentsLimit());
        assertEquals(location, updatedAppointmentServiceDefinition.getLocation());
        assertEquals(speciality, updatedAppointmentServiceDefinition.getSpeciality());
        assertNotNull(updatedAppointmentServiceDefinition.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(updatedAppointmentServiceDefinition.getWeeklyAvailability());
        assertEquals(2, availabilityList.size());
    }

    @Test
    public void shouldUpdateExistingAppointmentServiceFromPayloadWithVoidedWeeklyAvailability() throws Exception {
        AppointmentServiceDefinition existingAppointmentServiceDefinition = new AppointmentServiceDefinition();
        existingAppointmentServiceDefinition.setName("Chemotherapy");
        existingAppointmentServiceDefinition.setUuid("Uuid");
        ServiceWeeklyAvailability monday = new ServiceWeeklyAvailability();
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        String availabilityUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        monday.setStartTime(Time.valueOf("09:00:00"));
        monday.setEndTime(Time.valueOf("13:00:00"));
        monday.setMaxAppointmentsLimit(20);
        monday.setUuid(availabilityUuid);
        HashSet<ServiceWeeklyAvailability> existingAvailabilityList = new HashSet<>();
        existingAvailabilityList.add(monday);
        existingAppointmentServiceDefinition.setWeeklyAvailability(existingAvailabilityList);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentServiceDefinition);

        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setUuid("Uuid");
        List<ServiceWeeklyAvailabilityDescription> weeklyAvailability = new ArrayList<>();
        ServiceWeeklyAvailabilityDescription mondayPayload = createServiceWeeklyAvailabilityDescription(DayOfWeek.MONDAY, null, null, 0, availabilityUuid, true);
        weeklyAvailability.add(mondayPayload);
        appointmentServiceDescription.setWeeklyAvailability(weeklyAvailability);
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);

        AppointmentServiceDefinition updatedAppointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(existingAppointmentServiceDefinition.getUuid(), updatedAppointmentServiceDefinition.getUuid());
        assertEquals(updatedAppointmentServiceDefinition.getName(), appointmentServiceDescription.getName());
        assertEquals(updatedAppointmentServiceDefinition.getDurationMins(), appointmentServiceDescription.getDurationMins());
        assertEquals(updatedAppointmentServiceDefinition.getStartTime(), appointmentServiceDescription.getStartTime());
        assertEquals(updatedAppointmentServiceDefinition.getEndTime(), appointmentServiceDescription.getEndTime());
        assertEquals(updatedAppointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceDescription.getMaxAppointmentsLimit());
        assertEquals(location, updatedAppointmentServiceDefinition.getLocation());
        assertEquals(speciality, updatedAppointmentServiceDefinition.getSpeciality());
        assertNotNull(updatedAppointmentServiceDefinition.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(updatedAppointmentServiceDefinition.getWeeklyAvailability());
        assertEquals(0, availabilityList.size());
    }

    @Test
    public void shouldCreateFullResponseFromAnAppointmentService() throws Exception {

        AppointmentServiceDefinition appointmentServiceDefinition = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"), null,
                20, 30);

        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        appointmentServiceDefinition.setWeeklyAvailability(availabilityList);
        AppointmentServiceFullResponse appointmentServiceFullResponse;
        appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDefinition);
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceFullResponse.getName());
        assertEquals(appointmentServiceDefinition.getDurationMins(), appointmentServiceFullResponse.getDurationMins());
        assertEquals(appointmentServiceDefinition.getStartTime().toString(), appointmentServiceFullResponse.getStartTime());
        assertEquals(new String(), appointmentServiceFullResponse.getEndTime());
        assertEquals(appointmentServiceDefinition.getMaxAppointmentsLimit(), appointmentServiceFullResponse.getMaxAppointmentsLimit());
        assertEquals(location.getName(), appointmentServiceFullResponse.getLocation().get("name"));
        assertEquals(speciality.getName(), appointmentServiceFullResponse.getSpeciality().get("name"));
        assertNotNull(appointmentServiceFullResponse.getWeeklyAvailability());
        assertEquals(appointmentServiceFullResponse.getWeeklyAvailability().size(), 1);
    }
    
    @Test
    public void shouldCreateDefaultResponseFromAppointmentServicesList() throws Exception {
        AppointmentServiceDefinition cardiologyService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"),
                null, 20, 30);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        cardiologyService.setWeeklyAvailability(availabilityList);
        
        AppointmentServiceDefinition chemoTherapyService = createAppointmentService("Chemotherapy", Time.valueOf("11:00:00"),
                Time.valueOf("18:30:00"), 30, 10);
        ServiceWeeklyAvailability serviceWeeklyAvailability = new ServiceWeeklyAvailability();
        serviceWeeklyAvailability.setDayOfWeek(DayOfWeek.TUESDAY);
        HashSet<ServiceWeeklyAvailability> availabilities = new HashSet<>();
        availabilities.add(serviceWeeklyAvailability);
        chemoTherapyService.setWeeklyAvailability(availabilities);
        
        List<AppointmentServiceDefinition> appointmentServiceDefinitions = new ArrayList<>();
        appointmentServiceDefinitions.add(cardiologyService);
        appointmentServiceDefinitions.add(chemoTherapyService);

        List<AppointmentServiceDefaultResponse> appointmentServicesResponse = appointmentServiceMapper.constructDefaultResponseForServiceList(appointmentServiceDefinitions);
        assertEquals(cardiologyService.getName(), appointmentServicesResponse.get(0).getName());
        assertEquals(cardiologyService.getDurationMins(), appointmentServicesResponse.get(0).getDurationMins());
        assertEquals(cardiologyService.getStartTime().toString(), appointmentServicesResponse.get(0).getStartTime());
        assertEquals(new String(), appointmentServicesResponse.get(0).getEndTime());
        assertEquals(cardiologyService.getMaxAppointmentsLimit(),
                appointmentServicesResponse.get(0).getMaxAppointmentsLimit());
        assertEquals(location.getName(), appointmentServicesResponse.get(0).getLocation().get("name"));
        assertEquals(speciality.getName(), appointmentServicesResponse.get(0).getSpeciality().get("name"));
        assertEquals(chemoTherapyService.getName(), appointmentServicesResponse.get(1).getName());
        assertEquals(chemoTherapyService.getDurationMins(), appointmentServicesResponse.get(1).getDurationMins());
        assertEquals(chemoTherapyService.getStartTime().toString(), appointmentServicesResponse.get(1).getStartTime());
        assertEquals(chemoTherapyService.getEndTime().toString(), appointmentServicesResponse.get(1).getEndTime());
        assertEquals(chemoTherapyService.getMaxAppointmentsLimit(),
                appointmentServicesResponse.get(1).getMaxAppointmentsLimit());
        assertEquals(location.getName(), appointmentServicesResponse.get(1).getLocation().get("name"));
        assertEquals(speciality.getName(), appointmentServicesResponse.get(1).getSpeciality().get("name"));
    }

    @Test
    public void ShouldMapTheVoidedAppointmentServiceTypes() throws Exception {
        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setName("Service1");
        appointmentServiceDescription.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypeDescription> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceTypeDescription payloadType1 = new AppointmentServiceTypeDescription();
        payloadType1.setName("Type1");
        payloadType1.setDuration(15);
        payloadType1.setVoided(true);
        String voidedReason = "web services call";
        payloadType1.setVoidedReason(voidedReason);
        serviceTypes.add(payloadType1);
        appointmentServiceDescription.setServiceTypes(serviceTypes);

        AppointmentServiceDefinition mappedAppointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(appointmentServiceDescription.getName(), mappedAppointmentServiceDefinition.getName());
        Set<AppointmentServiceType> mappedServiceTypes = mappedAppointmentServiceDefinition.getServiceTypes(true);
        Iterator<AppointmentServiceType> iterator = mappedServiceTypes.iterator();
        AppointmentServiceType mappedServiceType = iterator.next();
        assertEquals(payloadType1.getName(), mappedServiceType.getName());
        assertEquals(payloadType1.getDuration(), mappedServiceType.getDuration());
        assertTrue(mappedServiceType.getVoided());
        assertEquals(voidedReason, mappedServiceType.getVoidReason());
        assertEquals(authenticatedUser, mappedServiceType.getVoidedBy());
        assertNotNull(mappedServiceType.getDateVoided());
        assertEquals(appointmentServiceDescription.getName(), mappedServiceType.getAppointmentServiceDefinition().getName());
    }

    @Test
    public void ShouldMapTheAppointmentServiceTypes() throws Exception {
        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setName("Service1");
        appointmentServiceDescription.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypeDescription> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceTypeDescription payloadType1 = new AppointmentServiceTypeDescription();
        payloadType1.setName("Type1");
        payloadType1.setDuration(15);
        serviceTypes.add(payloadType1);
        appointmentServiceDescription.setServiceTypes(serviceTypes);

        AppointmentServiceDefinition mappedAppointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(appointmentServiceDescription.getName(), mappedAppointmentServiceDefinition.getName());
        Set<AppointmentServiceType> mappedServiceTypes = mappedAppointmentServiceDefinition.getServiceTypes();
        Iterator<AppointmentServiceType> iterator = mappedServiceTypes.iterator();
        AppointmentServiceType type1 = iterator.next();
        assertEquals(payloadType1.getName(), type1.getName());
        assertEquals(payloadType1.getDuration(), type1.getDuration());
        assertEquals(appointmentServiceDescription.getName(), type1.getAppointmentServiceDefinition().getName());
    }

    @Test
    public void ShouldUpdateTheExistingAppointmentServiceWithTheAppointmentServiceTypes() throws Exception {
        AppointmentServiceDefinition existingAppointmentServiceDefinition = new AppointmentServiceDefinition();
        existingAppointmentServiceDefinition.setName("Chemotherapy");
        existingAppointmentServiceDefinition.setUuid("ServiceUuid");
        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceType serviceType1 = new AppointmentServiceType();
        AppointmentServiceType serviceType2 = new AppointmentServiceType();
        AppointmentServiceType serviceType3 = new AppointmentServiceType();
        serviceType1.setName("Type1");
        serviceType1.setUuid("type1Uuid");
        serviceType1.setDuration(10);
        serviceType2.setName("Type2");
        serviceType2.setUuid("type2Uuid");
        serviceType2.setDuration(0);
        serviceType2.setVoided(true);
        serviceType3.setName("Type3");
        serviceType3.setUuid("type3Uuid");
        serviceType3.setDuration(25);
        serviceTypes.add(serviceType1);
        serviceTypes.add(serviceType2);
        serviceTypes.add(serviceType3);
        existingAppointmentServiceDefinition.setServiceTypes(serviceTypes);
        when(appointmentServiceDefinitionService.getAppointmentServiceByUuid("ServiceUuid")).thenReturn(existingAppointmentServiceDefinition);

        AppointmentServiceDescription appointmentServiceDescription = createAppointmentServicePayload();
        appointmentServiceDescription.setName("Chemotherapy");
        appointmentServiceDescription.setUuid("ServiceUuid");
        appointmentServiceDescription.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypeDescription> serviceTypePayloads = new LinkedHashSet<>();
        AppointmentServiceTypeDescription payloadType1 = new AppointmentServiceTypeDescription();
        payloadType1.setName("Type1");
        payloadType1.setUuid("type1Uuid");
        payloadType1.setDuration(30);
        AppointmentServiceTypeDescription payloadType3 = new AppointmentServiceTypeDescription();
        payloadType3.setName("Type3");
        payloadType3.setUuid("type3Uuid");
        payloadType3.setVoided(true);
        AppointmentServiceTypeDescription payloadType4 = new AppointmentServiceTypeDescription();
        payloadType4.setName("Type4");
        payloadType4.setDuration(20);
        serviceTypePayloads.add(payloadType1);
        serviceTypePayloads.add(payloadType3);
        serviceTypePayloads.add(payloadType4);
        appointmentServiceDescription.setServiceTypes(serviceTypePayloads);
        AppointmentServiceDefinition mappedAppointmentServiceDefinition = appointmentServiceMapper.fromDescription(appointmentServiceDescription);
        assertEquals(appointmentServiceDescription.getName(), mappedAppointmentServiceDefinition.getName());
        assertEquals(4, mappedAppointmentServiceDefinition.getServiceTypes(true).size());
        assertEquals(2, mappedAppointmentServiceDefinition.getServiceTypes().size());

        List<AppointmentServiceType> mappedServiceTypes =
                mappedAppointmentServiceDefinition.getServiceTypes(true).stream()
                .sorted(Comparator.comparing(AppointmentServiceType::getName))
                .collect(Collectors.toList());
        assertEquals(payloadType1.getName(), mappedServiceTypes.get(0).getName());
        assertEquals(payloadType1.getDuration(), mappedServiceTypes.get(0).getDuration());
        assertEquals(payloadType1.getUuid(), mappedServiceTypes.get(0).getUuid());
        assertFalse(mappedServiceTypes.get(0).getVoided());

        assertEquals(serviceType2.getName(), mappedServiceTypes.get(1).getName());
        assertEquals(serviceType2.getDuration(), mappedServiceTypes.get(1).getDuration());
        assertEquals(serviceType2.getUuid(), mappedServiceTypes.get(1).getUuid());
        assertEquals(serviceType2.getVoided(), mappedServiceTypes.get(1).getVoided());

        assertEquals(payloadType3.getName(), mappedServiceTypes.get(2).getName());
        assertEquals(payloadType3.getDuration(), mappedServiceTypes.get(2).getDuration());
        assertEquals(payloadType3.getUuid(), mappedServiceTypes.get(2).getUuid());
        assertTrue(mappedServiceTypes.get(2).getVoided());

        assertEquals(payloadType4.getName(), mappedServiceTypes.get(3).getName());
        assertEquals(payloadType4.getDuration(), mappedServiceTypes.get(3).getDuration());
        assertNotNull(mappedServiceTypes.get(3).getUuid());
        assertFalse(mappedServiceTypes.get(3).getVoided());
    }

    @Test
    public void shouldCreateFullResponseWithNonVoidedServiceTypeFromAnAppointmentService() throws Exception {

        AppointmentServiceDefinition appointmentServiceDefinition = createAppointmentService("Cardiology-OPD", null, null,
                null, null);

        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceType serviceType1 = new AppointmentServiceType();
        AppointmentServiceType serviceType2 = new AppointmentServiceType();
        AppointmentServiceType serviceType3 = new AppointmentServiceType();
        serviceType1.setName("Type1");
        serviceType1.setDuration(10);
        serviceType2.setName("Type2");
        serviceType2.setDuration(0);
        serviceType3.setName("Type3");
        serviceType3.setDuration(3);
        serviceType3.setVoided(true);
        serviceTypes.add(serviceType1);
        serviceTypes.add(serviceType2);
        serviceTypes.add(serviceType3);
        appointmentServiceDefinition.setServiceTypes(serviceTypes);

        AppointmentServiceFullResponse appointmentServiceFullResponse;
        appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentServiceDefinition);
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceFullResponse.getName());

        List serviceTypesResponse = appointmentServiceFullResponse.getServiceTypes();
        assertEquals(2, serviceTypesResponse.size());

        HashMap fullResponseType1 = null, fullResponseType2 = null;
        for (Object r : serviceTypesResponse){
            HashMap response = (HashMap)r;
            if (response.get("name").equals(serviceType1.getName())) {
                fullResponseType1 = response;
            }else if (response.get("name").equals(serviceType2.getName())) {
                fullResponseType2 = response;
            }
        }
        assertNotNull(fullResponseType1.get("uuid"));
        assertEquals(serviceType1.getName(), fullResponseType1.get("name"));
        assertEquals(serviceType1.getDuration(), fullResponseType1.get("duration"));
        assertNotNull(fullResponseType2.get("uuid"));
        assertEquals(serviceType2.getName(), fullResponseType2.get("name"));
        assertEquals(serviceType1.getDuration(), fullResponseType1.get("duration"));
    }

    @Test
    public void shouldMapAppointmentServiceToDefaultResponse() {
        AppointmentServiceDefinition appointmentServiceDefinition = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"), null,
                20, 30);
        appointmentServiceDefinition.setUuid("someUuid");
        AppointmentServiceDefaultResponse appointmentServiceDefaultResponse = appointmentServiceMapper.constructDefaultResponse(appointmentServiceDefinition);
        assertEquals(appointmentServiceDefinition.getName(), appointmentServiceDefaultResponse.getName());
        assertEquals(appointmentServiceDefinition.getDurationMins(), appointmentServiceDefaultResponse.getDurationMins());
        assertEquals(appointmentServiceDefinition.getStartTime().toString(), appointmentServiceDefaultResponse.getStartTime());
        assertEquals(appointmentServiceDefinition.getUuid(), appointmentServiceDefaultResponse.getUuid());
    }

    @Test
    public void shouldMapAppointmentServiceToFullResponse() {
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setName("Something");
        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType);
        Speciality speciality = new Speciality();
        speciality.setName("Speciality");
        AppointmentServiceDefinition cardiologyService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"),
                null, 20, 30);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        cardiologyService.setWeeklyAvailability(availabilityList);
        cardiologyService.setServiceTypes(serviceTypes);
        cardiologyService.setSpeciality(speciality);

        List<AppointmentServiceDefinition> appointmentServiceDefinitions = new ArrayList<>();
        appointmentServiceDefinitions.add(cardiologyService);

        List<AppointmentServiceFullResponse> appointmentServiceFullResponses = appointmentServiceMapper.constructFullResponseForServiceList(appointmentServiceDefinitions);

        AppointmentServiceFullResponse cardioService = appointmentServiceFullResponses.get(0);
        assertEquals("Cardiology-OPD", cardiologyService.getName());
        AppointmentServiceType appointmentServiceType1 = cardiologyService.getServiceTypes().iterator().next();
        assertEquals("Something", appointmentServiceType1.getName());
        assertEquals("Speciality", cardioService.getSpeciality().get("name"));
    }

    private AppointmentServiceDefinition createAppointmentService(String name, Time startTime, Time endTime, Integer duration,
                                                                  Integer maxAppointmentsLimit) {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName(name);
        appointmentServiceDefinition.setStartTime(startTime);
        appointmentServiceDefinition.setEndTime(endTime);
        appointmentServiceDefinition.setDurationMins(duration);
        appointmentServiceDefinition.setMaxAppointmentsLimit(maxAppointmentsLimit);

        location = new Location();
        location.setName("Room1");
        appointmentServiceDefinition.setLocation(location);

        speciality = new Speciality();
        speciality.setName("cardiology");
        appointmentServiceDefinition.setSpeciality(speciality);
        
        return appointmentServiceDefinition;
    }

    private AppointmentServiceDescription createAppointmentServicePayload() {
        AppointmentServiceDescription appointmentServiceDescription = new AppointmentServiceDescription();
        appointmentServiceDescription.setName("Cardiology-OPD");
        appointmentServiceDescription.setDurationMins(20);
        appointmentServiceDescription.setStartTime(Time.valueOf("09:00:00"));
        appointmentServiceDescription.setMaxAppointmentsLimit(30);
        location = new Location();
        location.setUuid("locUuid");
        speciality = new Speciality();
        speciality.setUuid("specUuid");
        appointmentServiceDescription.setLocationUuid("locUuid");
        appointmentServiceDescription.setSpecialityUuid("specUuid");
        appointmentServiceDescription.setDescription("OPD ward for cardiology");
        return appointmentServiceDescription;
    }

    private ServiceWeeklyAvailabilityDescription createServiceWeeklyAvailabilityDescription(DayOfWeek dayOfWeek, Time startTime, Time endTime, Integer maxAppointmentsLimit, String uuid, Boolean voided){
        ServiceWeeklyAvailabilityDescription availabilityPayload = new ServiceWeeklyAvailabilityDescription();
        availabilityPayload.setDayOfWeek(dayOfWeek);
        availabilityPayload.setStartTime(startTime);
        availabilityPayload.setEndTime(endTime);
        availabilityPayload.setMaxAppointmentsLimit(maxAppointmentsLimit);
        availabilityPayload.setUuid(uuid);
        availabilityPayload.setVoided(voided);
        return availabilityPayload;
    }

}
