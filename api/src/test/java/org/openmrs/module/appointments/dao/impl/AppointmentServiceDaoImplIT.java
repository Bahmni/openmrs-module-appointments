package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

import static org.junit.Assert.*;

public class AppointmentServiceDaoImplIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentServiceDao appointmentServiceDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServicesTestData.xml");
    }

    @Test
    public void shouldGetAllNonVoidedAppointmentServices() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(1, allAppointmentServices.size());
    }


    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(true);
        assertEquals(2, allAppointmentServices.size());
    }

    @Test
    public void shouldGetAppointmentServiceByUuid() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(true);
        assertEquals(2, allAppointmentServices.size());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(1, allAppointmentServices.size());
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Cardiology OPD");
        appointmentServiceDao.save(appointmentService);
        allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(2, allAppointmentServices.size());
    }

    @Test
    public void shouldSaveAppointmentServiceWithServiceTypes() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setName("Type1");
        appointmentServiceType1.setDuration(15);
        appointmentServiceType1.setAppointmentService(appointmentService);
        AppointmentServiceType appointmentServiceType2 = new AppointmentServiceType();
        appointmentServiceType2.setName("Type2");
        appointmentService.setName("serviceWithServiceTypes");
        appointmentServiceType1.setAppointmentService(appointmentService);
        appointmentServiceType2.setAppointmentService(appointmentService);
        Set<AppointmentServiceType> appointmentServiceTypes = new LinkedHashSet<>();
        appointmentServiceTypes.add(appointmentServiceType1);
        appointmentServiceTypes.add(appointmentServiceType2);
        appointmentService.setServiceTypes(appointmentServiceTypes);
        AppointmentService saved = appointmentServiceDao.save(appointmentService);
        assertNotNull(saved.getId());
        assertNotNull(saved.getServiceTypes());
        Iterator<AppointmentServiceType> iterator = saved.getServiceTypes().iterator();
        AppointmentServiceType type1 = iterator.next();
        AppointmentServiceType type2 = iterator.next();
        assertNotNull(type1.getId());
        assertEquals(15, type1.getDuration(), 0);
        assertNotNull("Type1", type1.getName());
        assertNotNull(type1.getId());
        assertNull(type2.getDuration());
        assertNotNull("Type2", type2.getName());
    }
}
