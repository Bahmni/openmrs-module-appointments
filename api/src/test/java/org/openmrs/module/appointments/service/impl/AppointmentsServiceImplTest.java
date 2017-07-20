package org.openmrs.module.appointments.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
public class AppointmentsServiceImplTest {

    @Mock
    private AppointmentDao appointmentDao;

    @InjectMocks
    AppointmentsServiceImpl appointmentsService;

    @Test
    public void testCreateAppointment() throws Exception {
        Appointment appointment = new Appointment();
        appointment.setPatient(new Patient());
        appointmentsService.save(appointment);
        Mockito.verify(appointmentDao, times(1)).save(appointment);
    }

    @Test
    public void testGetAllAppointmentServices() throws Exception {
        appointmentsService.getAllAppointments();
        Mockito.verify(appointmentDao, times(1)).getAllAppointments();
    }
}