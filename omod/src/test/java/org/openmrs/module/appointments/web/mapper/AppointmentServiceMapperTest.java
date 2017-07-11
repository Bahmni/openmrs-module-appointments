package org.openmrs.module.appointments.web.mapper;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.appointments.web.contract.ServiceWeeklyAvailabilityPayload;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(PowerMockRunner.class)
public class AppointmentServiceMapperTest {
    @Mock
    private LocationService locationService;

    @Mock
    private SpecialityService specialityService;

    @InjectMocks
    private AppointmentServiceMapper appointmentServiceMapper;

    private Location location;

    private Speciality speciality;

    @Test
    public void shouldGetAppointmentServiceFromPayloadWithoutWeeklyAvailability() throws Exception {
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(appointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(appointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(appointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
        assertNull(appointmentService.getWeeklyAvailability());
    }

    @Test
    public void shouldGetAppointmentServiceFromPayload() throws Exception {
        AppointmentServicePayload appointmentServicePayload = createAppointmentServicePayload();
        List<ServiceWeeklyAvailabilityPayload> weeklyAvailability = new ArrayList<>();
        ServiceWeeklyAvailabilityPayload mondayMorning = new ServiceWeeklyAvailabilityPayload();
        mondayMorning.setStartTime(Time.valueOf("09:00:00"));
        mondayMorning.setEndTime(Time.valueOf("13:00:00"));
        mondayMorning.setMaxAppointmentsLimit(20);
        mondayMorning.setDayOfWeek(DayOfWeek.MONDAY);
        weeklyAvailability.add(mondayMorning);
        appointmentServicePayload.setWeeklyAvailability(weeklyAvailability);
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
        return appointmentServicePayload;
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
        
        List<AppointmentServiceDefaultResponse> appointmentServicesResponse = appointmentServiceMapper.constructResponse(appointmentServices);
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
        speciality.setName("cardio");
        appointmentService.setSpeciality(speciality);
        
        return appointmentService;
    }

}
