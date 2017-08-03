package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentServiceControllerIT extends BaseIntegrationTest {
    @Autowired
    AppointmentServiceController appointmentServiceController;

    @Autowired
    AppointmentServiceService appointmentServiceService;

    @Autowired
    AppointmentsService appointmentsService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentServicesTestData.xml");
    }

    @Test
    public void should_createAppointmentService() throws Exception {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a2");
        assertNull(appointmentService);
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"maxAppointmentsLimit\":\"30\"," +
                "\"color\":\"#00ff00\"" +
                "}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a2"))), new TypeReference<AppointmentServiceFullResponse>() {});
        assertNotNull(asResponse);
        assertEquals(asResponse.getName(), "Cardiology Consultation");
        assertEquals(asResponse.getStartTime(), "09:00:00");
        assertEquals(asResponse.getEndTime(), "17:30:00");
        assertEquals(asResponse.getDurationMins().intValue(), 30);
        assertEquals(asResponse.getMaxAppointmentsLimit().intValue(), 30);
        assertEquals(asResponse.getColor(), "#00ff00");
        assertNull(asResponse.getWeeklyAvailability());
    }

    @Test
    public void should_createAppointmentServiceOnlyWithName() throws Exception {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a2");
        assertNull(appointmentService);
        String dataJson = "{\"name\":\"Cardiology Consultation\", \"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a2\"}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a2"))), new TypeReference<AppointmentServiceFullResponse>() {});
        assertNotNull(asResponse);
        assertEquals(asResponse.getName(), "Cardiology Consultation");
        assertEquals(new String(),asResponse.getStartTime());
        assertNull(asResponse.getDurationMins());
        assertNull(asResponse.getMaxAppointmentsLimit());
        assertNull(asResponse.getWeeklyAvailability());
    }

    @Test
    public void should_createAppointmentServiceWithServiceAvailability() throws Exception {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a2");
        assertNull(appointmentService);
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a2\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"maxAppointmentsLimit\":\"30\"," +
                "\"color\":\"#0000ff\"," +
                "\"weeklyAvailability\": [{ \"dayOfWeek\": \"MONDAY\", \"startTime\":\"09:00:00\", \"endTime\":\"17:30:00\", \"maxAppointmentsLimit\":\"10\" }]" +
                "}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a2"))), new TypeReference<AppointmentServiceFullResponse>() {});
        assertNotNull(asResponse);
        assertEquals("Cardiology Consultation", asResponse.getName());
        assertEquals("09:00:00", asResponse.getStartTime());
        assertEquals("17:30:00", asResponse.getEndTime());
        assertEquals(30, asResponse.getDurationMins().intValue());
        assertEquals(30, asResponse.getMaxAppointmentsLimit().intValue());
        assertEquals("#0000ff", asResponse.getColor());
        assertNotNull(asResponse.getWeeklyAvailability());
        assertEquals(1, asResponse.getWeeklyAvailability().size());

        List<Map> availabilityList = new ArrayList<>(asResponse.getWeeklyAvailability());
        assertEquals(1, availabilityList.size());
        assertEquals("MONDAY", availabilityList.get(0).get("dayOfWeek"));
        assertEquals("09:00:00", availabilityList.get(0).get("startTime"));
        assertEquals("17:30:00", availabilityList.get(0).get("endTime"));
        assertEquals(10, availabilityList.get(0).get("maxAppointmentsLimit"));

    }

    @Test
    public void shouldCreateAppointmentServiceWithServiceTypes() throws Exception {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006e5-9fbb-4f20-866b-0ece245615a2");
        assertNull(appointmentService);
        String dataJson = "{\"name\":\"Cardiology Consultation\",\"startTime\":\"09:00:00\"," +
            "\"endTime\":\"17:30:00\"," +
            "\"durationMins\":\"30\"," +
            "\"color\":\"#fff000\"," +
            "\"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a2\"," +
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

    @Test(expected = RuntimeException.class)
    public void should_notCreateAppointmentServiceWhenNameIsNull() throws Exception {
        String dataJson = "{}";
        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test
    public void should_GetAllAppointmentServices() throws Exception {
        List<AppointmentServiceDefaultResponse> asResponses = deserialize(handle(newGetRequest("/rest/v1/appointmentService/all")), new TypeReference<List<AppointmentServiceDefaultResponse>>() {});
        assertEquals(1,asResponses.size());
        assertEquals("Consultation", asResponses.get(0).getName());
        assertEquals("Consultation", asResponses.get(0).getDescription());
        assertEquals("09:00:00", asResponses.get(0).getStartTime());
        assertEquals("17:00:00", asResponses.get(0).getEndTime());
        assertEquals(30, asResponses.get(0).getDurationMins().intValue());
        assertEquals(4, asResponses.get(0).getMaxAppointmentsLimit().intValue());
        assertEquals("Ortho", asResponses.get(0).getSpeciality().get("name"));
        assertEquals("Room1", asResponses.get(0).getLocation().get("name"));
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
    public void shouldVoidTheAppointmentServiceAlongWithServiceAvailabilityAndServiceTypesWitoutVoidReason() throws Exception {
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
                "There are appointments in future against this service. Please cancel those appointments before deleting this service. After deleting the service, you will not be able to see any appts. for that service",
                responseObject.get("message"));
    }

    @Test
    public void shouldUpdateService() throws Exception {
        AppointmentService appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006d4-9fbb-4f20-866b-0ece245615a1");
        assertNotNull(appointmentService);
        String uuid = appointmentService.getUuid();
        String dataJson = "{\"name\":\"Chemotherapy\",\"startTime\":\"09:00:00\"," +
                "\"endTime\":\"17:30:00\"," +
                "\"durationMins\":\"30\"," +
                "\"uuid\":\""+ uuid +"\"," +
                "\"locationUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"specialityUuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"serviceTypes\": [{ \"name\": \"stage 1\", \"duration\":\"20\", \"uuid\":\"c36006d5-9fcc-4f20-866b-0ece245615b1\" }]" +
                "}";
        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        appointmentService = appointmentServiceService.getAppointmentServiceByUuid("c36006d4-9fbb-4f20-866b-0ece245615a1");
        assertEquals("Chemotherapy",appointmentService.getName());
        assertEquals(uuid, appointmentService.getUuid());
        assertEquals(1, appointmentService.getServiceTypes().size());
    }
}
