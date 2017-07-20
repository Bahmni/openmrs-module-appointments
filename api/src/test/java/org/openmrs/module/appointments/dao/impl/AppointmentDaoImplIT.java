package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppointmentDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentDao appointmentDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void shouldGetAllNonVoidedAppointments() throws Exception {
        List<Appointment> allAppointmentServices = appointmentDao.getAllAppointments();
        assertEquals(1, allAppointmentServices.size());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<Appointment> allAppointments = appointmentDao.getAllAppointments();
        assertEquals(1, allAppointments.size());
        Appointment apt = new Appointment();
        apt.setPatient(allAppointments.get(0).getPatient());
        appointmentDao.save(apt);
        allAppointments = appointmentDao.getAllAppointments();
        assertEquals(2, allAppointments.size());
    }

}
