package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.search.param.AppointmentUnavailabilitySearchParams;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentUnavailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AppointmentUnavailabilityControllerIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentUnavailabilityController controller;

    @Autowired
    private AppointmentUnavailabilityService service;

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String futureDate(int plusDays) {
        return LocalDate.now().plusDays(plusDays).format(DATE_FORMATTER);
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentUnavailabilityTestData.xml");
    }

    @Test
    public void shouldCreateSingleUnavailabilityBlock() throws Exception {
        String date = futureDate(30);
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date + "\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + date + "\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] createdUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(createdUnavailabilityBlocks);
        assertEquals(1, createdUnavailabilityBlocks.length);

        AppointmentUnavailabilityResponse createdBlock = createdUnavailabilityBlocks[0];
        assertNotNull(createdBlock.getUuid());
        assertNotNull(createdBlock.getLocation());
        assertEquals("aaa006e5-9fbb-4f20-866b-0ece245615a1", createdBlock.getLocation().getUuid());
        assertEquals("Test Location 1", createdBlock.getLocation().getName());
        assertEquals(date, createdBlock.getStartDate());
        assertEquals("09:00", createdBlock.getStartTime());
        assertEquals(date, createdBlock.getEndDate());
        assertEquals("17:00", createdBlock.getEndTime());
        assertNull(createdBlock.getService());
        assertNull(createdBlock.getProvider());
    }

    @Test
    public void shouldCreateBatchOfFourBlocks() throws Exception {
        String date = futureDate(32);
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"fff006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"providerUuid\":\"ccc006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date + "\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"" + date + "\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"fff006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"providerUuid\":\"ddd006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"startDate\":\"" + date + "\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"" + date + "\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"ggg006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"providerUuid\":\"ccc006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date + "\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"" + date + "\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"ggg006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"providerUuid\":\"ddd006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"startDate\":\"" + date + "\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"" + date + "\",\"endTime\":\"19:00\"}" +
                "]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] createdUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(createdUnavailabilityBlocks);
        assertEquals(4, createdUnavailabilityBlocks.length);

        AppointmentUnavailabilityResponse firstCreatedBlock = createdUnavailabilityBlocks[0];
        assertNotNull(firstCreatedBlock.getService());
        assertEquals("fff006e5-9fbb-4f20-866b-0ece245615a1", firstCreatedBlock.getService().getUuid());
        assertEquals("Test Service 1", firstCreatedBlock.getService().getName());
        assertNotNull(firstCreatedBlock.getProvider());
        assertEquals("ccc006e5-9fbb-4f20-866b-0ece245615a1", firstCreatedBlock.getProvider().getUuid());
    }

    @Test
    public void shouldCreateBlockWithServiceAndProviderAsNull() throws Exception {
        String date = futureDate(31);
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":null," +
                "\"providerUuid\":null," +
                "\"startDate\":\"" + date + "\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + date + "\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] createdUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertEquals(1, createdUnavailabilityBlocks.length);
        AppointmentUnavailabilityResponse createdBlock = createdUnavailabilityBlocks[0];
        assertNull(createdBlock.getService());
        assertNull(createdBlock.getProvider());
    }

    @Test
    public void shouldReturnBadRequestForEmptyArray() throws Exception {
        String dataJson = "[]";

        try {
            handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown APIException");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("at least one unavailability block"));
        }
    }

    @Test
    public void shouldReturnBadRequestWhenEndDateBeforeStartDate() throws Exception {
        String startDate = futureDate(35);
        String endDate = futureDate(30);  // Earlier than startDate
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + startDate + "\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + endDate + "\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        try {
            handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown APIException");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("End date/time must be after start date/time"));
        }
    }

    @Test
    public void shouldReturnBadRequestForBatchWithOneInvalidElement() throws Exception {
        String date1 = futureDate(32);
        String date2 = futureDate(33);
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date1 + "\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + date1 + "\",\"endTime\":\"17:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date2 + "\",\"startTime\":\"17:00\"," +
                "\"endDate\":\"" + date2 + "\",\"endTime\":\"09:00\"}" + // Invalid: end before start
                "]";

        try {
            handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown APIException");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("End date/time must be after start date/time"));
        }

        AppointmentUnavailabilitySearchParams searchParams = new AppointmentUnavailabilitySearchParams();
        List<AppointmentUnavailability> allBlocks = service.getAll(searchParams);
        assertEquals(5, allBlocks.size());
    }

    @Test
    public void shouldGetAllUnavailabilityBlocks() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability"));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertTrue(fetchedUnavailabilityBlocks.length > 0);
    }

    @Test
    public void shouldFilterByLocationUuid() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1")));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertEquals(4, fetchedUnavailabilityBlocks.length); // 4 non-voided blocks for location 1
    }

    @Test
    public void shouldFilterByServiceUuid() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("serviceUuid", "fff006e5-9fbb-4f20-866b-0ece245615a1")));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertEquals(2, fetchedUnavailabilityBlocks.length);
    }

    @Test
    public void shouldFilterByDateRange() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("startDate", "2099-08-05"),
                new Parameter("endDate", "2099-08-07")));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertEquals(3, fetchedUnavailabilityBlocks.length);
    }

    @Test
    public void shouldIncludeVoidedWhenRequested() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("includeVoided", "true")));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertEquals(5, fetchedUnavailabilityBlocks.length);
    }

    @Test
    public void shouldRespectLimit() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("limit", "2")));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse[] fetchedUnavailabilityBlocks = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse[].class);

        assertNotNull(fetchedUnavailabilityBlocks);
        assertEquals(2, fetchedUnavailabilityBlocks.length);
    }

    @Test
    public void shouldGetByUuid() throws Exception {
        String uuid = "uuu006e5-9fbb-4f20-866b-0ece245615a1";
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability/" + uuid));
        assertEquals(200, response.getStatus());

        AppointmentUnavailabilityResponse fetchedBlock = 
                objectMapper.readValue(response.getContentAsString(), AppointmentUnavailabilityResponse.class);
        
        assertNotNull(fetchedBlock);
        assertEquals(uuid, fetchedBlock.getUuid());
        assertNotNull(fetchedBlock.getLocation());
        assertEquals("aaa006e5-9fbb-4f20-866b-0ece245615a1", fetchedBlock.getLocation().getUuid());
        assertEquals("Test Location 1", fetchedBlock.getLocation().getName());
    }

    @Test
    public void shouldReturn404WhenUuidNotFound() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability/non-existent-uuid"));
        assertEquals(404, response.getStatus());
        assertTrue(response.getContentAsString().contains("not found"));
    }

    @Test
    public void shouldVoidUnavailabilityBlock() throws Exception {
        String uuid = "vvv006e5-9fbb-4f20-866b-0ece245615a2";

        AppointmentUnavailability beforeVoid = service.getByUuid(uuid);
        assertFalse(beforeVoid.getVoided());

        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/" + uuid,
                new Parameter("voidReason", "Test void reason")));
        assertEquals(204, response.getStatus());

        AppointmentUnavailability afterVoid = service.getByUuid(uuid);
        assertTrue(afterVoid.getVoided());
        assertEquals("Test void reason", afterVoid.getVoidReason());
    }

    @Test
    public void shouldBeIdempotentWhenVoidingAlreadyVoidedBlock() throws Exception {
        String uuid = "yyy006e5-9fbb-4f20-866b-0ece245615a5"; // Already voided in test data

        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/" + uuid,
                new Parameter("voidReason", "Another reason")));
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldReturn404WhenVoidingNonExistentBlock() throws Exception {
        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/non-existent-uuid",
                new Parameter("voidReason", "Test")));
        assertEquals(404, response.getStatus());
        assertTrue(response.getContentAsString().contains("not found"));
    }

    @Test
    public void shouldDeleteOneBlockFromBatch() throws Exception {
        String date1 = futureDate(45);
        String date2 = futureDate(46);
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date1 + "\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + date1 + "\",\"endTime\":\"17:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"" + date2 + "\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"" + date2 + "\",\"endTime\":\"17:00\"}" +
                "]";

        MockHttpServletResponse createResponse = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, createResponse.getStatus());

        AppointmentUnavailabilityResponse[] createdUnavailabilityBlocks = 
                objectMapper.readValue(createResponse.getContentAsString(), AppointmentUnavailabilityResponse[].class);
        assertEquals(2, createdUnavailabilityBlocks.length);

        AppointmentUnavailabilityResponse firstCreatedBlock = createdUnavailabilityBlocks[0];
        String firstBlockUuid = firstCreatedBlock.getUuid();

        MockHttpServletResponse deleteResponse = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/" + firstBlockUuid,
                new Parameter("voidReason", "Test delete")));
        assertEquals(204, deleteResponse.getStatus());

        AppointmentUnavailability voidedBlock = service.getByUuid(firstBlockUuid);
        assertTrue(voidedBlock.getVoided());

        AppointmentUnavailabilityResponse secondCreatedBlock = createdUnavailabilityBlocks[1];
        String secondBlockUuid = secondCreatedBlock.getUuid();
        AppointmentUnavailability notVoidedBlock = service.getByUuid(secondBlockUuid);
        assertFalse(notVoidedBlock.getVoided());
    }
}
