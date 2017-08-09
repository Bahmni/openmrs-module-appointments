package org.openmrs.module.appointments.service.impl;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
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
    public void testGetAllAppointments() throws Exception {
        appointmentsService.getAllAppointments(null);
        Mockito.verify(appointmentDao, times(1)).getAllAppointments(null);
    }

    @Test
    public void shouldNotGetAppointmentsWithVoidedService() throws Exception {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        AppointmentService appointmentService1 = new AppointmentService();
        appointmentService1.setVoided(true);
        appointment1.setService(appointmentService1);
        Appointment appointment2 = new Appointment();
        appointment2.setService(new AppointmentService());
        appointments.add(appointment1);
        appointments.add(appointment2);
        when(appointmentDao.getAllAppointments(null)).thenReturn(appointments);
        List<Appointment> appointmentList = appointmentsService.getAllAppointments(null);
        Mockito.verify(appointmentDao, times(1)).getAllAppointments(null);
        assertEquals(appointmentList.size(), 1);
    }
    
    @Test
    public void shouldNotGetAppointmentsWithVoidedServiceType() throws Exception {
        List<Appointment> appointments = new ArrayList<>();
        Appointment appointment1 = new Appointment();
        AppointmentService appointmentService1 = new AppointmentService();
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setVoided(true);
        appointmentService1.setServiceTypes(Collections.singleton(appointmentServiceType1));
        appointment1.setService(appointmentService1);
        appointment1.setServiceType(appointmentServiceType1);
        appointments.add(appointment1);
        Appointment appointment2 = new Appointment();
        appointment2.setService(new AppointmentService());
        appointments.add(appointment2);
        when(appointmentDao.getAllAppointments(null)).thenReturn(appointments);
        List<Appointment> appointmentList = appointmentsService.getAllAppointments(null);
        Mockito.verify(appointmentDao, times(1)).getAllAppointments(null);
        assertEquals(appointmentList.size(), 1);
    }
    
    @Test
    public void shouldGetAllFutureAppointmentsForTheGivenAppointmentService() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setUuid("uuid");
        when(appointmentDao.getAllFutureAppointmentsForService(appointmentService)).thenReturn(new ArrayList<>());

        appointmentsService.getAllFutureAppointmentsForService(appointmentService);

        Mockito.verify(appointmentDao, times(1)).getAllFutureAppointmentsForService(appointmentService);
    }

    @Test
    public void shouldGetAllFutureAppoitmentsForTheGivenAppointmentServiceType() throws Exception {
        AppointmentServiceType appointmentServiceType = new AppointmentServiceType();
        appointmentServiceType.setUuid("typeUuid");
        when(appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType)).thenReturn(new ArrayList<>());

        appointmentsService.getAllFutureAppointmentsForServiceType(appointmentServiceType);

        Mockito.verify(appointmentDao, times(1)).getAllFutureAppointmentsForServiceType(appointmentServiceType);
    }
}