package org.openmrs.module.appointments.web.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentKind;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.appointments.service.SpecialityService;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class AppointmentMapperTest {

    @InjectMocks
    private AppointmentMapper appointmentMapper;

    @Test
    public void shouldCreateDefaultResponse() throws Exception {
        Appointment appointment = new Appointment();
        PersonName name = new PersonName();
        name.setGivenName("patient007");
        Set nameSet = new HashSet();
        nameSet.add(name);
        Patient patient = new Patient();
        patient.setNames(nameSet);
        appointment.setPatient(patient);
        appointment.setAppointmentsKind(AppointmentKind.Scheduled);
        appointment.setStatus(AppointmentStatus.Scheduled);
        List<Appointment> appointmentList = new ArrayList<>();
        appointmentList.add(appointment);
        
        List<AppointmentDefaultResponse> appointmentDefaultResponse = appointmentMapper.constructResponse(appointmentList);
        assertEquals(appointment.getAppointmentId(), appointmentDefaultResponse.get(0).getAppointmentId());
    }
}
