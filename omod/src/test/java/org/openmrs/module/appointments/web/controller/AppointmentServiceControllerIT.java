package org.openmrs.module.appointments.web.controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.BaseWebControllerTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashMap;
import java.util.List;

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
                "\"maxAppointmentsLimit\":\"30\"}";

        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
        AppointmentServiceResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a2"))), new TypeReference<AppointmentServiceResponse>() {});
        assertNotNull(asResponse);
        assertEquals(asResponse.getName(), "Cardiology Consultation");
        assertEquals(asResponse.getStartTime(), "09:00:00");
        assertEquals(asResponse.getEndTime(), "17:30:00");
        assertEquals(asResponse.getDurationMins().intValue(), 30);
        assertEquals(asResponse.getMaxAppointmentsLimit().intValue(), 30);
    }

    @Test
    public void should_notCreateAppointmentServiceWhenNameIsNull() throws Exception {
        String dataJson = "{}";
        handle(newPostRequest("/rest/v1/appointmentService", dataJson));
    }

    @Test
    public void should_GetAllAppointmentServices() throws Exception {
        List<AppointmentServiceResponse> asResponses = deserialize(handle(newGetRequest("/rest/v1/appointmentService/all")), new TypeReference<List<AppointmentServiceResponse>>() {});
        assertEquals(1,asResponses.size());
    }

    @Test
    public void should_GetServiceByUuid() throws Exception {
        AppointmentServiceResponse asResponse = deserialize(handle(newGetRequest("/rest/v1/appointmentService",new Parameter("uuid", "c36006d4-9fbb-4f20-866b-0ece245615a1"))), new TypeReference<AppointmentServiceResponse>() {});
        assertNotNull(asResponse);
        assertEquals(asResponse.getName(), "Consultation");
        assertEquals(asResponse.getDescription(), "Consultation");
        assertEquals(asResponse.getStartTime(), "09:00:00");
        assertEquals(asResponse.getEndTime(), "17:00:00");
        assertEquals(asResponse.getDurationMins().intValue(), 30);
        assertEquals(asResponse.getMaxAppointmentsLimit().intValue(), 4);
        assertEquals(asResponse.getSpeciality().get("name"), "Ortho");
        assertEquals(asResponse.getLocation().get("name"), "Room1");
    }
}
