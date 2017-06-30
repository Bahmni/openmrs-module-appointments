package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class AppointmentServiceDaoImplIT extends BaseIntegrationTest {


    @Autowired
    AppointmentServiceDao appointmentServiceDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServicesTestData.xml");
    }

    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices();
        assertEquals(1, allAppointmentServices.size());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices();
        assertEquals(1, allAppointmentServices.size());
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Cardiology OPD");
        appointmentServiceDao.save(appointmentService);
        allAppointmentServices = appointmentServiceDao.getAllAppointmentServices();
        assertEquals(2, allAppointmentServices.size());
    }

}