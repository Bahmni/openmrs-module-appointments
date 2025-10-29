package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentSearchParams;
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
        List<AppointmentServiceDefinition> allAppointmentServiceDefinitions = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(6, allAppointmentServiceDefinitions.size());
    }


    @Test
    public void shouldGetAllAppointmentServices() throws Exception {
        List<AppointmentServiceDefinition> allAppointmentServiceDefinitions = appointmentServiceDao.getAllAppointmentServices(true);
        assertEquals(7, allAppointmentServiceDefinitions.size());
    }

    @Test
    public void shouldGetAppointmentServiceByUuid() throws Exception {
        String appointmentServiceUuid = "c36006d4-9fbb-4f20-866b-0ece245615b1";
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentServiceDefinition);
        assertEquals(appointmentServiceUuid, appointmentServiceDefinition.getUuid());
    }

    @Test
    public void shouldGetAppointmentServiceByUuidAndChangeTheStateOfObjectFromPersistedToDetachedUsingEvict() throws Exception {
        String appointmentServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentServiceDefinition);
        assertEquals(appointmentServiceUuid, appointmentServiceDefinition.getUuid());
        assertEquals(30, appointmentServiceDefinition.getDurationMins().intValue());
        appointmentServiceDefinition.setDurationMins(60);
        AppointmentServiceDefinition appointmentServiceDefinition2 = appointmentServiceDao.getAppointmentServiceByUuid(appointmentServiceUuid);
        assertNotNull(appointmentServiceDefinition2);
        assertEquals(appointmentServiceUuid, appointmentServiceDefinition2.getUuid());
        assertEquals(30, appointmentServiceDefinition2.getDurationMins().intValue());
    }

    @Test
    public void shouldSaveAppointmentService() throws Exception {
        List<AppointmentServiceDefinition> allAppointmentServiceDefinitions = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(6, allAppointmentServiceDefinitions.size());
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("Cardiology OPD");
        appointmentServiceDao.save(appointmentServiceDefinition);
        allAppointmentServiceDefinitions = appointmentServiceDao.getAllAppointmentServices(false);
        assertEquals(7, allAppointmentServiceDefinitions.size());
    }

    @Test
    public void shouldSaveAppointmentServiceWithServiceTypes() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = new AppointmentServiceDefinition();
        appointmentServiceDefinition.setName("serviceWithServiceTypes");
        AppointmentServiceType appointmentServiceType1 = new AppointmentServiceType();
        appointmentServiceType1.setName("Type1");
        appointmentServiceType1.setDuration(15);
        appointmentServiceType1.setAppointmentServiceDefinition(appointmentServiceDefinition);
        Set<AppointmentServiceType> appointmentServiceTypes = new LinkedHashSet<>();
        appointmentServiceTypes.add(appointmentServiceType1);
        appointmentServiceDefinition.setServiceTypes(appointmentServiceTypes);
        AppointmentServiceDefinition saved = appointmentServiceDao.save(appointmentServiceDefinition);
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
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentServiceName);
        assertNotNull(appointmentServiceDefinition);
        assertEquals(appointmentServiceUuid, appointmentServiceDefinition.getUuid());
        assertEquals(appointmentServiceName, appointmentServiceDefinition.getName());
        assertEquals(30, appointmentServiceDefinition.getDurationMins().intValue());
        appointmentServiceDefinition.setDurationMins(60);
        AppointmentServiceDefinition appointmentServiceDefinition2 = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentServiceName);
        assertNotNull(appointmentServiceDefinition2);
        assertEquals(appointmentServiceUuid, appointmentServiceDefinition2.getUuid());
        assertEquals(appointmentServiceName, appointmentServiceDefinition2.getName());
        assertEquals(30, appointmentServiceDefinition2.getDurationMins().intValue());
    }

    @Test
    public void shouldNotGetAppointmentServiceByServiceNameIfTheAppointmentServiceIsVoidedAndNoNonVoidedAppointmentServicePresent() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getNonVoidedAppointmentServiceByName("Treatment");
        assertNull(appointmentServiceDefinition);
    }

    @Test
    public void shouldGetNonVoidedAppointmentServiceByServiceName() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getNonVoidedAppointmentServiceByName("Consultation");
        assertNotNull(appointmentServiceDefinition);
        assertEquals("Consultation", appointmentServiceDefinition.getName());
        assertEquals(false, appointmentServiceDefinition.getVoided());
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentServiceDefinition.getUuid());
    }

    @Test
    public void shouldUpdateAppointmentService() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertNotNull(appointmentServiceDefinition);
        assertEquals(4, appointmentServiceDefinition.getMaxAppointmentsLimit().intValue());
        assertEquals(1, appointmentServiceDefinition.getWeeklyAvailability().size());
        assertEquals(1, appointmentServiceDefinition.getServiceTypes().size());
        appointmentServiceDefinition.setMaxAppointmentsLimit(40);
        ServiceWeeklyAvailability availability = new ServiceWeeklyAvailability();
        availability.setDayOfWeek(DayOfWeek.SATURDAY);
        availability.setStartTime(Time.valueOf("10:00:00"));
        availability.setEndTime(Time.valueOf("11:00:00"));
        availability.setService(appointmentServiceDefinition);
        Set avbList = appointmentServiceDefinition.getWeeklyAvailability(true);
        avbList.add(availability);
        AppointmentServiceType serviceType = new AppointmentServiceType();
        serviceType.setName("Second stage of hallucination");
        serviceType.setAppointmentServiceDefinition(appointmentServiceDefinition);
        Set serviceTypes = appointmentServiceDefinition.getServiceTypes(true);
        serviceTypes.add(serviceType);
        appointmentServiceDao.save(appointmentServiceDefinition);
        appointmentServiceDefinition = appointmentServiceDao.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertEquals(40, appointmentServiceDefinition.getMaxAppointmentsLimit().intValue());
        assertNotNull(appointmentServiceDefinition);
        assertEquals(2, appointmentServiceDefinition.getWeeklyAvailability().size());
        assertEquals(2, appointmentServiceDefinition.getServiceTypes().size());
    }

    @Test
    public void shouldGetAppointmentServiceTypeByUuid() throws Exception {
        String serviceTypeUuid = "678906e5-9fbb-4f20-866b-0ece24564578";
        AppointmentServiceType appointmentServiceType = appointmentServiceDao.getAppointmentServiceTypeByUuid(serviceTypeUuid);
        assertNotNull(appointmentServiceType);
        assertEquals(serviceTypeUuid, appointmentServiceType.getUuid());
    }

    @Test
    public void shouldSearchAppointmentServicesByLocationUuid() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(4, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Consultation")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Ortho")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Cardiology Room1")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("General Room1")));
    }

    @Test
    public void shouldSearchAppointmentServicesBySpecialityUuid() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(4, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Consultation")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Ortho")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Ortho Room2")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Tele Ortho")));
    }

    @Test
    public void shouldSearchAppointmentServicesByLocationAndSpecialityUuid() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Consultation")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Ortho")));
    }

    @Test
    public void shouldSearchAppointmentServicesIncludingVoided() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setIncludeVoided(true);

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(3, results.size());
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Consultation")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Ortho")));
        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Treatment") && s.getVoided()));
    }

    @Test
    public void shouldSearchAppointmentServicesExcludingVoidedByDefault() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(AppointmentServiceDefinition::getVoided));
    }

    @Test
    public void shouldSearchAppointmentServicesWithLimit() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setLimit(2);

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(2, results.size());
    }

    @Test
    public void shouldReturnAllNonVoidedServicesWhenNoFiltersProvided() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(6, results.size());
        assertTrue(results.stream().noneMatch(AppointmentServiceDefinition::getVoided));
    }

    @Test
    public void shouldReturnEmptyListWhenNoMatchingLocation() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("non-existent-location-uuid");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(0, results.size());
    }

    @Test
    public void shouldReturnEmptyListWhenNoMatchingSpeciality() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setSpecialityUuid("non-existent-speciality-uuid");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(0, results.size());
    }

    @Test
    public void shouldOrderResultsByAppointmentServiceId() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertTrue(results.size() > 1);
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getAppointmentServiceId() <= results.get(i + 1).getAppointmentServiceId());
        }
    }

    @Test
    public void shouldHandleNullIncludeVoidedAsExcludingVoided() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");
        searchParams.setIncludeVoided(null);

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(AppointmentServiceDefinition::getVoided));
    }

    @Test
    public void shouldIncludeServicesWithoutLocationWhenSearchingBySpeciality() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setSpecialityUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertTrue(results.stream().anyMatch(s -> s.getName().equals("Tele Ortho")));
    }

    @Test
    public void shouldIncludeServicesWithoutSpecialityWhenSearchingByLocation() throws Exception {
        AppointmentSearchParams searchParams = new AppointmentSearchParams();
        searchParams.setLocationUuid("c36006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentServiceDefinition> results = appointmentServiceDao.search(searchParams);

        assertTrue(results.stream().anyMatch(s -> s.getName().equals("General Room1")));
    }
}
