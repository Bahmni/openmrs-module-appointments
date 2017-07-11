package org.openmrs.module.appointments.web.mapper;

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
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;
import java.time.DayOfWeek;
import java.util.HashSet;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class AppointmentServiceMapperTest {
    @Mock
    LocationService locationService;

    @Mock
    SpecialityService specialityService;

    @InjectMocks
    AppointmentServiceMapper appointmentServiceMapper;

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
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Cardiology-OPD");
        appointmentService.setDurationMins(20);
        appointmentService.setStartTime(Time.valueOf("09:00:00"));
        appointmentService.setMaxAppointmentsLimit(30);
        Location location = new Location();
        location.setName("Room1");
        location.setUuid("locUuid");
        appointmentService.setLocation(location);
        Speciality speciality = new Speciality();
        speciality.setName("cardio");
        speciality.setUuid("specUuid");
        appointmentService.setSpeciality(speciality);

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
}
