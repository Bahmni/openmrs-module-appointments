package org.openmrs.module.appointments.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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

    @Test
    public void shouldGetAllFutureAppoitmentsForTheGivenAppointmentService() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        when(appointmentDao.getAllFutureAppointmentsForService(appointmentService)).thenReturn(new ArrayList<>());

        appointmentsService.getAllFutureAppointmentsForService(appointmentService);

        Mockito.verify(appointmentDao, times(1)).getAllFutureAppointmentsForService(appointmentService);
    }
}