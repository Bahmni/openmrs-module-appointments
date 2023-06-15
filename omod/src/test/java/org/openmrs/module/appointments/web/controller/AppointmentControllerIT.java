package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentsSummary;
import org.openmrs.module.appointments.web.contract.DailyAppointmentServiceSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppointmentControllerIT extends BaseIntegrationTest {
    @Autowired
    AppointmentController appointmentController;

    @Autowired
    AppointmentsService appointmentsService;

    @Autowired
    AppointmentAuditDao appointmentAuditDao;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
        Context.getAdministrationService().setGlobalProperty("disableDefaultAppointmentValidations", "false");
    }

    @Test
    public void should_GetAllAppointments() throws Exception {
        List<AppointmentDefaultResponse> asResponses
                = deserialize(handle(newGetRequest("/rest/v1/appointment/all")),
                new TypeReference<List<AppointmentDefaultResponse>>() {
                });
        assertEquals(17, asResponses.size());
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
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": [ {" +
                    "\"uuid\":\"2d15071d-439d-44e8-9825-aa8e1a30d2a2\"," +
                    "\"comments\":\"available\"," +
                    "\"response\":\"ACCEPTED\"" +
                "} ] }";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetAllNonVoidedNonCancelledFutureAppointmentsforaServiceType() throws Exception {
        String requestURI = "/rest/v1/appointment/futureAppointmentsForServiceType";
        String serviceTypeUuid = "678906e5-9fbb-4f20-866b-0ece24564578";
        MockHttpServletRequest getRequest = newGetRequest(requestURI, new Parameter("appointmentServiceTypeUuid", serviceTypeUuid));
        List<AppointmentDefaultResponse> asResponse = deserialize(handle(getRequest),
            new TypeReference<List<AppointmentDefaultResponse>>() {});

        assertEquals(13, asResponse.size());
    }

    @Test
    public void shouldGetCountOfAppointmentsForAllServicesInAGivenDateRange() throws Exception {
        MockHttpServletRequest getRequest = newGetRequest("/rest/v1/appointment/appointmentSummary", new Parameter("startDate", "2108-08-15T00:00:00.0Z"),
                new Parameter("endDate", "2108-08-22T00:00:00.0Z"));
        List<AppointmentsSummary> appointmentsSummaries
                = deserialize(handle(getRequest), new TypeReference<List<AppointmentsSummary>>() {
        });
        AppointmentsSummary appointmentsSummary = appointmentsSummaries.get(0);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date appointmentDate = simpleDateFormat.parse("2108-08-15");
        assertEquals(3, appointmentsSummaries.size());
        assertNotNull(appointmentsSummary);
        assertEquals(1, appointmentsSummary.getAppointmentService().getAppointmentServiceId(), 0);
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentsSummary.getAppointmentService().getUuid());
        assertEquals(1, appointmentsSummary.getAppointmentCountMap().size());
        Map<String, DailyAppointmentServiceSummary> appointmentCountMap = appointmentsSummary.getAppointmentCountMap();
        Map appointmentCount = (Map)appointmentCountMap.get("2108-08-15");
        assertNotNull(appointmentCount);
        assertEquals(4, appointmentCount.get("allAppointmentsCount"));
        assertEquals(1, appointmentCount.get("missedAppointmentsCount"));
        assertEquals(appointmentDate.getTime(), appointmentCount.get("appointmentDate"));
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a6", appointmentCount.get("appointmentServiceUuid"));
    }

    @Test
    public void shouldThrowErrorWhenAppointmentDoesNotHavePatientInIT() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"appointmentKind\": \"WalkIn\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldThrowErrorWhenAppointmentDoesNotHaveServiceInIT() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"appointmentKind\": \"WalkIn\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void should_SearchWithAppointment() throws Exception {
        String content = "{ \"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment/search", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void should_SearchForDatelessAppointments() throws Exception {
        String content = "{ \"isDatelessAppointments\": true }";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment/search", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldCreateAuditEventsWhenDetailsChangesOnEditAppointment() throws Exception {
        String content = "{ \"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Appointment appointment = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        List<AppointmentAudit> historyForAppointment = appointmentAuditDao
                .getAppointmentHistoryForAppointment(appointment);
        assertEquals(1, historyForAppointment.size());
        assertNotNull(historyForAppointment.get(0).getDateCreated());
        assertNotNull(historyForAppointment.get(0).getCreator());
        assertEquals(appointment, historyForAppointment.get(0).getAppointment());
        assertNotNull(historyForAppointment.get(0).getNotes());
    }

    @Test
    public void shouldThrowExceptionForInvalidAppointmentOnUndoStatus() throws Exception {
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment/undoStatusChange/"+ "randomUuid", "{}"));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("Appointment does not exist"));
    }


    @Test
    public void shouldThrowExceptionWhenThereIsNoPriorActionToUndo() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"3\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"appointmentKind\": \"WalkIn\"}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        AppointmentDefaultResponse appointmentDefaultResponse = deserialize(response, new TypeReference<AppointmentDefaultResponse>() {});
        String appointmentUuid = appointmentDefaultResponse.getUuid();
        content = "{}";
        response = handle(newPostRequest("/rest/v1/appointment/undoStatusChange/"+ appointmentUuid, content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
        assertTrue(response.getContentAsString().contains("No status change actions to undo"));
    }

    @Test
    public void shouldThrowExceptionWhenThereIsNoRequestPayload() throws Exception {
        String content = "{}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldNotUpdateAppointmentMetadataOnConflictsCall() throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String content = "{" +
                "\"uuid\":\"c36006e5-9fbb-4f20-866b-0ece245615a7\"," +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\"," +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2107-07-15T17:30:00.0\"," +
                "\"endDateTime\": \"2107-07-15T18:30:00.0\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/conflicts", content));
        assertEquals(200, response.getStatus());
        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());

        MockHttpServletResponse editResponse = handle(newPostRequest("/rest/v1/appointment", content));
        assertNotNull(editResponse);
        assertEquals(200, editResponse.getStatus());
        AppointmentDefaultResponse  appointmentEditResponse = deserialize(editResponse, new TypeReference<AppointmentDefaultResponse>() {});
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a7", appointmentEditResponse.getUuid());
        assertEquals("Fri Jul 15 17:30:00 UTC 2107", appointmentEditResponse.getStartDateTime().toString());
        assertEquals("Fri Jul 15 18:30:00 UTC 2107", appointmentEditResponse.getEndDateTime().toString());
    }
}
