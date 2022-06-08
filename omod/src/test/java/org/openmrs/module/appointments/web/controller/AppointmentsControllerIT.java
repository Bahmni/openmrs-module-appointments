package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentAuditDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentAudit;
import org.openmrs.module.appointments.model.AppointmentConflictType;
import org.openmrs.module.appointments.model.AppointmentProviderResponse;
import org.openmrs.module.appointments.model.AppointmentStatus;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.util.DateUtil;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AppointmentsControllerIT extends BaseIntegrationTest {

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
    public void shouldGetASpecificAppointment() throws Exception {
        AppointmentDefaultResponse response = deserialize(
                handle(newGetRequest("/rest/v1/appointments/75504r42-3ca8-11e3-bf2b-0800271c13346")),
                new TypeReference<AppointmentDefaultResponse>() {
                });
        assertEquals("GAN200000", response.getPatient().get("identifier"));
    }

    @Test
    public void shouldSaveNewAppointment() throws Exception {
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


        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        String contentAsString = response.getContentAsString();
        Map responseBody = new ObjectMapper().readValue(contentAsString, Map.class);
        assertNotNull(responseBody.get("uuid"));
        assertEquals(AppointmentStatus.Scheduled.name(), responseBody.get("status"));
    }

    @Test
    public void shouldSaveNewAppointmentWithGivenStatus() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"status\": \"Requested\",  " +
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


        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        String contentAsString = response.getContentAsString();
        Map responseBody = new ObjectMapper().readValue(contentAsString, Map.class);
        assertNotNull(responseBody.get("uuid"));
        assertEquals(AppointmentStatus.Requested.name(), responseBody.get("status"));
    }

    @Test
    public void shouldSearchAppointmentsGivenDateRange() throws Exception {
        String requestBodyJson = "{\"startDate\":\"2108-08-13T18:30:00.000Z\"," +
                "\"endDate\":\"2108-08-15T18:29:59.000Z\"," +
                "\"patientUuid\":\"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"limit\":4}";

        List<AppointmentDefaultResponse> response = deserialize(
                handle(newPostRequest("/rest/v1/appointments/search", requestBodyJson)),
                new TypeReference<List<AppointmentDefaultResponse>>() {
                });

        assertEquals(4, response.size());
    }

    @Test
    public void shouldSearchAcceptProviderUuidToSearchAppointments() throws Exception {
        String requestBodyJson = "{\"startDate\":\"2108-08-13T18:30:00.000Z\"," +
                "\"providerUuid\":\"provider-uuid\"}";

        MockHttpServletResponse httpResponse = handle(newPostRequest("/rest/v1/appointments/search", requestBodyJson));

        assertEquals(200, httpResponse.getStatus());
    }

    @Test
    public void should_changeAppointmentStatusWithDate() throws Exception {
        String onDate = "2108-08-22T10:30:00.0Z";
        String content = "{ \"toStatus\": \"CheckedIn\", \"onDate\":\"" + onDate + "\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/c36006e5-9fbb-4f20-866b-0ece245615a7/status-change", content));

        assertNotNull(response);
        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        assertEquals(200, response.getStatus());
        assertNotNull(appointmentByUuid);
        List<AppointmentAudit> historyForAppointment = appointmentAuditDao
                .getAppointmentHistoryForAppointment(appointmentByUuid);
        assertEquals(1, historyForAppointment.size());
        assertNotNull(historyForAppointment.get(0).getId());
        assertNotNull(historyForAppointment.get(0).getNotes());
        assertEquals(DateUtil.convertToLocalDateFromUTC(onDate).toInstant().toString(), historyForAppointment.get(0).getNotes());
    }

    @Test
    public void should_changeAppointmentStatusWithoutDate() throws Exception {
        String content = "{ \"toStatus\": \"CheckedIn\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/c36006e5-9fbb-4f20-866b-0ece245615a7/status-change", content));

        assertNotNull(response);
        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        assertEquals(200, response.getStatus());
        assertNotNull(appointmentByUuid);
        List<AppointmentAudit> historyForAppointment = appointmentAuditDao
                .getAppointmentHistoryForAppointment(appointmentByUuid);
        assertEquals(1, historyForAppointment.size());
        assertNotNull(historyForAppointment.get(0).getDateCreated());
        assertNotNull(historyForAppointment.get(0).getCreator());
        assertEquals(appointmentByUuid, historyForAppointment.get(0).getAppointment());
        assertEquals(appointmentByUuid.getStatus(), historyForAppointment.get(0).getStatus());
        assertNull(historyForAppointment.get(0).getNotes());
    }

    @Test
    public void should_throwExceptionForInvalidStatusChange() throws Exception {
        String content = "{ \"toStatus\": \"Completed\"}";

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment status can not be changed from Missed to Completed");

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/75504r42-3ca8-11e3-bf2b-0800271c13555/status-change", content));
    }

    @Test
    public void should_throwExceptionForInvalidAppointment() throws Exception {
        String content = "{ \"toStatus\": \"Scheduled\"}";

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/c36006e5-9fbb-4f20-866b-0ece245615a8/status-change", content));
    }

    @Test
    public void shouldUndoCheckedInAppointment() throws Exception {
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
        AppointmentDefaultResponse appointmentDefaultResponse = deserialize(response, new TypeReference<AppointmentDefaultResponse>() {
        });
        String appointmentUuid = appointmentDefaultResponse.getUuid();

        content = "{ \"toStatus\": \"CheckedIn\"}";
        response = handle(newPostRequest("/rest/v1/appointments/" + appointmentUuid + "/status-change", content));
        assertEquals(200, response.getStatus());
        content = "{ \"toStatus\": \"Scheduled\"}";
        response = handle(newPostRequest("/rest/v1/appointments/" + appointmentUuid + "/status-change", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
        assertNotNull(appointment);
        assertEquals("Scheduled", appointment.getStatus().toString());
    }

    @Test
    public void shouldResetMissedAppointmentToScheduledStateWhenUserHasResetAppointmentStatusPrivilege() throws Exception {
        String appointmentUuid = "75504r42-3ca8-11e3-bf2b-0800271c13555";
        String content = "{ \"toStatus\": \"Scheduled\"}";

        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/" + appointmentUuid + "/status-change", content));

        assertEquals(200, response.getStatus());
        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        assertEquals(appointmentByUuid.getStatus().toString(), "Scheduled");
    }

    @Test
    public void shouldReturnDayConflictsForAnAppointment() throws Exception {
        String content = "{\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2019-09-24T11:30:00\"," +
                "\"endDateTime\": \"2019-09-24T12:00:00\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());
    }

    @Test
    public void shouldReturnTimeSlotConflictsForAnAppointment() throws Exception {
        String content = "{\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2019-09-23T13:30:00\"," +
                "\"endDateTime\": \"2019-09-23T14:00:00\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());
    }

    @Test
    public void shouldReturnPatientDoubleBookingConflictsForAnAppointment() throws Exception {
        String content = "{\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2107-07-15T11:30:00\"," +
                "\"endDateTime\": \"2107-07-15T12:30:00\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.PATIENT_DOUBLE_BOOKING.name()).size());
    }

    @Test
    public void shouldReturnPatientDoubleBookingConflictsForExistingAppointment() throws Exception {
        String content = "{ \"uuid\": \"75504r42-3ca8-11e3-bf2b-0800271c13545\"," +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2107-07-15T11:30:00\"," +
                "\"endDateTime\": \"2107-07-15T12:30:00\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.PATIENT_DOUBLE_BOOKING.name()).size());
    }

    @Test
    public void shouldChangeProviderResponse() throws Exception {
        Context.authenticate("provider-response-user", "test");
        String content = "{\"uuid\":\"2bdc3f7d-jh76-401a-84e9-5494dda83e8e\",\"response\":\"ACCEPTED\"}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/c36006e5-9fbb-4f20-8y6t-0ece245615a7/providerResponse", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-8y6t-0ece245615a7");
        assertEquals(AppointmentProviderResponse.ACCEPTED, appointmentByUuid.getProviders().iterator().next().getResponse());
    }

    @Test
    public void shouldChangeRequestedAppointmentStatusToScheduledWhenProviderAccepts() throws Exception {
        Context.authenticate("provider-response-user", "test");
        String content = "{\"uuid\":\"2bdc3f7d-jh76-401a-84e9-5494dda83e8e\",\"response\":\"ACCEPTED\"}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/appointments/c36006e5-9fbb-8ui6-8y6t-0ece245615a7/providerResponse", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-8ui6-8y6t-0ece245615a7");
        assertEquals(AppointmentStatus.Scheduled, appointmentByUuid.getStatus());
        assertEquals(AppointmentProviderResponse.ACCEPTED, appointmentByUuid.getProviders().iterator().next().getResponse());
    }

    @Test
    public void shouldNotUpdateAppointmentMetadataOnConflictsCall() throws Exception {
        String content = "{" +
                "\"uuid\":\"4cee2712-d983-48a1-a4e5-1bbbc960bb95\"," +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\"," +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2107-07-15T17:30:00\"," +
                "\"endDateTime\": \"2107-07-15T18:30:00\"," +
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
        Appointment appointment = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        assertEquals("2107-07-15 17:00:00.0", appointment.getStartDateTime().toString());
    }
}
