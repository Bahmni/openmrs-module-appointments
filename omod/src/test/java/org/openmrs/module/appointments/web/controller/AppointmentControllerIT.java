package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;
import org.openmrs.module.appointments.web.contract.AppointmentServiceDefaultResponse;
import org.springframework.beans.factory.annotation.Autowired;

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
                                new TypeReference<List<AppointmentDefaultResponse>>() {});
        assertEquals(1, asResponses.size());
    }

    @Test
    public void should_SaveNewAppointment() throws Exception {
        List<AppointmentDefaultResponse> asResponses
                = deserialize(handle(newPostRequest("/rest/v1/appointment", "{patientUuid = ''}")),
                new TypeReference<List<AppointmentDefaultResponse>>() {});
        assertEquals(1, asResponses.size());
    }
}
