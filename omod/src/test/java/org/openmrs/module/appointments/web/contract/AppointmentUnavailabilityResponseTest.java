package org.openmrs.module.appointments.web.contract;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AppointmentUnavailabilityResponseTest {

    private AppointmentUnavailabilityResponse response;

    @Before
    public void setUp() {
        response = new AppointmentUnavailabilityResponse();
    }

    @Test
    public void shouldSetAndGetUuid() {
        String uuid = "test-uuid-123";
        response.setUuid(uuid);
        assertEquals(uuid, response.getUuid());
    }

    @Test
    public void shouldSetAndGetLocationUuid() {
        String locationUuid = "location-uuid-456";
        response.setLocationUuid(locationUuid);
        assertEquals(locationUuid, response.getLocationUuid());
    }

    @Test
    public void shouldSetAndGetLocationName() {
        String locationName = "Test Location";
        response.setLocationName(locationName);
        assertEquals(locationName, response.getLocationName());
    }

    @Test
    public void shouldSetAndGetAppointmentServiceUuid() {
        String serviceUuid = "service-uuid-789";
        response.setAppointmentServiceUuid(serviceUuid);
        assertEquals(serviceUuid, response.getAppointmentServiceUuid());
    }

    @Test
    public void shouldSetAndGetAppointmentServiceName() {
        String serviceName = "Test Service";
        response.setAppointmentServiceName(serviceName);
        assertEquals(serviceName, response.getAppointmentServiceName());
    }

    @Test
    public void shouldSetAndGetProviderUuid() {
        String providerUuid = "provider-uuid-111";
        response.setProviderUuid(providerUuid);
        assertEquals(providerUuid, response.getProviderUuid());
    }

    @Test
    public void shouldSetAndGetProviderName() {
        String providerName = "Dr. Test Provider";
        response.setProviderName(providerName);
        assertEquals(providerName, response.getProviderName());
    }

    @Test
    public void shouldSetAndGetStartDate() {
        String startDate = "2026-08-10";
        response.setStartDate(startDate);
        assertEquals(startDate, response.getStartDate());
    }

    @Test
    public void shouldSetAndGetStartTime() {
        String startTime = "09:00";
        response.setStartTime(startTime);
        assertEquals(startTime, response.getStartTime());
    }

    @Test
    public void shouldSetAndGetEndDate() {
        String endDate = "2026-08-10";
        response.setEndDate(endDate);
        assertEquals(endDate, response.getEndDate());
    }

    @Test
    public void shouldSetAndGetEndTime() {
        String endTime = "17:00";
        response.setEndTime(endTime);
        assertEquals(endTime, response.getEndTime());
    }

    @Test
    public void shouldSetAndGetVoided() {
        Boolean voided = true;
        response.setVoided(voided);
        assertEquals(voided, response.getVoided());
    }

    @Test
    public void shouldSetAndGetVoidedAsFalse() {
        Boolean voided = false;
        response.setVoided(voided);
        assertEquals(voided, response.getVoided());
    }

    @Test
    public void shouldSetAndGetDateCreated() {
        String dateCreated = "2026-08-10 10:30";
        response.setDateCreated(dateCreated);
        assertEquals(dateCreated, response.getDateCreated());
    }

    @Test
    public void shouldSetAndGetCreatorName() {
        String creatorName = "admin";
        response.setCreatorName(creatorName);
        assertEquals(creatorName, response.getCreatorName());
    }

    @Test
    public void shouldHandleNullValues() {
        response.setUuid(null);
        response.setLocationUuid(null);
        response.setLocationName(null);
        response.setAppointmentServiceUuid(null);
        response.setAppointmentServiceName(null);
        response.setProviderUuid(null);
        response.setProviderName(null);
        response.setStartDate(null);
        response.setStartTime(null);
        response.setEndDate(null);
        response.setEndTime(null);
        response.setVoided(null);
        response.setDateCreated(null);
        response.setCreatorName(null);

        assertNull(response.getUuid());
        assertNull(response.getLocationUuid());
        assertNull(response.getLocationName());
        assertNull(response.getAppointmentServiceUuid());
        assertNull(response.getAppointmentServiceName());
        assertNull(response.getProviderUuid());
        assertNull(response.getProviderName());
        assertNull(response.getStartDate());
        assertNull(response.getStartTime());
        assertNull(response.getEndDate());
        assertNull(response.getEndTime());
        assertNull(response.getVoided());
        assertNull(response.getDateCreated());
        assertNull(response.getCreatorName());
    }

    @Test
    public void shouldCreateEmptyResponse() {
        AppointmentUnavailabilityResponse emptyResponse = new AppointmentUnavailabilityResponse();
        
        assertNull(emptyResponse.getUuid());
        assertNull(emptyResponse.getLocationUuid());
        assertNull(emptyResponse.getLocationName());
        assertNull(emptyResponse.getAppointmentServiceUuid());
        assertNull(emptyResponse.getAppointmentServiceName());
        assertNull(emptyResponse.getProviderUuid());
        assertNull(emptyResponse.getProviderName());
        assertNull(emptyResponse.getStartDate());
        assertNull(emptyResponse.getStartTime());
        assertNull(emptyResponse.getEndDate());
        assertNull(emptyResponse.getEndTime());
        assertNull(emptyResponse.getVoided());
        assertNull(emptyResponse.getDateCreated());
        assertNull(emptyResponse.getCreatorName());
    }

    @Test
    public void shouldSetAllFieldsAndRetrieveThem() {
        // Setup
        response.setUuid("uuid-123");
        response.setLocationUuid("loc-uuid");
        response.setLocationName("Main Location");
        response.setAppointmentServiceUuid("service-uuid");
        response.setAppointmentServiceName("General Consultation");
        response.setProviderUuid("provider-uuid");
        response.setProviderName("Dr. John");
        response.setStartDate("2026-08-15");
        response.setStartTime("10:00");
        response.setEndDate("2026-08-15");
        response.setEndTime("18:00");
        response.setVoided(false);
        response.setDateCreated("2026-08-01 09:00");
        response.setCreatorName("system");

        // Verify
        assertEquals("uuid-123", response.getUuid());
        assertEquals("loc-uuid", response.getLocationUuid());
        assertEquals("Main Location", response.getLocationName());
        assertEquals("service-uuid", response.getAppointmentServiceUuid());
        assertEquals("General Consultation", response.getAppointmentServiceName());
        assertEquals("provider-uuid", response.getProviderUuid());
        assertEquals("Dr. John", response.getProviderName());
        assertEquals("2026-08-15", response.getStartDate());
        assertEquals("10:00", response.getStartTime());
        assertEquals("2026-08-15", response.getEndDate());
        assertEquals("18:00", response.getEndTime());
        assertFalse(response.getVoided());
        assertEquals("2026-08-01 09:00", response.getDateCreated());
        assertEquals("system", response.getCreatorName());
    }
}
