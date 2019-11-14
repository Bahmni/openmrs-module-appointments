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
        assertEquals(30, asResponse.get("durationMins"));
        assertEquals(30, asResponse.get("maxAppointmentsLimit"));
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
        assertEquals(30, asResponse.get("durationMins"));
        assertEquals(30, asResponse.get("maxAppointmentsLimit"));
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
        assertEquals("c36006d4-9fbb-4f20-866b-0ece245615a1", asResponses.get(0).getUuid());
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
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006d4-9fbb-4f20-866b-0ece245615a1"))), new TypeReference<AppointmentServiceFullResponse>() {});
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
        Parameter uuid = new Parameter("uuid", "c36006d4-9fbb-4f20-866b-0ece245615a1");
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
        AppointmentServiceDefinition appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid("c36006d4-9fbb-4f20-866b-0ece245615a1");
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
        appointmentServiceDefinition = appointmentServiceDefinitionService.getAppointmentServiceByUuid("c36006d4-9fbb-4f20-866b-0ece245615a1");
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
        assertEquals("c36006d4-9fbb-4f20-866b-0ece245615a1", asResponses.get(0).getUuid());
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
}
