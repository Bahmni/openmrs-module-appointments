package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class AppointmentUnavailabilityDaoImplIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentUnavailabilityDao appointmentUnavailabilityDao;

    @Autowired
    private LocationService locationService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentUnavailabilityTestData.xml");
    }

    @Test
    public void shouldSaveAppointmentUnavailability() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");

        AppointmentUnavailability unavailability = new AppointmentUnavailability();
        unavailability.setLocation(location);
        unavailability.setStartDate(new java.sql.Date(DATE_FORMAT.parse("2026-08-10").getTime()));
        unavailability.setStartTime(Time.valueOf("09:00:00"));
        unavailability.setEndDate(new java.sql.Date(DATE_FORMAT.parse("2026-08-10").getTime()));
        unavailability.setEndTime(Time.valueOf("17:00:00"));

        AppointmentUnavailability saved = appointmentUnavailabilityDao.save(unavailability);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getUuid());
        assertEquals(location.getLocationId(), saved.getLocation().getLocationId());
    }

    @Test
    public void shouldGetAppointmentUnavailabilityByUuid() throws Exception {
        String uuid = "uuu006e5-9fbb-4f20-866b-0ece245615a1";
        AppointmentUnavailability unavailability = appointmentUnavailabilityDao.getByUuid(uuid);

        assertNotNull(unavailability);
        assertEquals(uuid, unavailability.getUuid());
        assertNotNull(unavailability.getLocation());
        assertNull(unavailability.getService());
        assertNull(unavailability.getProvider());
    }

    @Test
    public void shouldEvictObjectFromSessionAfterGetByUuid() throws Exception {
        String uuid = "uuu006e5-9fbb-4f20-866b-0ece245615a1";
        AppointmentUnavailability unavailability = appointmentUnavailabilityDao.getByUuid(uuid);
        assertNotNull(unavailability);

        // Modify the object
        unavailability.setEndTime(Time.valueOf("23:00:00"));

        // Fetch again - should get fresh copy from database
        AppointmentUnavailability unavailability2 = appointmentUnavailabilityDao.getByUuid(uuid);
        assertNotNull(unavailability2);
        assertEquals("11:00:00", unavailability2.getEndTime().toString());
    }

    @Test
    public void shouldGetAllForLocation() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, null, null, false, null);

        assertNotNull(results);
        assertEquals(4, results.size()); // 4 non-voided blocks for location 1
    }

    @Test
    public void shouldGetAllIncludingVoided() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, null, null, true, null);

        assertNotNull(results);
        assertEquals(5, results.size()); // 5 blocks including voided for location 1
    }

    @Test
    public void shouldFilterByService() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        AppointmentServiceDefinition service = appointmentServiceDefinitionService
                .getAppointmentServiceByUuid("fff006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, service, null, null, null, false, null);

        assertNotNull(results);
        assertEquals(2, results.size()); // 2 blocks with service 1
    }

    @Test
    public void shouldFilterByProvider() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        Provider provider = providerService.getProviderByUuid("ccc006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, provider, null, null, false, null);

        assertNotNull(results);
        assertEquals(2, results.size()); // 2 blocks with provider 1
    }

    @Test
    public void shouldFilterByServiceAndProvider() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        AppointmentServiceDefinition service = appointmentServiceDefinitionService
                .getAppointmentServiceByUuid("fff006e5-9fbb-4f20-866b-0ece245615a1");
        Provider provider = providerService.getProviderByUuid("ccc006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, service, provider, null, null, false, null);

        assertNotNull(results);
        assertEquals(1, results.size()); // 1 block with both service 1 and provider 1
    }

    @Test
    public void shouldFilterByDateRange() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        Date startDate = DATE_FORMAT.parse("2026-08-05");
        Date endDate = DATE_FORMAT.parse("2026-08-07");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, startDate, endDate, false, null);

        assertNotNull(results);
        assertEquals(3, results.size()); // Blocks 2, 3, 4 overlap with this range
    }

    @Test
    public void shouldRespectLimit() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, null, null, false, 2);

        assertNotNull(results);
        assertEquals(2, results.size()); // Limited to 2 results
    }

    @Test
    public void shouldOrderByStartDateAndStartTime() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, null, null, false, null);

        assertNotNull(results);
        assertTrue(results.size() > 1);

        // Verify ordering
        for (int i = 0; i < results.size() - 1; i++) {
            AppointmentUnavailability current = results.get(i);
            AppointmentUnavailability next = results.get(i + 1);

            // Current startDate should be <= next startDate
            assertTrue(current.getStartDate().compareTo(next.getStartDate()) <= 0);

            // If same date, current startTime should be <= next startTime
            if (current.getStartDate().equals(next.getStartDate())) {
                assertTrue(current.getStartTime().compareTo(next.getStartTime()) <= 0);
            }
        }
    }

    @Test
    public void shouldReturnEmptyListWhenNoMatches() throws Exception {
        Location location = locationService.getLocationByUuid("aaa006e5-9fbb-4f20-866b-0ece245615a1");
        Date startDate = DATE_FORMAT.parse("2026-09-01");
        Date endDate = DATE_FORMAT.parse("2026-09-30");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(
                location, null, null, startDate, endDate, false, null);

        assertNotNull(results);
        assertEquals(0, results.size());
    }
}
