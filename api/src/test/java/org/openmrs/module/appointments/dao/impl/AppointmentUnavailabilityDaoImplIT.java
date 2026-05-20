package org.openmrs.module.appointments.dao.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appointments.BaseIntegrationTest;
import org.openmrs.module.appointments.dao.AppointmentUnavailabilityDao;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.model.AppointmentUnavailabilitySearchParams;
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

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String LOCATION_UUID = "aaa006e5-9fbb-4f20-866b-0ece245615a1";
    private static final String SERVICE_UUID = "fff006e5-9fbb-4f20-866b-0ece245615a1";
    private static final String PROVIDER_UUID = "ccc006e5-9fbb-4f20-866b-0ece245615a1";

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentUnavailabilityTestData.xml");
    }

    @Test
    public void shouldSaveAppointmentUnavailability() throws Exception {
        Location location = locationService.getLocationByUuid(LOCATION_UUID);

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

        unavailability.setEndTime(Time.valueOf("23:00:00"));

        AppointmentUnavailability unavailability2 = appointmentUnavailabilityDao.getByUuid(uuid);
        assertNotNull(unavailability2);
        assertEquals("11:00:00", unavailability2.getEndTime().toString());
    }

    @Test
    public void shouldGetAllForLocation() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(4, results.size()); // 4 non-voided blocks for location 1
    }

    @Test
    public void shouldGetAllIncludingVoided() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setIncludeVoided(true);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(5, results.size()); 
    }

    @Test
    public void shouldFilterByService() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setServiceUuid(SERVICE_UUID);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void shouldFilterByProvider() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setProviderUuid(PROVIDER_UUID);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    public void shouldFilterByServiceAndProvider() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setServiceUuid(SERVICE_UUID);
        searchParams.setProviderUuid(PROVIDER_UUID);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    public void shouldFilterByDateRange() throws Exception {
        Date startDate = DATE_FORMAT.parse("2026-08-05");
        Date endDate = DATE_FORMAT.parse("2026-08-07");

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setStartDate(startDate);
        searchParams.setEndDate(endDate);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(3, results.size()); // Blocks 2, 3, 4 overlap with this range
    }

    @Test
    public void shouldRespectLimit() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setLimit(2);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(2, results.size()); // Limited to 2 results
    }

    @Test
    public void shouldOrderByStartDateAndStartTime() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertTrue(results.size() > 1);

        for (int i = 0; i < results.size() - 1; i++) {
            AppointmentUnavailability current = results.get(i);
            AppointmentUnavailability next = results.get(i + 1);

            // Current startDate should be <= next startDate
            assertTrue(current.getStartDate().compareTo(next.getStartDate()) <= 0);

            if (current.getStartDate().equals(next.getStartDate())) {
                assertTrue(current.getStartTime().compareTo(next.getStartTime()) <= 0);
            }
        }
    }

    @Test
    public void shouldReturnEmptyListWhenNoMatches() throws Exception {
        Date startDate = DATE_FORMAT.parse("2026-09-01");
        Date endDate = DATE_FORMAT.parse("2026-09-30");

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid(LOCATION_UUID);
        searchParams.setStartDate(startDate);
        searchParams.setEndDate(endDate);

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    public void shouldReturnAllWhenLocationUuidIsNull() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertTrue(results.size() >= 4); // Should return all non-voided blocks
    }

    @Test
    public void shouldIgnoreInvalidLocationUuid() throws Exception {
        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        searchParams.setLocationUuid("non-existent-uuid");

        List<AppointmentUnavailability> results = appointmentUnavailabilityDao.getAll(searchParams);

        assertNotNull(results);
        assertTrue(results.size() >= 4);
    }
}
