package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Time;
import java.time.DayOfWeek;
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
        assertEquals(2, allAppointmentServices.size());
    }


    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(true);
        assertEquals(3, allAppointmentServices.size());
    }

    @Test
    public void shouldGetAppointmentServiceByUuid() throws Exception {
        String appointmentServiceUuid = "c36006d4-9fbb-4f20-866b-0ece245615b1";
        AppointmentService appointmentService = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentService);
        assertEquals(appointmentServiceUuid, appointmentService.getUuid());
    }

    @Test
    public void shouldGetAppointmentServiceByUuidAndChangeTheStateOfObjectFromPersistedToDetachedUsingEvict() throws Exception {
        String appointmentServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        AppointmentService appointmentService = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentService);
        assertEquals(appointmentServiceUuid, appointmentService.getUuid());
        assertEquals(30, appointmentService.getDurationMins().intValue());
        appointmentService.setDurationMins(60);
        AppointmentService appointmentService2 = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentService2);
        assertEquals(appointmentServiceUuid, appointmentService2.getUuid());
        assertEquals(30, appointmentService2.getDurationMins().intValue());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<AppointmentService> allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(2, allAppointmentServices.size());
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("Cardiology OPD");
        appointmentServiceDao.save(appointmentService);
        allAppointmentServices = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(3, allAppointmentServices.size());
    }

    @Test
    public void shouldSaveAppointmentServiceWithServiceTypes() throws Exception {
        AppointmentService appointmentService = new AppointmentService();
        appointmentService.setName("serviceWithServiceTypes");
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setName("Type1");
        appointmentServiceType1.setDuration(15);
        appointmentServiceType1.setAppointmentService(appointmentService);
        Set<AppointmentServiceType> appointmentServiceTypes = new LinkedHashSet<>();
        appointmentServiceTypes.add(appointmentServiceType1);
        appointmentService.setServiceTypes(appointmentServiceTypes);
        AppointmentService saved = appointmentServiceDao.save(appointmentService);
        assertNotNull(saved.getId());
        assertNotNull(saved.getServiceTypes());
        Iterator<AppointmentServiceType> iterator = saved.getServiceTypes().iterator();
        AppointmentServiceType type1 = iterator.next();
        assertNotNull(type1.getId());
        assertEquals(15, type1.getDuration(), 0);
        assertEquals("Type1", type1.getName());
    }

    @Test
    public void shouldGetNonVoidedAppointmentServiceByNameAndChangeTheStateOfObjectFromPersistedToDetachedUsingEvict() throws Exception {
        String appointmentServiceName = "Consultation";
        String appointmentServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        AppointmentService appointmentService = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentServiceName);
        assertNotNull(appointmentService);
        assertEquals(appointmentServiceUuid, appointmentService.getUuid());
        assertEquals(appointmentServiceName, appointmentService.getName());
        assertEquals(30, appointmentService.getDurationMins().intValue());
        appointmentService.setDurationMins(60);
        AppointmentService appointmentService2 = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentServiceName);
        assertNotNull(appointmentService2);
        assertEquals(appointmentServiceUuid, appointmentService2.getUuid());
        assertEquals(appointmentServiceName, appointmentService2.getName());
        assertEquals(30, appointmentService2.getDurationMins().intValue());
    }

    @Test
    public void shouldNotGetAppointmentServiceByServiceNameIfTheAppointmentServiceIsVoidedAndNoNonVoidedAppointmentServicePresent() throws Exception {
        AppointmentService appointmentService = appointmentServiceDao.getNonVoidedAppointmentServiceByName("Treatment");
        assertNull(appointmentService);
    }

    @Test
    public void shouldGetNonVoidedAppointmentServiceByServiceName() throws Exception {
        AppointmentService appointmentService = appointmentServiceDao.getNonVoidedAppointmentServiceByName("Consultation");
        assertNotNull(appointmentService);
        assertEquals("Consultation", appointmentService.getName());
        assertEquals(false, appointmentService.getVoided());
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentService.getUuid());
    }

    @Test
    public void shouldUpdateAppointmentService() throws Exception {
        AppointmentService appointmentService = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertNotNull(appointmentService);
        assertEquals(4, appointmentService.getMaxAppointmentsLimit().intValue());
        assertEquals(1, appointmentService.getWeeklyAvailability().size());
        assertEquals(1, appointmentService.getServiceTypes().size());
        appointmentService.setMaxAppointmentsLimit(40);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.SATURDAY);
        availability.setStartTime(Time.valueOf("10:00:00"));
        availability.setEndTime(Time.valueOf("11:00:00"));
        availability.setService(appointmentService);
        Set avbList = appointmentService.getWeeklyAvailability(true);
        avbList.add(availability);
        AppointmentServiceType serviceType = new AppointmentServiceType();
        serviceType.setName("Second stage of hallucination");
        serviceType.setAppointmentService(appointmentService);
        Set serviceTypes = appointmentService.getServiceTypes(true);
        serviceTypes.add(serviceType);
        appointmentServiceDao.save(appointmentService);
        appointmentService = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertEquals(40, appointmentService.getMaxAppointmentsLimit().intValue());
        assertNotNull(appointmentService);
        assertEquals(2, appointmentService.getWeeklyAvailability().size());
        assertEquals(2, appointmentService.getServiceTypes().size());
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuid() throws Exception {
        String serviceTypeUuid = "678906e5-9fbb-4f20-866b-0ece24564578";
        AppointmentServiceType appointmentServiceType = appointmentServiceDao.getAppointmentServiceTypeByUuid(serviceTypeUuid);
        assertNotNull(appointmentServiceType);
        assertEquals(serviceTypeUuid, appointmentServiceType.getUuid());
    }
}
