package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentUnavailability;
import org.openmrs.module.appointments.service.AppointmentUnavailabilityService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AppointmentUnavailabilityControllerIT extends BaseIntegrationTest {

    @Autowired
    private AppointmentUnavailabilityController controller;

    @Autowired
    private AppointmentUnavailabilityService service;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentUnavailabilityTestData.xml");
    }

    @Test
    public void shouldCreateSingleUnavailabilityBlock() throws Exception {
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-10\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-10\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(1, result.size());

        SimpleObject block = SimpleObject.parseJson(objectMapper.writeValueAsString(result.get(0)));
        assertNotNull(block.get("uuid"));
        assertEquals("aaa006e5-9fbb-4f20-866b-0ece245615a1", block.get("locationUuid"));
        assertEquals("Test Location 1", block.get("locationName"));
        assertEquals("2026-08-10", block.get("startDate"));
        assertEquals("09:00", block.get("startTime"));
        assertEquals("2026-08-10", block.get("endDate"));
        assertEquals("17:00", block.get("endTime"));
        assertNull(block.get("appointmentServiceUuid"));
        assertNull(block.get("providerUuid"));
    }

    @Test
    public void shouldCreateBatchOfFourBlocks() throws Exception {
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"fff006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"providerUuid\":\"ccc006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-12\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"2026-08-12\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"fff006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"providerUuid\":\"ddd006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"startDate\":\"2026-08-12\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"2026-08-12\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"ggg006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"providerUuid\":\"ccc006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-12\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"2026-08-12\",\"endTime\":\"19:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":\"ggg006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"providerUuid\":\"ddd006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"startDate\":\"2026-08-12\",\"startTime\":\"07:00\"," +
                "\"endDate\":\"2026-08-12\",\"endTime\":\"19:00\"}" +
                "]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(4, result.size());

        SimpleObject block = SimpleObject.parseJson(objectMapper.writeValueAsString(result.get(0)));
        assertEquals("fff006e5-9fbb-4f20-866b-0ece245615a1", block.get("appointmentServiceUuid"));
        assertEquals("Test Service 1", block.get("appointmentServiceName"));
        assertEquals("ccc006e5-9fbb-4f20-866b-0ece245615a1", block.get("providerUuid"));
    }

    @Test
    public void shouldCreateBlockWithServiceAndProviderAsNull() throws Exception {
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentServiceUuid\":null," +
                "\"providerUuid\":null," +
                "\"startDate\":\"2026-08-11\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-11\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertEquals(1, result.size());
        SimpleObject block = SimpleObject.parseJson(objectMapper.writeValueAsString(result.get(0)));
        assertNull(block.get("appointmentServiceUuid"));
        assertNull(block.get("providerUuid"));
    }

    @Test
    public void shouldReturnBadRequestForEmptyArray() throws Exception {
        String dataJson = "[]";

        try {
            MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("at least one unavailability block"));
        }
    }

    @Test
    public void shouldReturnBadRequestWhenEndDateBeforeStartDate() throws Exception {
        String dataJson = "[{" +
                "\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-10\"," +
                "\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-05\"," +
                "\"endTime\":\"17:00\"" +
                "}]";

        try {
            MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("endDate cannot be before startDate"));
        }
    }

    @Test
    public void shouldReturnBadRequestForBatchWithOneInvalidElement() throws Exception {
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-12\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-12\",\"endTime\":\"17:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-13\",\"startTime\":\"17:00\"," +
                "\"endDate\":\"2026-08-13\",\"endTime\":\"09:00\"}" + // Invalid: end before start
                "]";

        try {
            MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("endTime must be after startTime when dates are the same"));
        }

        List<AppointmentUnavailability> all = service.getAll(null, null, null, null, null, false, null);
        assertEquals(5, all.size());
    }

    @Test
    public void shouldGetAllUnavailabilityBlocks() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability"));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    public void shouldFilterByLocationUuid() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1")));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(4, result.size()); // 4 non-voided blocks for location 1
    }

    @Test
    public void shouldFilterByServiceUuid() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("serviceUuid", "fff006e5-9fbb-4f20-866b-0ece245615a1")));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void shouldFilterByDateRange() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("startDate", "2026-08-05"),
                new Parameter("endDate", "2026-08-07")));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    public void shouldIncludeVoidedWhenRequested() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("includeVoided", "true")));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test
    public void shouldRespectLimit() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability",
                new Parameter("locationUuid", "aaa006e5-9fbb-4f20-866b-0ece245615a1"),
                new Parameter("limit", "2")));
        assertEquals(200, response.getStatus());

        String content = response.getContentAsString();
        ArrayList result = objectMapper.readValue(content, ArrayList.class);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void shouldGetByUuid() throws Exception {
        String uuid = "uuu006e5-9fbb-4f20-866b-0ece245615a1";
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentUnavailability/" + uuid));
        assertEquals(200, response.getStatus());

        SimpleObject result = SimpleObject.parseJson(response.getContentAsString());
        assertNotNull(result);
        assertEquals(uuid, result.get("uuid"));
        assertEquals("aaa006e5-9fbb-4f20-866b-0ece245615a1", result.get("locationUuid"));
        assertEquals("Test Location 1", result.get("locationName"));
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

        AppointmentUnavailability before = service.getByUuid(uuid);
        assertFalse(before.getVoided());

        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/" + uuid,
                new Parameter("voidReason", "Test void reason")));
        assertEquals(204, response.getStatus());

        AppointmentUnavailability after = service.getByUuid(uuid);
        assertTrue(after.getVoided());
        assertEquals("Test void reason", after.getVoidReason());
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
        String dataJson = "[" +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-15\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-15\",\"endTime\":\"17:00\"}," +
                "{\"locationUuid\":\"aaa006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"startDate\":\"2026-08-16\",\"startTime\":\"09:00\"," +
                "\"endDate\":\"2026-08-16\",\"endTime\":\"17:00\"}" +
                "]";

        MockHttpServletResponse createResponse = handle(newPostRequest("/rest/v1/appointmentUnavailability", dataJson));
        assertEquals(200, createResponse.getStatus());

        String content = createResponse.getContentAsString();
        ArrayList created = objectMapper.readValue(content, ArrayList.class);
        assertEquals(2, created.size());

        SimpleObject block1 = SimpleObject.parseJson(objectMapper.writeValueAsString(created.get(0)));
        String uuid1 = (String) block1.get("uuid");

        MockHttpServletResponse deleteResponse = handle(newDeleteRequest("/rest/v1/appointmentUnavailability/" + uuid1,
                new Parameter("voidReason", "Test delete")));
        assertEquals(204, deleteResponse.getStatus());

        AppointmentUnavailability voided = service.getByUuid(uuid1);
        assertTrue(voided.getVoided());

        SimpleObject block2 = SimpleObject.parseJson(objectMapper.writeValueAsString(created.get(1)));
        String uuid2 = (String) block2.get("uuid");
        AppointmentUnavailability notVoided = service.getByUuid(uuid2);
        assertFalse(notVoided.getVoided());
    }
}
