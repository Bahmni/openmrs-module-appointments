package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;

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
        assertEquals(1, asResponses.size());
    }

    @Test
    public void should_SaveNewAppointment() throws Exception {
        String content = "{ " +
                "\"providerUuid\": \"823fdcd7-3f10-11e4-adec-0800271c1b75\", " +
                "\"appointmentNumber\": \"1\",  " +
                "\"locationUuid\": null, " +
                "\"patientUuid\": \"2c33920f-7aa6-48d6-998a-60412d8ff7d5\", " +
                "\"status\": \"new\",  " +
                "\"startDateTime\": \"2017-07-20\", " +
                "\"endDateTime\": \"2017-07-20\",  " +
                "\"comments\": null, " +
                "\"appointmentsKind\": \"walkin\"}";

        List<AppointmentDefaultResponse> asResponses
                = deserialize(handle(newPostRequest("/rest/v1/appointment", "{patientUuid = ''}")),
                new TypeReference<List<AppointmentDefaultResponse>>() {
                });
        assertEquals(2, asResponses.size());
    }

    @Test
    public void shouldGetAllNonVoidedNonCncelledFutureAppointmentsforaServiceType() throws Exception {
        String requestURI = "/rest/v1/appointment";
        String serviceTypeUuid = "678906e5-9fbb-4f20-866b-0ece24564578";
        MockHttpServletRequest getRequest = newGetRequest(requestURI, new Parameter("appointmentServiceTypeUuid", serviceTypeUuid));
        List<AppointmentDefaultResponse> asResponse = deserialize(handle(getRequest), new TypeReference<List<AppointmentDefaultResponse>>() {});

        assertEquals(1, asResponse.size());
    }
}
