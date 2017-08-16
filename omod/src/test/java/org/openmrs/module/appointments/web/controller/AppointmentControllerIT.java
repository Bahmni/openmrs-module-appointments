package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentCount;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentsSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentControllerIT extends BaseIntegrationTest {
    @Autowired
    AppointmentController appointmentController;

    @Autowired
    AppointmentsService appointmentsService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
    }

    @Test
    public void should_GetAllAppointments() throws Exception {
        List<AppointmentDefaultResponse> asResponses
                = deserialize(handle(newGetRequest("/rest/v1/appointment/all")),
                new TypeReference<List<AppointmentDefaultResponse>>() {
                });
        assertEquals(5, asResponses.size());
    }
    
    @Test
    public void should_GetAllAppointmentsForDate() throws Exception {
        List<AppointmentDefaultResponse> asResponses
                = deserialize(handle(newGetRequest("/rest/v1/appointment/all",
                new Parameter("forDate", "2108-08-15T00:00:00.0Z"))),
                new TypeReference<List<AppointmentDefaultResponse>>() {
                });
        assertEquals(4, asResponses.size());
    }

    @Test
    public void should_SaveNewAppointment() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"status\": \"Scheduled\",  " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"appointmentKind\": \"WalkIn\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetAllNonVoidedNonCancelledFutureAppointmentsforaServiceType() throws Exception {
        String requestURI = "/rest/v1/appointment";
        String serviceTypeUuid = "678906e5-9fbb-4f20-866b-0ece24564578";
        MockHttpServletRequest getRequest = newGetRequest(requestURI, new Parameter("appointmentServiceTypeUuid", serviceTypeUuid));
        List<AppointmentDefaultResponse> asResponse = deserialize(handle(getRequest),
            new TypeReference<List<AppointmentDefaultResponse>>() {});

        assertEquals(3, asResponse.size());
    }

    @Test
    public void shouldGetCountOfAppointmentsForAllServicesInAGivenDateRange() throws Exception {
        MockHttpServletRequest getRequest = newGetRequest("/rest/v1/appointment/appointmentSummary",
                new Parameter("startDate", "2108-08-15T00:00:00.0Z"),
                new Parameter("endDate", "2108-08-22T00:00:00.0Z"));
        List<AppointmentsSummary> appointmentsSummaries
                = deserialize(handle(getRequest), new TypeReference<List<AppointmentsSummary>>() {
        });
        AppointmentsSummary appointmentsSummary = appointmentsSummaries.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date appointmentDate = simpleDateFormat.parse("2108-08-15");
        assertEquals(1, appointmentsSummaries.size());
        assertNotNull(appointmentsSummary);
        assertEquals(1, appointmentsSummary.getAppointmentService().getAppointmentServiceId(), 0);
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentsSummary.getAppointmentService().getUuid());
        assertEquals(1, appointmentsSummary.getAppointmentCountList().size());
//        Iterator<LinkedHashMap> iterator = appointmentsSummary.getAppointmentCountList().iterator();
//
//        LinkedHashMap<String, String> firstAppointment = iterator.next();
//        assertEquals(3, firstAppointment.get("allAppointmentsCount"));
//        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", firstAppointment.get("appointmentServiceUuid"));
//        assertEquals(1, firstAppointment.get("missedAppointmentsCount"));
//        assertEquals(appointmentDate.getTime(), firstAppointment.get("appointmentDate"));

//        AppointmentCount appointmentCount = appointmentsSummary.getAppointmentCountList()[""];
//        assertNotNull(appointmentCount);
//        assertEquals(3, appointmentCount.getAllAppointmentsCount(), 0);
//        assertEquals(1, appointmentCount.getMissedAppointmentsCount(), 0);
//        assertEquals(appointmentDate.getTime(), appointmentCount.getAppointmentDate());
//        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentCount.getAppointmentServiceUuid());

//        for (Map.Entry<String, AppointmentCount> entry : appointmentsSummary.getAppointmentCountList().entrySet()) {
//            String key = entry.getKey();
////            AppointmentCount appointmentCount = (AppointmentCount) entry.getValue();
//            assertEquals("15 Aug Wed", key);
//            assertNotNull(appointmentCount);
//        }
    }
}
