package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentDaoImplIT extends BaseIntegrationTest {

    @Autowired
    AppointmentDao appointmentDao;

    @Autowired
    AppointmentServiceDao appointmentServiceDao;

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

    @Test
    public void shouldGetAllFutureAppointmentFortheGivenService() throws Exception {
        AppointmentService appointmentService = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        List<Appointment> allAppointments = appointmentDao.getAllFutureAppointmentsForService(appointmentService);
        assertNotNull(allAppointments);
        assertEquals(2, allAppointments.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c1111", allAppointments.get(0).getUuid());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c12222", allAppointments.get(1).getUuid());
    }

    @Test
    public void shouldGetAllNonVoidedAndNonCancelledFutureAppointmentFortheGivenServiceType() throws Exception {
        AppointmentService appointmentService = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        Set<AppointmentServiceType> serviceTypes = appointmentService.getServiceTypes();
        AppointmentServiceType appointmentServiceType = serviceTypes.iterator().next();
        List<Appointment> allFutureAppointmentsForServiceType = appointmentDao.getAllFutureAppointmentsForServiceType(appointmentServiceType);
        assertNotNull(allFutureAppointmentsForServiceType);
        assertEquals(1,allFutureAppointmentsForServiceType.size());
        assertEquals("75504r42-3ca8-11e3-bf2b-0800271c13346", allFutureAppointmentsForServiceType.get(0).getUuid());
    }
}
