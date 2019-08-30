package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class RecurringAppointmentsControllerIT extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
        Context.getAdministrationService().setGlobalProperty("disableDefaultAppointmentValidations", "false");
    }

    @Test
    public void shouldSaveRecurringAppointments() throws Exception {
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
        String content = "{ \"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"serviceUuid\": \"c36006d4-9fbb-4f20-866b-0ece245615c1\", " +
                "\"startDateTime\": \"2019-05-19T23:45:00.000Z\", " +
                "\"endDateTime\": \"2019-05-20T00:15:00.000Z\",  " +
                "\"appointmentKind\": \"WalkIn\", " +
                "\"providers\": []," +
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
}
