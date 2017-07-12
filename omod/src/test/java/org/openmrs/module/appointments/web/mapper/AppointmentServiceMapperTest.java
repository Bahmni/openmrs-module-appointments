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
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
public class AppointmentServiceMapperTest {
    @Mock
    private LocationService locationService;

    @Mock
    private SpecialityService specialityService;

    @InjectMocks
    private AppointmentServiceMapper appointmentServiceMapper;

    @Test
    public void shouldGetAppointmentServiceFromPayload() throws Exception {
        AppointmentServicePayload appointmentServicePayload = new AppointmentServicePayload();
        appointmentServicePayload.setName("Cardiology-OPD");
        appointmentServicePayload.setDurationMins(20);
        appointmentServicePayload.setStartTime(Time.valueOf("09:00:00"));
        appointmentServicePayload.setMaxAppointmentsLimit(30);
        appointmentServicePayload.setLocationUuid("locUuid");
        appointmentServicePayload.setSpecialityUuid("specUuid");
        AppointmentService appointmentService = appointmentServiceMapper.getAppointmentServiceFromPayload(appointmentServicePayload);
        assertEquals(appointmentService.getName(),appointmentServicePayload.getName());
        assertEquals(appointmentService.getDurationMins(),appointmentServicePayload.getDurationMins());
        assertEquals(appointmentService.getStartTime(),appointmentServicePayload.getStartTime());
        assertEquals(appointmentService.getEndTime(),appointmentServicePayload.getEndTime());
        assertEquals(appointmentService.getMaxAppointmentsLimit(),appointmentServicePayload.getMaxAppointmentsLimit());
    }


    @Test
    public void shouldCreateResponseFromAppointmentService() throws Exception {
        Location location = new Location();
        location.setName("Room1");
        location.setUuid("locUuid");
        Speciality speciality = new Speciality();
        speciality.setName("cardio");
        speciality.setUuid("specUuid");
        AppointmentService appointmentService = buildAppointmentService("Cardiology-OPD",Time.valueOf("09:00:00"),
                null,20, 30, location, speciality );

        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        appointmentService.setWeeklyAvailability(availabilityList);
        AppointmentServiceFullResponse appointmentServiceFullResponse = appointmentServiceMapper.constructResponse(appointmentService);
        assertEquals(appointmentServiceFullResponse.getName(),appointmentService.getName());
        assertEquals(appointmentServiceFullResponse.getDurationMins(),appointmentService.getDurationMins());
        assertEquals(appointmentServiceFullResponse.getStartTime(),appointmentService.getStartTime().toString());
        assertEquals(appointmentServiceFullResponse.getEndTime(),new String());
        assertEquals(appointmentServiceFullResponse.getMaxAppointmentsLimit(),appointmentService.getMaxAppointmentsLimit());
        assertEquals(appointmentServiceFullResponse.getLocation().get("name"), "Room1");
        assertEquals(appointmentServiceFullResponse.getSpeciality().get("name"), "cardio");
        assertNotNull(appointmentServiceFullResponse.getWeeklyAvailability());
        assertEquals(1, appointmentServiceFullResponse.getWeeklyAvailability().size());
    }
    
    @Test
    public void shouldCreateDefaultResponseFromAppointmentServicesList() throws Exception {
        Location location = new Location();
        location.setName("Room1");
        location.setUuid("locUuid");
        Speciality speciality = new Speciality();
        speciality.setName("cardio");
        speciality.setUuid("specUuid");
        
        AppointmentService cardiologyService = buildAppointmentService("Cardiology-OPD", Time.valueOf("09:00:00"),
                null, 20, 30, location, speciality);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.MONDAY);
        HashSet<ServiceWeeklyAvailability> availabilityList = new HashSet<>();
        availabilityList.add(availability);
        cardiologyService.setWeeklyAvailability(availabilityList);
        
        AppointmentService chemoTherapyService = buildAppointmentService("Chemotherapy", Time.valueOf("11:00:00"),
                Time.valueOf("18:30:00"), 30, 10, location, speciality);
        ServiceWeeklyAvailability serviceWeeklyAvailability = new ServiceWeeklyAvailability();
        serviceWeeklyAvailability.setDayOfWeek(DayOfWeek.TUESDAY);
        HashSet<ServiceWeeklyAvailability> availabilities = new HashSet<>();
        availabilities.add(serviceWeeklyAvailability);
        chemoTherapyService.setWeeklyAvailability(availabilities);
        
        List<AppointmentService> appointmentServices = new ArrayList<>();
        appointmentServices.add(cardiologyService);
        appointmentServices.add(chemoTherapyService);
        
        List<AppointmentServiceDefaultResponse> appointmentServicesResponse = appointmentServiceMapper.constructResponse(appointmentServices);
        assertEquals(appointmentServicesResponse.get(0).getName(), cardiologyService.getName());
        assertEquals(appointmentServicesResponse.get(0).getDurationMins(), cardiologyService.getDurationMins());
        assertEquals(appointmentServicesResponse.get(0).getStartTime(), cardiologyService.getStartTime().toString());
        assertEquals(appointmentServicesResponse.get(0).getEndTime(), new String());
        assertEquals(appointmentServicesResponse.get(0).getMaxAppointmentsLimit(), cardiologyService.getMaxAppointmentsLimit());
        assertEquals(appointmentServicesResponse.get(0).getLocation().get("name"), "Room1");
        assertEquals(appointmentServicesResponse.get(0).getSpeciality().get("name"), "cardio");
        assertEquals(appointmentServicesResponse.get(1).getName(), chemoTherapyService.getName());
        assertEquals(appointmentServicesResponse.get(1).getDurationMins(), chemoTherapyService.getDurationMins());
        assertEquals(appointmentServicesResponse.get(1).getStartTime(), chemoTherapyService.getStartTime().toString());
        assertEquals(appointmentServicesResponse.get(1).getEndTime(), chemoTherapyService.getEndTime().toString());
        assertEquals(appointmentServicesResponse.get(1).getMaxAppointmentsLimit(), chemoTherapyService.getMaxAppointmentsLimit());
        assertEquals(appointmentServicesResponse.get(1).getLocation().get("name"), "Room1");
        assertEquals(appointmentServicesResponse.get(1).getSpeciality().get("name"), "cardio");
    }
    
    private AppointmentService buildAppointmentService(String name, Time startTime, Time endTime, Integer duration,
                                                       Integer maxAppointmentsLimit, Location location, Speciality speciality) {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName(name);
        appointmentService.setStartTime(startTime);
        appointmentService.setEndTime(endTime);
        appointmentService.setDurationMins(duration);
        appointmentService.setMaxAppointmentsLimit(maxAppointmentsLimit);
        appointmentService.setLocation(location);
        appointmentService.setSpeciality(speciality);
        
        return appointmentService;
    }
}
