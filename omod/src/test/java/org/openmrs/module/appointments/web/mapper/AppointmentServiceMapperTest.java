package org.openmrs.module.appointments.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
import org.openmrs.module.appointments.web.contract.AppointmentServiceResponse;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Time;

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
        appointmentServicePayload.setEndTime(Time.valueOf("17:00:00"));
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
        appointmentService.setEndTime(Time.valueOf("17:00:00"));
        appointmentService.setMaxAppointmentsLimit(30);
        Location location = new Location();
        location.setName("Room1");
        location.setUuid("locUuid");
        appointmentService.setLocation(location);
        Speciality speciality = new Speciality();
        speciality.setName("cardio");
        speciality.setUuid("specUuid");
        appointmentService.setSpeciality(speciality);
        AppointmentServiceResponse appointmentServiceResponse = appointmentServiceMapper.constructResponse(appointmentService);
        assertEquals(appointmentServiceResponse.getName(),appointmentService.getName());
        assertEquals(appointmentServiceResponse.getDurationMins(),appointmentService.getDurationMins());
        assertEquals(appointmentServiceResponse.getStartTime(),appointmentService.getStartTime().toString());
        assertEquals(appointmentServiceResponse.getEndTime(),appointmentService.getEndTime().toString());
        assertEquals(appointmentServiceResponse.getMaxAppointmentsLimit(),appointmentService.getMaxAppointmentsLimit());
        assertEquals(appointmentServiceResponse.getLocation().get("name"), "Room1");
        assertEquals(appointmentServiceResponse.getSpeciality().get("name"), "cardio");
    }
}