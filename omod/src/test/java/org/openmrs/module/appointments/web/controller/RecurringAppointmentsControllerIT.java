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
import org.openmrs.module.appointments.web.contract.RecurringAppointmentDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RecurringAppointmentsControllerIT extends BaseIntegrationTest {

    @Autowired
    RecurringAppointmentsController recurringAppointmentsController;

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
    public void shouldSaveRecurringAppointments() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":2," +
                "\"daysOfWeek\":[]," +
                "\"endDate\":\"\"," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldSaveRecurringAppointmentsWithEndDate() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"period\":2," +
                "\"daysOfWeek\":[]," +
                "\"endDate\":\"2019-05-25T23:45:00.000Z\"," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenSaveRecurringAppointmentsWithNoFrequencyAndNoEndDate() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"period\":2," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenSaveRecurringAppointmentsWithInvalidEndDate() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"period\":2," +
                "\"daysOfWeek\":[]," +
                "\"endDate\":\"\"," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldSaveWeeklyRecurringAppointmentsWithEndDate() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"period\":2," +
                "\"daysOfWeek\":[\"SUNDAY\",\"THURSDAY\"]," +
                "\"endDate\":\"2019-05-25T23:45:00.000Z\"," +
                "\"type\":\"WEEK\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldSaveWeeklyRecurringAppointmentsWithFrequencyForAllDays() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"period\":2," +
                "\"daysOfWeek\":[\"Sunday\",\"Monday\",\"Tuesday\",\"Wednesday\",\"Thursday\",\"Friday\",\"Saturday\"]," +
                "\"endDate\":\"2019-05-25T23:45:00.000Z\"," +
                "\"type\":\"WEEK\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenSaveWeeklyRecurringAppointmentsWithInvalidDaysOfWeek() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":2," +
                "\"daysOfWeek\":[\"MISC\"]," +
                "\"type\":\"Week\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenSaveWeeklyRecurringAppointmentsWithNoDaysOfWeek() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":2," +
                "\"type\":\"Week\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldThrowExceptionWhenSaveWeeklyRecurringAppointmentsWithEmptyListForDaysOfWeek() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []}," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":2," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Week\"" +
                "}}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldUpdateRecurringAppointmentWhenApplyForAllIsFalse() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": false," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldUpdateRecurringPatternWithNewAppointmentAndOldAppointmentAsRelatedAppointment() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": false," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        List<RecurringAppointmentDefaultResponse> appointmentDefaultResponse = deserialize(response, new TypeReference<List<RecurringAppointmentDefaultResponse>>() {
        });
        String appointmentUuid = appointmentDefaultResponse.get(0).getAppointmentDefaultResponse().getUuid();

        Appointment appointment = appointmentsService.getAppointmentByUuid(appointmentUuid);
        Appointment relatedAppointment = appointment.getRelatedAppointment();
        assertEquals("c36006e5-9fbb-4f20-866b-0ece245615a7", relatedAppointment.getUuid());
    }

    @Test
    public void shouldUpdateRecurringAppointmentWhenApplyForAllIsTrue() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": true," +
                "\"timeZone\": \"UTC\"," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldUpdateRecurringAppointmentForRequestedStatusWhenApplyForAllIsTrue() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"25a7e84b-6933-49cc-aebe-c1d4d33a86y2\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": [{\"uuid\":\"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"}]}," +
                "\"applyForAll\": true," +
                "\"timeZone\": \"IST\"," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        List<RecurringAppointmentDefaultResponse> appointmentDefaultResponse = deserialize(response, new TypeReference<List<RecurringAppointmentDefaultResponse>>() {
        });
        for (RecurringAppointmentDefaultResponse recurringAppointmentDefaultResponse : appointmentDefaultResponse) {
            assertEquals(1, recurringAppointmentDefaultResponse.getAppointmentDefaultResponse().getProviders().size());
        }
    }

    @Test
    public void shouldUpdateRecurringAppointmentByAddingAppointmentsWhenFrequencyIsChanged() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": true," +
                "\"timeZone\": \"UTC\"," +
                "\"recurringPattern\":{" +
                "\"frequency\":5," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        List<RecurringAppointmentDefaultResponse> appointmentDefaultResponse = deserialize(response, new TypeReference<List<RecurringAppointmentDefaultResponse>>() {
        });
        assertEquals(5, appointmentDefaultResponse.size());
    }

    @Test
    public void shouldGetAppointmentGivenAnUuid() throws Exception {
        RecurringAppointmentDefaultResponse response
                = deserialize(handle(newGetRequest("/rest/v1/recurring-appointments",
                new Parameter("uuid", "c36006e5-9fbb-4f20-866b-0ece245615a7"))),
                new TypeReference<RecurringAppointmentDefaultResponse>() {
                });
        assertNotNull(response);
        assertNotNull(response.getAppointmentDefaultResponse());
        assertNotNull(response.getRecurringPattern());

    }

    @Test
    public void shouldThrowExceptionIfAppointmentDoesNotExist() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Appointment does not exist");
        deserialize(handle(newGetRequest("/rest/v1/recurring-appointments",
                new Parameter("uuid", "randomUuid"))),
                new TypeReference<RecurringAppointmentDefaultResponse>() {});

    }

    @Test
    public void should_changeAppointmentStatusForAllRecurringAppointments() throws Exception {
        String content = "{ \"toStatus\": \"Cancelled\"," +
                "\"timeZone\": \"Asia/Calcutta\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments/c36006e5-9fbb-4f20-866b-0ece245615a7/changeStatus", content));
        assertNotNull(response);

        Appointment appointmentByUuid = appointmentsService.getAppointmentByUuid("c36006e5-9fbb-4f20-866b-0ece245615a7");
        assertEquals(200, response.getStatus());
        assertNotNull(appointmentByUuid);

        Set<Appointment> recurringAppointments = appointmentByUuid.getAppointmentRecurringPattern().getAppointments();
        recurringAppointments.stream().forEach(appointment -> assertEquals(appointmentByUuid.getStatus(), appointment.getStatus()));

        List<AppointmentAudit> historyForAppointment = appointmentAuditDao.getAppointmentHistoryForAppointment(appointmentByUuid);
        assertEquals(1, historyForAppointment.size());
        assertNotNull(historyForAppointment.get(0).getDateCreated());
        assertNotNull(historyForAppointment.get(0).getCreator());
        assertEquals(appointmentByUuid, historyForAppointment.get(0).getAppointment());
        assertNull(historyForAppointment.get(0).getAppointment().getComments());
    }

    @Test
    public void should_throwExceptionWhenNoClientTimeZoneIsProvided() throws Exception {
        String content = "{ \"toStatus\": \"Scheduled\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments/c36006e5-9fbb-4f20-866b-0ece245615a8/changeStatus", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void should_throwExceptionWhenEmptyClientTimeZoneIsProvided() throws Exception {
        String content = "{ \"toStatus\": \"Scheduled\"," +
                "\"timeZone\": \"\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments/c36006e5-9fbb-4f20-866b-0ece245615a8/changeStatus", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldReturnNoContentResponseForRecurringAppointmentsForNoConflicts() throws Exception {
        String content = "{" +
                "\"appointmentRequest\": {" +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\"," +
                "\"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\"," +
                "\"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\"," +
                "\"startDateTime\": \"2019-09-23T07:30:00\"," +
                "\"endDateTime\": \"2019-09-23T12:00:00\"," +
                "\"providers\": [{" +
                "\"uuid\": \"2bdc3f7d-d911-401a-84e9-5494dda83e8e\"," +
                "\"response\": \"ACCEPTED\"," +
                "\"comments\": null" +
                "}]," +
                "\"locationUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a1\"," +
                "\"appointmentKind\": \"Scheduled\"" +
                "}," +
                "\"recurringPattern\": {" +
                "\"frequency\": 2," +
                "\"period\": 1," +
                "\"daysOfWeek\": []," +
                "\"endDate\": \"\"," +
                "\"type\": \"Day\"" +
                "}," +
                "\"timeZone\": \"asia/kolkata\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(204, response.getStatus());
    }

    @Test
    public void shouldReturnResponseForRecurringAppointmentsForNoConflicts() throws Exception {
        String content = "{" +
                "\"appointmentRequest\": {" +
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
                "}," +
                "\"recurringPattern\": {" +
                "\"frequency\": 2," +
                "\"period\": 1," +
                "\"daysOfWeek\": []," +
                "\"endDate\": \"\"," +
                "\"type\": \"Day\"" +
                "}," +
                "\"timeZone\": \"asia/kolkata\"" +
                "}";
        MockHttpServletResponse response = handle(newPostRequest("/rest/v1/recurring-appointments/conflicts", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());

        Map<String, List<AppointmentDefaultResponse>> appointmentDefaultResponse = deserialize(response, new TypeReference<Map<String, List<AppointmentDefaultResponse>>>() {
        });
        assertEquals(2, appointmentDefaultResponse.get(AppointmentConflictType.SERVICE_UNAVAILABLE.name()).size());
        assertEquals(1, appointmentDefaultResponse.get(AppointmentConflictType.PATIENT_DOUBLE_BOOKING.name()).size());
    }

    @Test
    public void shouldThrowApiExceptionWhenEndDateIsBeforeStartDate() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": true," +
                "\"timeZone\": \"UTC\"," +
                "\"recurringPattern\":{" +
                "\"frequency\":0," +
                "\"endDate\":\"2009-07-20\"," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldReturnNineAppointmentsOnUpdateFromSix() throws Exception {

        executeDataSet("recurringAppointmentUpdateTestData.xml");

        String payload = "{\n" +
                "   \"appointmentRequest\": {\n" +
                "      \"uuid\": \"a86c8742-538e-4da6-b0a3-f8e0d478b8fc\",\n" +
                "      \"patientUuid\": \"88cc17a0-b97d-4209-abfd-6ce1800be62c\",\n" +
                "      \"serviceUuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a6\",\n" +
                "      \"serviceTypeUuid\": \"678906e5-9fbb-4f20-866b-0ece24564578\",\n" +
                "      \"startDateTime\": \"2050-01-31T03:21:00.000Z\",\n" +
                "      \"endDateTime\": \"2050-01-31T03:51:00.000Z\",\n" +
                "      \"providers\": [],\n" +
                "      \"locationUuid\": \"8de35e75-20e0-11e7-a53f-000c29e530d2\",\n" +
                "      \"appointmentKind\": \"Scheduled\",\n" +
                "      \"comments\": null\n" +
                "   },\n" +
                "   \"recurringPattern\": {\n" +
                "      \"type\": \"WEEK\",\n" +
                "      \"period\": 2,\n" +
                "      \"frequency\": 9,\n" +
                "      \"daysOfWeek\": [\n" +
                "         \"MONDAY\",\n" +
                "         \"SATURDAY\"\n" +
                "      ]\n" +
                "   },\n" +
                "   \"applyForAll\": true,\n" +
                "\"timeZone\": \"UTC\"" +
                "}";
        System.out.println(Calendar.getInstance().getTimeZone().getDisplayName());

        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/a86c8742-538e-4da6-b0a3-f8e0d478b8fc", payload));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
        List<RecurringAppointmentDefaultResponse> appointmentDefaultResponse = deserialize(response, new TypeReference<List<RecurringAppointmentDefaultResponse>>() {
        });
        assertEquals(9, appointmentDefaultResponse.size());

    }

    @Test
    public void shouldUpdateRecurringAppointmentForRequestedAppointmentWhenApplyForAllIsTrue() throws Exception {
        String content = "{ \"appointmentRequest\":{" +
                "\"uuid\": \"c36006e5-9fbb-4f20-866b-0ece245615a7\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"serviceTypeUuid\": \"672546e5-9fbb-4f20-866b-0ece24564578\", " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": \"Some notes\",  " +
                "\"appointmentKind\": \"WalkIn\"," +
                "\"providers\": []}," +
                "\"applyForAll\": true," +
                "\"timeZone\": \"UTC\"," +
                "\"recurringPattern\":{" +
                "\"frequency\":2," +
                "\"period\":1," +
                "\"daysOfWeek\":[]," +
                "\"type\":\"Day\"" +
                "}}";
        MockHttpServletResponse response = handle(newPutRequest("/rest/v1/recurring-appointments/uuid", content));
        assertNotNull(response);
        assertEquals(200, response.getStatus());
    }
}
