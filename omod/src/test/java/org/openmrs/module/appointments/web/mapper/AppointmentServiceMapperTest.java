package org.openmrs.module.appointments.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.AppointmentServicePayload;
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

}