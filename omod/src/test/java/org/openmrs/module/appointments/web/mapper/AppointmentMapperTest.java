package org.openmrs.module.appointments.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AppointmentMapperTest {

    @InjectMocks
    private AppointmentMapper appointmentMapper;

    @Test
    public void shouldCreateDefaultResponse() throws Exception {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        appointment.setPatient(patient);
        appointment.setAppointmentsKind(AppointmentKind.Scheduled);
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        
        List<AppointmentDefaultResponse> appointmentDefaultResponse = appointmentMapper.constructResponse(appointmentList);
        assertEquals(appointment.getAppointmentId(), appointmentDefaultResponse.get(0).getAppointmentId());
    }
}
