package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppointmentServiceControllerIT extends BaseIntegrationTest {
    @Autowired
    AppointmentServiceController appointmentServiceController;

    @Autowired
    AppointmentServiceService appointmentServiceService;

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
                "\"weeklyAvailabilities\": [{ \"dayOfWeek\": \"Monday\", \"startTime\":\"09:00:00\", \"endTime\":\"17:30:00\"}]" +
                "}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        AppointmentServiceFullResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a2"))), new TypeReference<AppointmentServiceFullResponse>() {});
        assertNotNull(asResponse);
        assertEquals(asResponse.getName(), "Cardiology Consultation");
        assertEquals(asResponse.getStartTime(), "09:00:00");
        assertEquals(asResponse.getEndTime(), "17:30:00");
        assertEquals(asResponse.getDurationMins().intValue(), 30);
        assertEquals(asResponse.getMaxAppointmentsLimit().intValue(), 30);
//        assertNotNull(asResponse.getWeeklyAvailability());
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
    }
}
