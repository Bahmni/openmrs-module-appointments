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
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.AppointmentServiceService;
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
public class AppointmentServiceMapperTest {
    @Mock
    private LocationService locationService;

    @Mock
    private SpecialityService specialityService;

    @Mock
    private AppointmentServiceService appointmentServiceService;

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
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(appointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(appointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(appointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
        assertEquals(0, appointmentService.getWeeklyAvailability().size());
    }

    @Test
    public void shouldGetAppointmentServiceFromPayload() throws Exception {
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        ServiceWeeklyAvailabilityPayload mondayMorning = createServiceWeeklyAvailabilityPayload(DayOfWeek.MONDAY,Time.valueOf("09:00:00"),Time.valueOf("13:00:00"),20,null, false);
        appointmentServicePayload.setWeeklyAvailability(Collections.singletonList(mondayMorning));
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(appointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(appointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(appointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
        assertEquals(location, appointmentService.getLocation());
        assertEquals(speciality,appointmentService.getSpeciality());
        assertNotNull(appointmentService.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(appointmentService.getWeeklyAvailability());
        assertEquals(1, availabilityList.size());
        assertEquals(mondayMorning.getDayOfWeek(), availabilityList.get(0).getDayOfWeek());
        assertEquals(mondayMorning.getStartTime(), availabilityList.get(0).getStartTime());
        assertEquals(mondayMorning.getEndTime(), availabilityList.get(0).getEndTime());
        assertEquals(mondayMorning.getMaxAppointmentsLimit(), availabilityList.get(0).getMaxAppointmentsLimit());
    }

    @Test
    public void shouldGetUpdatedAppointmentServiceFromPayloadIfExisting() throws Exception {
        AppointmentService existingAppointmentService = new AppointmentService();
        existingAppointmentService.setName("Chemotherapy");
        existingAppointmentService.setUuid("Uuid");
        when(appointmentServiceService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentService);
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setUuid("Uuid");
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(existingAppointmentService.getUuid(), appointmentService.getUuid());
        assertEquals(appointmentService.getName(), appointmentServicePayload.getName());
        assertEquals(appointmentService.getDurationMins(), appointmentServicePayload.getDurationMins());
        assertEquals(appointmentService.getStartTime(), appointmentServicePayload.getStartTime());
        assertEquals(appointmentService.getEndTime(), appointmentServicePayload.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(), appointmentServicePayload.getMaxAppointmentsLimit());
        assertEquals(0, appointmentService.getWeeklyAvailability().size());
    }

    @Test
    public void shouldGetUpdatedAppointmentServiceFromPayloadIfExistingWithWeeklyAvailability() throws Exception {
        AppointmentService existingAppointmentService = new AppointmentService();
        existingAppointmentService.setName("Chemotherapy");
        existingAppointmentService.setUuid("Uuid");
        ServiceWeeklyAvailability monday = new ServiceWeeklyAvailability();
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        String availabilityUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        monday.setUuid(availabilityUuid);
        HashSet<ServiceWeeklyAvailability> existingAvailabilityList = new HashSet<>();
        existingAvailabilityList.add(monday);
        existingAppointmentService.setWeeklyAvailability(existingAvailabilityList);
        when(appointmentServiceService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentService);

        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setUuid("Uuid");
        List<ServiceWeeklyAvailabilityPayload> weeklyAvailability = new ArrayList<>();
        ServiceWeeklyAvailabilityPayload mondayPayload = createServiceWeeklyAvailabilityPayload(DayOfWeek.MONDAY,Time.valueOf("09:00:00"),Time.valueOf("13:00:00"),20,availabilityUuid, false);
        ServiceWeeklyAvailabilityPayload saturdayPayload = createServiceWeeklyAvailabilityPayload(DayOfWeek.SATURDAY, Time.valueOf("10:00:00"), Time.valueOf("16:00:00"),10, null, false);
        weeklyAvailability.add(mondayPayload);
        weeklyAvailability.add(saturdayPayload);
        appointmentServicePayload.setWeeklyAvailability(weeklyAvailability);
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);

        AppointmentService updatedAppointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(updatedAppointmentService.getUuid(), existingAppointmentService.getUuid());
        assertEquals(updatedAppointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(updatedAppointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(updatedAppointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(updatedAppointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(updatedAppointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
        assertEquals(location, updatedAppointmentService.getLocation());
        assertEquals(speciality,updatedAppointmentService.getSpeciality());
        assertNotNull(updatedAppointmentService.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(updatedAppointmentService.getWeeklyAvailability());
        assertEquals(2, availabilityList.size());
    }

    @Test
    public void shouldUpdateExistingAppointmentServiceFromPayloadWithVoidedWeeklyAvailability() throws Exception {
        AppointmentService existingAppointmentService = new AppointmentService();
        existingAppointmentService.setName("Chemotherapy");
        existingAppointmentService.setUuid("Uuid");
        ServiceWeeklyAvailability monday = new ServiceWeeklyAvailability();
        monday.setDayOfWeek(DayOfWeek.MONDAY);
        String availabilityUuid = "7869637c-12fe-4121-9692-b01f93f99e55";
        monday.setStartTime(Time.valueOf("09:00:00"));
        monday.setEndTime(Time.valueOf("13:00:00"));
        monday.setMaxAppointmentsLimit(20);
        monday.setUuid(availabilityUuid);
        HashSet<ServiceWeeklyAvailability> existingAvailabilityList = new HashSet<>();
        existingAvailabilityList.add(monday);
        existingAppointmentService.setWeeklyAvailability(existingAvailabilityList);
        when(appointmentServiceService.getAppointmentServiceByUuid("Uuid")).thenReturn(existingAppointmentService);

        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setUuid("Uuid");
        List<ServiceWeeklyAvailabilityPayload> weeklyAvailability = new ArrayList<>();
        ServiceWeeklyAvailabilityPayload mondayPayload = createServiceWeeklyAvailabilityPayload(DayOfWeek.MONDAY, null, null, 0, availabilityUuid, true);
        weeklyAvailability.add(mondayPayload);
        appointmentServicePayload.setWeeklyAvailability(weeklyAvailability);
        when(locationService.getLocationByUuid("locUuid")).thenReturn(location);
        when(specialityService.getSpecialityByUuid("specUuid")).thenReturn(speciality);

        AppointmentService updatedAppointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(existingAppointmentService.getUuid(), updatedAppointmentService.getUuid());
        assertEquals(updatedAppointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(updatedAppointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(updatedAppointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(updatedAppointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(updatedAppointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
        assertEquals(location, updatedAppointmentService.getLocation());
        assertEquals(speciality,updatedAppointmentService.getSpeciality());
        assertNotNull(updatedAppointmentService.getWeeklyAvailability());
        List<ServiceWeeklyAvailability> availabilityList = new ArrayList<>(updatedAppointmentService.getWeeklyAvailability());
        assertEquals(0, availabilityList.size());
    }

    @Test
    public void shouldCreateFullResponseFromAnAppointmentService() throws Exception {

        AppointmentService appointmentService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"), null,
                20, 30);

        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        appointmentService.setWeeklyAvailability(availabilityList);
        AppointmentServiceFullResponse appointmentServiceFullResponse;
        appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService);
        assertEquals(appointmentService.getName(), appointmentServiceFullResponse.getName());
        assertEquals(appointmentService.getDurationMins(), appointmentServiceFullResponse.getDurationMins());
        assertEquals(appointmentService.getStartTime().toString(), appointmentServiceFullResponse.getStartTime());
        assertEquals(new String(), appointmentServiceFullResponse.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(), appointmentServiceFullResponse.getMaxAppointmentsLimit());
        assertEquals(location.getName(), appointmentServiceFullResponse.getLocation().get("name"));
        assertEquals(speciality.getName(), appointmentServiceFullResponse.getSpeciality().get("name"));
        assertNotNull(appointmentServiceFullResponse.getWeeklyAvailability());
        assertEquals(appointmentServiceFullResponse.getWeeklyAvailability().size(), 1);
    }
    
    @Test
    public void shouldCreateDefaultResponseFromAppointmentServicesList() throws Exception {
        AppointmentService cardiologyService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"),
                null, 20, 30);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        cardiologyService.setWeeklyAvailability(availabilityList);
        
        AppointmentService chemoTherapyService = createAppointmentService("Chemotherapy", Time.valueOf("11:00:00"),
                Time.valueOf("18:30:00"), 30, 10);
        ServiceWeeklyAvailability serviceWeeklyAvailability = new ServiceWeeklyAvailability();
        serviceWeeklyAvailability.setDayOfWeek(DayOfWeek.TUESDAY);
        HashSet<ServiceWeeklyAvailability> availabilities = new HashSet<>();
        availabilities.add(serviceWeeklyAvailability);
        chemoTherapyService.setWeeklyAvailability(availabilities);
        
        List<AppointmentService> appointmentServices = new ArrayList<>();
        appointmentServices.add(cardiologyService);
        appointmentServices.add(chemoTherapyService);

        List<AppointmentServiceDefaultResponse> appointmentServicesResponse = appointmentServiceMapper.constructDefaultResponseForServiceList(appointmentServices);
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
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setName("Service1");
        appointmentServicePayload.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypePayload> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceTypePayload payloadType1 = new AppointmentServiceTypePayload();
        payloadType1.setName("Type1");
        payloadType1.setDuration(15);
        payloadType1.setVoided(true);
        String voidedReason = "web services call";
        payloadType1.setVoidedReason(voidedReason);
        serviceTypes.add(payloadType1);
        appointmentServicePayload.setServiceTypes(serviceTypes);

        AppointmentService mappedAppointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentServicePayload.getName(), mappedAppointmentService.getName());
        Set<AppointmentServiceType> mappedServiceTypes = mappedAppointmentService.getServiceTypes(true);
        Iterator<AppointmentServiceType> iterator = mappedServiceTypes.iterator();
        AppointmentServiceType mappedServiceType = iterator.next();
        assertEquals(payloadType1.getName(), mappedServiceType.getName());
        assertEquals(payloadType1.getDuration(), mappedServiceType.getDuration());
        assertTrue(mappedServiceType.getVoided());
        assertEquals(voidedReason, mappedServiceType.getVoidReason());
        assertEquals(authenticatedUser, mappedServiceType.getVoidedBy());
        assertNotNull(mappedServiceType.getDateVoided());
        assertEquals(appointmentServicePayload.getName(), mappedServiceType.getAppointmentService().getName());
    }

    @Test
    public void ShouldMapTheAppointmentServiceTypes() throws Exception {
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setName("Service1");
        appointmentServicePayload.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypePayload> serviceTypes = new LinkedHashSet<>();
        AppointmentServiceTypePayload payloadType1 = new AppointmentServiceTypePayload();
        payloadType1.setName("Type1");
        payloadType1.setDuration(15);
        serviceTypes.add(payloadType1);
        appointmentServicePayload.setServiceTypes(serviceTypes);

        AppointmentService mappedAppointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentServicePayload.getName(), mappedAppointmentService.getName());
        Set<AppointmentServiceType> mappedServiceTypes = mappedAppointmentService.getServiceTypes();
        Iterator<AppointmentServiceType> iterator = mappedServiceTypes.iterator();
        AppointmentServiceType type1 = iterator.next();
        assertEquals(payloadType1.getName(), type1.getName());
        assertEquals(payloadType1.getDuration(), type1.getDuration());
        assertEquals(appointmentServicePayload.getName(), type1.getAppointmentService().getName());
    }

    @Test
    public void ShouldUpdateTheExistingAppointmentServiceWithTheAppointmentServiceTypes() throws Exception {
        AppointmentService existingAppointmentService = new AppointmentService();
        existingAppointmentService.setName("Chemotherapy");
        existingAppointmentService.setUuid("ServiceUuid");
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
        existingAppointmentService.setServiceTypes(serviceTypes);
        when(appointmentServiceService.getAppointmentServiceByUuid("ServiceUuid")).thenReturn(existingAppointmentService);

        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        appointmentServicePayload.setName("Chemotherapy");
        appointmentServicePayload.setUuid("ServiceUuid");
        appointmentServicePayload.setWeeklyAvailability(new ArrayList<>());
        Set<AppointmentServiceTypePayload> serviceTypePayloads = new LinkedHashSet<>();
        AppointmentServiceTypePayload payloadType1 = new AppointmentServiceTypePayload();
        payloadType1.setName("Type1");
        payloadType1.setUuid("type1Uuid");
        payloadType1.setDuration(30);
        AppointmentServiceTypePayload payloadType3 = new AppointmentServiceTypePayload();
        payloadType3.setName("Type3");
        payloadType3.setUuid("type3Uuid");
        payloadType3.setVoided(true);
        AppointmentServiceTypePayload payloadType4 = new AppointmentServiceTypePayload();
        payloadType4.setName("Type4");
        payloadType4.setDuration(20);
        serviceTypePayloads.add(payloadType1);
        serviceTypePayloads.add(payloadType3);
        serviceTypePayloads.add(payloadType4);
        appointmentServicePayload.setServiceTypes(serviceTypePayloads);
        AppointmentService mappedAppointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentServicePayload.getName(), mappedAppointmentService.getName());
        assertEquals(4, mappedAppointmentService.getServiceTypes(true).size());
        assertEquals(2, mappedAppointmentService.getServiceTypes().size());

        List<AppointmentServiceType> mappedServiceTypes =
                mappedAppointmentService.getServiceTypes(true).stream()
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

        AppointmentService appointmentService = createAppointmentService("Cardiology-OPD", null, null,
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
        appointmentService.setServiceTypes(serviceTypes);

        AppointmentServiceFullResponse appointmentServiceFullResponse;
        appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService);
        assertEquals(appointmentService.getName(), appointmentServiceFullResponse.getName());

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
        AppointmentService appointmentService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"), null,
                20, 30);
        appointmentService.setUuid("someUuid");
        AppointmentServiceDefaultResponse appointmentServiceDefaultResponse = appointmentServiceMapper.constructDefaultResponse(appointmentService);
        assertEquals(appointmentService.getName(), appointmentServiceDefaultResponse.getName());
        assertEquals(appointmentService.getDurationMins(), appointmentServiceDefaultResponse.getDurationMins());
        assertEquals(appointmentService.getStartTime().toString(), appointmentServiceDefaultResponse.getStartTime());
        assertEquals(appointmentService.getUuid(), appointmentServiceDefaultResponse.getUuid());
    }

    @Test
    public void shouldMapAppointmentServiceToFullResponse() {
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setName("Something");
        Set<AppointmentServiceType> serviceTypes = new LinkedHashSet<>();
        serviceTypes.add(appointmentServiceType);
        Speciality speciality = new Speciality();
        speciality.setName("Speciality");
        AppointmentService cardiologyService = createAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"),
                null, 20, 30);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        cardiologyService.setWeeklyAvailability(availabilityList);
        cardiologyService.setServiceTypes(serviceTypes);
        cardiologyService.setSpeciality(speciality);

        List<AppointmentService> appointmentServices = new ArrayList<>();
        appointmentServices.add(cardiologyService);

        List<AppointmentServiceFullResponse> appointmentServiceFullResponses = appointmentServiceMapper.constructFullResponseForServiceList(appointmentServices);

        AppointmentServiceFullResponse cardioService = appointmentServiceFullResponses.get(0);
        assertEquals("Cardiology-OPD", cardiologyService.getName());
        AppointmentServiceType appointmentServiceType1 = cardiologyService.getServiceTypes().iterator().next();
        assertEquals("Something", appointmentServiceType1.getName());
        assertEquals("Speciality", cardioService.getSpeciality().get("name"));
    }

    private AppointmentService createAppointmentService(String name, Time startTime, Time endTime, Integer duration,
                                                        Integer maxAppointmentsLimit) {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName(name);
        appointmentService.setStartTime(startTime);
        appointmentService.setEndTime(endTime);
        appointmentService.setDurationMins(duration);
        appointmentService.setMaxAppointmentsLimit(maxAppointmentsLimit);

        location = new Location();
        location.setName("Room1");
        appointmentService.setLocation(location);

        speciality = new Speciality();
        speciality.setName("cardiology");
        appointmentService.setSpeciality(speciality);
        
        return appointmentService;
    }

    private AppointmentServicePayload createAppointmentServicePayload() {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardiology-OPD");
        appointmentServicePayload.setDurationMins(20);
        appointmentServicePayload.setStartTime(Time.valueOf("09:00:00"));
        appointmentServicePayload.setMaxAppointmentsLimit(30);
        location = new Location();
        location.setUuid("locUuid");
        speciality = new Speciality();
        speciality.setUuid("specUuid");
        appointmentServicePayload.setLocationUuid("locUuid");
        appointmentServicePayload.setSpecialityUuid("specUuid");
        appointmentServicePayload.setDescription("OPD ward for cardiology");
        return appointmentServicePayload;
    }

    private ServiceWeeklyAvailabilityPayload createServiceWeeklyAvailabilityPayload(DayOfWeek dayOfWeek, Time startTime, Time endTime, Integer maxAppointmentsLimit, String uuid, Boolean voided){
        ServiceWeeklyAvailabilityPayload availabilityPayload = new ServiceWeeklyAvailabilityPayload();
        availabilityPayload.setDayOfWeek(dayOfWeek);
        availabilityPayload.setStartTime(startTime);
        availabilityPayload.setEndTime(endTime);
        availabilityPayload.setMaxAppointmentsLimit(maxAppointmentsLimit);
        availabilityPayload.setUuid(uuid);
        availabilityPayload.setVoided(voided);
        return availabilityPayload;
    }

}
