package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceAttributeResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.*;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentServiceControllerIT extends BaseIntegrationTest {
    @Autowired
    AppointmentServiceController appointmentServiceController;

    @Autowired
    AppointmentServiceDefinitionService appointmentServiceDefinitionService;

    @Autowired
    AppointmentsService appointmentsService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServicesTestData.xml");
    }

    @Test
    public void should_createAppointmentService() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"maxAppointmentsLimit\":\"30\"," +
                "\"color\":\"#00ff00\"" +
                "}";

        MockHttpServletResponse handle = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject asResponse = SimpleObject.parseJson(handle.getContentAsString());
        assertNotNull(asResponse);
        assertEquals("Cardiology Consultation", asResponse.get("name"));
        assertEquals("09:00:00", asResponse.get("startTime"));
        assertEquals("17:30:00", asResponse.get("endTime"));
        assertEquals(30, (int) asResponse.get("durationMins"));
        assertEquals(30, (int) asResponse.get("maxAppointmentsLimit"));
        assertEquals("#00ff00", asResponse.get("color"));
        assertNotNull(asResponse.get("weeklyAvailability"));
        assertEquals(0, ((ArrayList)asResponse.get("weeklyAvailability")).size());
    }

    @Test
    public void should_createAppointmentServiceOnlyWithName() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Consultation\"}";

        MockHttpServletResponse handle = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject asResponse = SimpleObject.parseJson(handle.getContentAsString());
        assertNotNull(asResponse);
        assertEquals("Cardiology Consultation", asResponse.get("name"));
        assertNotNull(asResponse.get("uuid"));
        assertNull(asResponse.get("durationMins"));
        assertNull(asResponse.get("maxAppointmentsLimit"));
        assertNotNull(asResponse.get("weeklyAvailability"));
        assertEquals(0, ((ArrayList)asResponse.get("weeklyAvailability")).size());
    }

    @Test
    public void should_createAppointmentServiceWithServiceAvailability() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"maxAppointmentsLimit\":\"30\"," +
                "\"color\":\"#0000ff\"," +
                "\"weeklyAvailability\": [{ \"dayOfWeek\": \"MONDAY\", \"startTime\":\"09:00:00\", \"endTime\":\"17:30:00\", \"maxAppointmentsLimit\":\"10\" }]" +
                "}";

        MockHttpServletResponse handle = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject asResponse = SimpleObject.parseJson(handle.getContentAsString());
        assertNotNull(asResponse);
        assertEquals("Cardiology Consultation", asResponse.get("name"));
        assertEquals("09:00:00", asResponse.get("startTime"));
        assertEquals("17:30:00", asResponse.get("endTime"));
        assertEquals(30, (int)asResponse.get("durationMins"));
        assertEquals(30, (int)asResponse.get("maxAppointmentsLimit"));
        assertEquals("#0000ff", asResponse.get("color"));
        ArrayList weeklyAvailabilities = (ArrayList) asResponse.get("weeklyAvailability");
        assertNotNull(weeklyAvailabilities);
        LinkedHashMap<String, Object> weeklyAvailability = (LinkedHashMap<String, Object>) (weeklyAvailabilities.get(0));
        assertNotNull(weeklyAvailability);
        assertEquals("MONDAY", weeklyAvailability.get("dayOfWeek"));
        assertEquals("09:00:00", weeklyAvailability.get("startTime"));
        assertEquals("17:30:00", weeklyAvailability.get("endTime"));
        assertEquals(10, weeklyAvailability.get("maxAppointmentsLimit"));
    }

    @Test
    public void shouldCreateAppointmentServiceWithServiceTypes() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
            "\"endTime\":\"17:30:00\"," +
            "\"durationMins\":\"30\"," +
            "\"color\":\"#fff000\"," +
            "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
            "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
            "\"serviceTypes\": [{ \"name\": \"type1\", \"duration\":\"20\"}]" +
            "}";

        MockHttpServletResponse handle = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject response = SimpleObject.parseJson(handle.getContentAsString());
        assertNotNull(response);
        ArrayList serviceTypes = (ArrayList) response.get("serviceTypes");
        assertNotNull(serviceTypes);
        LinkedHashMap<String, Object> serviceTypes1 = (LinkedHashMap<String, Object>) serviceTypes.get(0);

        assertNotNull(serviceTypes1.get("uuid"));
        assertEquals("type1", serviceTypes1.get("name"));
        assertEquals(20, serviceTypes1.get("duration"));
    }

    @Test
    public void should_createAppointmentServiceWithInitialAppointmentStatus() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Consultation\"," +
                " \"initialAppointmentStatus\":\"Requested\"}";

        MockHttpServletResponse handle = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject asResponse = SimpleObject.parseJson(handle.getContentAsString());
        assertNotNull(asResponse);
        assertEquals("Cardiology Consultation", asResponse.get("name"));
        assertEquals(AppointmentStatus.Requested.toString(), asResponse.get("initialAppointmentStatus"));
    }


    @Test(expected = RuntimeException.class)
    public void should_notCreateAppointmentServiceWhenNameIsNull() throws Exception {
        String dataJson = "{}";
        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test
    public void should_GetAllAppointmentServices() throws Exception {
        List<AppointmentServiceDefaultResponse> asResponses = deserialize(handle(newGetRequest("/rest/v1/appointmentService/all/default")), new TypeReference<List<AppointmentServiceDefaultResponse>>() {});
        assertEquals(3,asResponses.size());
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", asResponses.get(0).getUuid());
        assertEquals("Consultation", asResponses.get(0).getName());
        assertEquals("Consultation", asResponses.get(0).getDescription());
        assertEquals("09:00:00", asResponses.get(0).getStartTime());
        assertEquals("17:00:00", asResponses.get(0).getEndTime());
        assertEquals(30, asResponses.get(0).getDurationMins().intValue());
        assertEquals(4, asResponses.get(0).getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponses.get(0).getSpeciality().get("name"));
        assertEquals("Room1", asResponses.get(0).getLocation().get("name"));
        assertEquals("c36006d4-9fbb-4f20-866b-0ece24560000", asResponses.get(1).getUuid());
        assertEquals("Ortho Service", asResponses.get(1).getName());
        assertEquals("09:00:00", asResponses.get(1).getStartTime());
        assertEquals("16:00:00", asResponses.get(1).getEndTime());
        assertEquals(30, asResponses.get(1).getDurationMins().intValue());
        assertEquals(4, asResponses.get(1).getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponses.get(1).getSpeciality().get("name"));
        assertEquals("Room1", asResponses.get(1).getLocation().get("name"));
    }

    @Test
    public void should_GetServiceByUuid() throws Exception {
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a6"))), new TypeReference<AppointmentServiceFullResponse>() {});
        assertNotNull(asResponse);
        assertEquals("Consultation", asResponse.getName());
        assertEquals("Consultation", asResponse.getDescription());
        assertEquals("09:00:00", asResponse.getStartTime());
        assertEquals("17:00:00", asResponse.getEndTime());
        assertEquals(30, asResponse.getDurationMins().intValue());
        assertEquals(4, asResponse.getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponse.getSpeciality().get("name"));
        assertEquals("Room1", asResponse.getLocation().get("name"));
        assertNotNull(asResponse.getWeeklyAvailability());
        assertEquals(1, asResponse.getWeeklyAvailability().size());
        assertEquals("MONDAY", ((Map)asResponse.getWeeklyAvailability().get(0)).get("dayOfWeek"));
        assertNotNull(asResponse.getServiceTypes());
        assertEquals(1, asResponse.getServiceTypes().size());
        assertEquals("c36006d5-9fcc-4f20-866b-0ece245615b1", ((Map)asResponse.getServiceTypes().get(0)).get("uuid"));
    }

    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceAvailabilityAndServiceTypes() throws Exception {
        String appointmentServiceUuid = "c36006d4-9fbb-4f20-866b-0ece24560000";
        Parameter uuid = new Parameter("uuid", appointmentServiceUuid);
        Parameter voidReason = new Parameter("void_reason", "webservice call");
        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentService", uuid, voidReason));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        SimpleObject responseObject = SimpleObject.parseJson(response.getContentAsString());
        assertNotNull(responseObject);
        assertEquals(appointmentServiceUuid, responseObject.get("uuid"));
    }

    @Test
    public void shouldVoidTheAppointmentServiceAlongWithServiceAvailabilityAndServiceTypesWithoutVoidReason() throws Exception {
        String appointmentServiceUuid = "c36006d4-9fbb-4f20-866b-0ece24560000";
        Parameter uuid = new Parameter("uuid", appointmentServiceUuid);
        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentService", uuid));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        SimpleObject responseObject = SimpleObject.parseJson(response.getContentAsString());
        assertNotNull(responseObject);
        assertEquals(appointmentServiceUuid, responseObject.get("uuid"));
    }

    @Test
    public void shouldTheDeleteServiceBeIdempotent() throws Exception {
        String voidedServiceUuid = "c36006d4-9fbb-4f20-866b-0ece245615b1";
        Parameter uuid = new Parameter("uuid", voidedServiceUuid);
        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentService", uuid));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        SimpleObject responseObject = SimpleObject.parseJson(response.getContentAsString());
        assertNotNull(responseObject);
        assertEquals(voidedServiceUuid, responseObject.get("uuid"));

    }

    @Test
    public void shouldThrowAnExceptionWhenThereAreFutureAppointmentsForAServiceAndTryingToDeleteService() throws Exception {
        Parameter uuid = new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a6");
        Parameter voidReason = new Parameter("void_reason", "webservice call");

        MockHttpServletResponse response = handle(newDeleteRequest("/rest/v1/appointmentService", uuid, voidReason));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        SimpleObject responseObject = SimpleObject.parseJson(response.getContentAsString());
        assertNotNull(responseObject);
        assertEquals(
                "Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it",
                responseObject.get("message"));
    }

    @Test
    public void shouldUpdateService() throws Exception {
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertNotNull(appointmentServiceDefinition);
        String uuid = appointmentServiceDefinition.getUuid();
        String dataJson = "{\"name\":\"Chemotherapy\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"uuid\":\""+ uuid +"\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"serviceTypes\": [{ \"name\": \"stage 1\", \"duration\":\"20\", \"uuid\":\"c36006d5-9fcc-4f20-866b-0ece245615b1\" }]" +
                "}";
        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a6");
        assertEquals("Chemotherapy", appointmentServiceDefinition.getName());
        assertEquals(uuid, appointmentServiceDefinition.getUuid());
        assertEquals(1, appointmentServiceDefinition.getServiceTypes().size());
    }

    @Test
    public void shouldGetCurrentLoadForServiceForGivenDateTimes() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment Service does not exist");
        deserialize(handle(newGetRequest("/rest/v1/appointmentService/load",
                new Parameter("uuid", "c36006d4-9fbb-4f20-866b-0ece245625b4"),
                new Parameter("startDateTime", "2108-08-14T18:30:00.0Z"), new Parameter("endDateTime", "2108-08-15T18:29:29.0Z"))),
                new TypeReference<Integer>() {});

    }

    @Test
    public void should_GetAllAppointmentServicesWithNonVoidedServiceTypes() throws Exception {
        List<AppointmentServiceFullResponse> asResponses = deserialize(handle(newGetRequest("/rest/v1/appointmentService/all/full")), new TypeReference<List<AppointmentServiceFullResponse>>() {});
        assertEquals(3,asResponses.size());
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", asResponses.get(0).getUuid());
        assertEquals("Consultation", asResponses.get(0).getName());
        assertEquals("Consultation", asResponses.get(0).getDescription());
        assertEquals("09:00:00", asResponses.get(0).getStartTime());
        assertEquals("17:00:00", asResponses.get(0).getEndTime());
        assertEquals(30, asResponses.get(0).getDurationMins().intValue());
        assertEquals(4, asResponses.get(0).getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponses.get(0).getSpeciality().get("name"));
        assertEquals("Room1", asResponses.get(0).getLocation().get("name"));
        ArrayList serviceTypes = (ArrayList) asResponses.get(0).getServiceTypes();
        assertNotNull(serviceTypes);
        LinkedHashMap<String, Object> serviceTypes1 = (LinkedHashMap<String, Object>) serviceTypes.get(0);
        assertEquals("Initial Consultation", serviceTypes1.get("name"));
        assertEquals("c36006d4-9fbb-4f20-866b-0ece24560000", asResponses.get(1).getUuid());
        assertEquals("Ortho Service", asResponses.get(1).getName());
        assertEquals("09:00:00", asResponses.get(1).getStartTime());
        assertEquals("16:00:00", asResponses.get(1).getEndTime());
        assertEquals(30, asResponses.get(1).getDurationMins().intValue());
        assertEquals(4, asResponses.get(1).getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponses.get(1).getSpeciality().get("name"));
        assertEquals("Room1", asResponses.get(1).getLocation().get("name"));
    }

    @Test
    public void shouldThrowErrorWhenAppointmentServiceIsNotAvailable() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment Service does not exist");
        MockHttpServletResponse asResponse = handle(newGetRequest("/rest/v1/appointmentService", new Parameter("uuid", "b123406d4-9fbb-4f20-866b-0ece245615a1")));
    }

    @Test
    public void should_returnAttributesInDefaultResponse() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentService/all/default"));
        List<AppointmentServiceDefaultResponse> services = deserialize(response, new TypeReference<List<AppointmentServiceDefaultResponse>>() {});

        assertNotNull(services);

        AppointmentServiceDefaultResponse service = services.stream()
                .filter(s -> s.getName().equals("Consultation"))
                .findFirst().orElse(null);

        assertNotNull("Consultation service should exist", service);
        assertNotNull("Service should have attributes", service.getAttributes());
        assertEquals("Should have 2 non-voided attributes", 2, service.getAttributes().size());

        AppointmentServiceAttributeResponse feeAttr = service.getAttributes().stream()
                .filter(a -> a.getAttributeType().equals("Consultation Fee"))
                .findFirst().orElse(null);
        assertNotNull("Consultation Fee attribute should exist", feeAttr);
        assertEquals("Consultation Fee", feeAttr.getAttributeType());
        assertEquals("d36006e5-9fbb-4f20-866b-0ece245615a1", feeAttr.getAttributeTypeUuid());
        assertEquals("500", feeAttr.getValue());
        assertEquals("e36006e5-9fbb-4f20-866b-0ece245615a1", feeAttr.getUuid());

        AppointmentServiceAttributeResponse roomAttr = service.getAttributes().stream()
                .filter(a -> a.getAttributeType().equals("Room Number"))
                .findFirst().orElse(null);
        assertNotNull("Room Number attribute should exist", roomAttr);
        assertEquals("Room Number", roomAttr.getAttributeType());
        assertEquals("d36006e5-9fbb-4f20-866b-0ece245615a2", roomAttr.getAttributeTypeUuid());
        assertEquals("Room 101", roomAttr.getValue());
        assertEquals("e36006e5-9fbb-4f20-866b-0ece245615a2", roomAttr.getUuid());
    }

    @Test
    public void should_notReturnVoidedAttributes() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentService/all/default"));
        List<AppointmentServiceDefaultResponse> services = deserialize(response, new TypeReference<List<AppointmentServiceDefaultResponse>>() {});

        assertNotNull(services);

        AppointmentServiceDefaultResponse service = services.stream()
                .filter(s -> s.getName().equals("Consultation"))
                .findFirst().orElse(null);

        assertNotNull("Consultation service should exist", service);
        assertNotNull("Service should have attributes", service.getAttributes());

        boolean hasVoidedAttribute = service.getAttributes().stream()
                .anyMatch(a -> a.getUuid().equals("e36006e5-9fbb-4f20-866b-0ece245615a3"));

        assertEquals("Voided attribute should not be in response", false, hasVoidedAttribute);
        assertEquals("Should only have 2 non-voided attributes", 2, service.getAttributes().size());
    }

    @Test
    public void should_returnAttributesInFullResponse() throws Exception {
        MockHttpServletResponse response = handle(newGetRequest("/rest/v1/appointmentService/all/full"));
        List<AppointmentServiceFullResponse> services = deserialize(response, new TypeReference<List<AppointmentServiceFullResponse>>() {});

        assertNotNull(services);

        AppointmentServiceFullResponse service = services.stream()
                .filter(s -> s.getName().equals("Consultation"))
                .findFirst().orElse(null);

        assertNotNull("Consultation service should exist", service);
        assertNotNull("Full response should include attributes (inherited from default)", service.getAttributes());
        assertEquals("Should have 2 non-voided attributes", 2, service.getAttributes().size());
    }

    @Test
    public void should_returnAttributesForSingleService() throws Exception {
        AppointmentServiceFullResponse service = deserialize(
                handle(newGetRequest("/rest/v1/appointmentService",
                    new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a6"))),
                new TypeReference<AppointmentServiceFullResponse>() {});

        assertNotNull(service);
        assertEquals("Consultation", service.getName());
        assertNotNull("Service should have attributes", service.getAttributes());
        assertEquals("Should have 2 non-voided attributes", 2, service.getAttributes().size());

        AppointmentServiceAttributeResponse feeAttr = service.getAttributes().stream()
                .filter(a -> a.getAttributeType().equals("Consultation Fee"))
                .findFirst().orElse(null);
        assertNotNull("Consultation Fee attribute should exist", feeAttr);
        assertEquals("500", feeAttr.getValue());
    }

    @Test
    public void should_createServiceWithAttributes() throws Exception {
        String dataJson = "{\"name\":\"Cardiology Service\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a1\", \"value\":\"600\"}," +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a2\", \"value\":\"Room 202\"}" +
                "]}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject serviceResponse = SimpleObject.parseJson(response.getContentAsString());

        assertNotNull(serviceResponse);
        assertEquals("Cardiology Service", serviceResponse.get("name"));
        assertNotNull(serviceResponse.get("uuid"));

        ArrayList attributes = (ArrayList) serviceResponse.get("attributes");
        assertNotNull("Service should have attributes", attributes);
        assertEquals("Should have 2 attributes", 2, attributes.size());

        LinkedHashMap<String, Object> attr1 = (LinkedHashMap<String, Object>) attributes.get(0);
        assertEquals("Consultation Fee", attr1.get("attributeType"));
        assertEquals("600", attr1.get("value"));
        assertNotNull(attr1.get("uuid"));

        LinkedHashMap<String, Object> attr2 = (LinkedHashMap<String, Object>) attributes.get(1);
        assertEquals("Room Number", attr2.get("attributeType"));
        assertEquals("Room 202", attr2.get("value"));
        assertNotNull(attr2.get("uuid"));
    }

    @Test
    public void should_updateServiceWithNewAttributes() throws Exception {
        String existingServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";

        AppointmentServiceFullResponse beforeUpdate = deserialize(
                handle(newGetRequest("/rest/v1/appointmentService",
                    new Parameter("uuid", existingServiceUuid))),
                new TypeReference<AppointmentServiceFullResponse>() {});

        int initialAttributeCount = beforeUpdate.getAttributes() != null ? beforeUpdate.getAttributes().size() : 0;

        String dataJson = "{\"name\":\"Consultation\"," +
                "\"uuid\":\"" + existingServiceUuid + "\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a3\", \"value\":\"Special instructions here\"}" +
                "]}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject serviceResponse = SimpleObject.parseJson(response.getContentAsString());

        assertNotNull(serviceResponse);
        assertEquals(existingServiceUuid, serviceResponse.get("uuid"));

        ArrayList attributes = (ArrayList) serviceResponse.get("attributes");
        assertNotNull("Service should have attributes", attributes);

        boolean hasNewAttribute = false;
        for (Object attr : attributes) {
            LinkedHashMap<String, Object> attrMap = (LinkedHashMap<String, Object>) attr;
            if ("Special Instructions".equals(attrMap.get("attributeType"))) {
                hasNewAttribute = true;
                assertEquals("Special instructions here", attrMap.get("value"));
            }
        }
        assertEquals("Should have new attribute", true, hasNewAttribute);
    }

    @Test
    public void should_updateExistingAttribute() throws Exception {
        String existingServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        String existingAttributeUuid = "e36006e5-9fbb-4f20-866b-0ece245615a1";

        String dataJson = "{\"name\":\"Consultation\"," +
                "\"uuid\":\"" + existingServiceUuid + "\"," +
                "\"attributes\": [" +
                "{\"uuid\":\"" + existingAttributeUuid + "\"," +
                " \"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                " \"value\":\"750\"}" +
                "]}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        SimpleObject serviceResponse = SimpleObject.parseJson(response.getContentAsString());

        assertNotNull(serviceResponse);
        assertEquals(existingServiceUuid, serviceResponse.get("uuid"));

        ArrayList attributes = (ArrayList) serviceResponse.get("attributes");
        assertNotNull("Service should have attributes", attributes);

        boolean foundUpdated = false;
        for (Object attr : attributes) {
            LinkedHashMap<String, Object> attrMap = (LinkedHashMap<String, Object>) attr;
            if (existingAttributeUuid.equals(attrMap.get("uuid"))) {
                foundUpdated = true;
                assertEquals("Value should be updated to 750", "750", attrMap.get("value"));
            }
        }
        assertEquals("Should find and verify updated attribute", true, foundUpdated);
    }

    @Test
    public void should_voidAttribute() throws Exception {
        String existingServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        String existingAttributeUuid = "e36006e5-9fbb-4f20-866b-0ece245615a2";

        AppointmentServiceFullResponse beforeVoid = deserialize(
                handle(newGetRequest("/rest/v1/appointmentService",
                    new Parameter("uuid", existingServiceUuid))),
                new TypeReference<AppointmentServiceFullResponse>() {});

        int initialCount = beforeVoid.getAttributes().size();

        String dataJson = "{\"name\":\"Consultation\"," +
                "\"uuid\":\"" + existingServiceUuid + "\"," +
                "\"attributes\": [" +
                "{\"uuid\":\"" + existingAttributeUuid + "\"," +
                " \"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a2\"," +
                " \"value\":\"Room 101\"," +
                " \"voided\":true," +
                " \"voidReason\":\"No longer needed\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));

        AppointmentServiceFullResponse afterVoid = deserialize(
                handle(newGetRequest("/rest/v1/appointmentService",
                    new Parameter("uuid", existingServiceUuid))),
                new TypeReference<AppointmentServiceFullResponse>() {});

        boolean hasVoidedAttribute = afterVoid.getAttributes().stream()
                .anyMatch(a -> a.getUuid().equals(existingAttributeUuid));

        assertEquals("Voided attribute should not be in response", false, hasVoidedAttribute);
        assertEquals("Should have one less attribute", initialCount - 1, afterVoid.getAttributes().size());
    }

    @Test
    public void should_handleMixOfNewUpdatedAndVoidedAttributes() throws Exception {
        String dataJson1 = "{\"name\":\"Mixed Attribute Service\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a1\", \"value\":\"100\"}" +
                "]}";

        MockHttpServletResponse response1 = handle(newPostRequest("/rest/v1/appointmentService", dataJson1));
        SimpleObject serviceResponse1 = SimpleObject.parseJson(response1.getContentAsString());
        String serviceUuid = (String) serviceResponse1.get("uuid");
        ArrayList initialAttributes = (ArrayList) serviceResponse1.get("attributes");
        String firstAttributeUuid = (String) ((LinkedHashMap<String, Object>) initialAttributes.get(0)).get("uuid");

        String dataJson2 = "{\"name\":\"Mixed Attribute Service\"," +
                "\"uuid\":\"" + serviceUuid + "\"," +
                "\"attributes\": [" +
                "{\"uuid\":\"" + firstAttributeUuid + "\"," +
                " \"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                " \"value\":\"200\"}," +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a2\", \"value\":\"New Room\"}," +
                "{\"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a3\"," +
                " \"value\":\"Void me\"," +
                " \"voided\":true," +
                " \"voidReason\":\"Test voiding\"}" +
                "]}";

        MockHttpServletResponse response2 = handle(newPostRequest("/rest/v1/appointmentService", dataJson2));
        SimpleObject serviceResponse2 = SimpleObject.parseJson(response2.getContentAsString());

        ArrayList finalAttributes = (ArrayList) serviceResponse2.get("attributes");
        assertEquals("Should have 2 non-voided attributes", 2, finalAttributes.size());

        boolean foundUpdated = false;
        boolean foundNew = false;
        for (Object attr : finalAttributes) {
            LinkedHashMap<String, Object> attrMap = (LinkedHashMap<String, Object>) attr;
            if (firstAttributeUuid.equals(attrMap.get("uuid"))) {
                foundUpdated = true;
                assertEquals("First attribute should be updated", "200", attrMap.get("value"));
            }
            if ("Room Number".equals(attrMap.get("attributeType"))) {
                foundNew = true;
                assertEquals("New attribute should exist", "New Room", attrMap.get("value"));
            }
        }

        assertEquals("Should find updated attribute", true, foundUpdated);
        assertEquals("Should find new attribute", true, foundNew);
    }

    @Test(expected = RuntimeException.class)
    public void should_throwErrorForInvalidAttributeTypeUuid() throws Exception {
        String dataJson = "{\"name\":\"Service With Invalid Attribute\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"invalid-uuid-12345\", \"value\":\"test\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test(expected = RuntimeException.class)
    public void should_throwErrorForNonExistentAttributeUuid() throws Exception {
        String dataJson = "{\"name\":\"Test Service\"," +
                "\"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"attributes\": [" +
                "{\"uuid\":\"non-existent-attribute-uuid\"," +
                " \"attributeTypeUuid\":\"d36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                " \"value\":\"test\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test(expected = RuntimeException.class)
    public void should_throwErrorWhenMinOccursNotSatisfied() throws Exception {
        String dataJson = "{\"name\":\"New Service Without Required Attribute\"," +
                "\"durationMins\":30," +
                "\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:00:00\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"d7477c21-444f-4ff0-a48f-b87b61c4b8a8\"," +
                " \"value\":\"DEPT-999\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test(expected = RuntimeException.class)
    public void should_throwErrorWhenMaxOccursExceeded() throws Exception {
        String dataJson = "{\"name\":\"Service With Too Many Attributes\"," +
                "\"durationMins\":30," +
                "\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:00:00\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"attributes\": [" +
                "{\"attributeTypeUuid\":\"e8588d22-555g-5gg1-b59g-c98c72d5c9b9\"," +
                " \"value\":\"Category 1\"}," +
                "{\"attributeTypeUuid\":\"e8588d22-555g-5gg1-b59g-c98c72d5c9b9\"," +
                " \"value\":\"Category 2\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test(expected = RuntimeException.class)
    public void should_throwErrorWhenTryingToUpdateVoidedAttribute() throws Exception {
        String existingServiceUuid = "c36006e5-9fbb-4f20-866b-0ece245615a6";
        String voidedAttributeUuid = "d4567890-4444-4f20-866b-0ece245615d4";

        String dataJson = "{\"name\":\"Consultation\"," +
                "\"uuid\":\"" + existingServiceUuid + "\"," +
                "\"attributes\": [" +
                "{\"uuid\":\"" + voidedAttributeUuid + "\"," +
                " \"attributeTypeUuid\":\"d7477c21-444f-4ff0-a48f-b87b61c4b8a8\"," +
                " \"value\":\"Updated Value\"}" +
                "]}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }
}
