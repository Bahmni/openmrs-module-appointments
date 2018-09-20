package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class AppointmentsControllerIT extends BaseIntegrationTest {

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
    public void shouldGetAllAppointmentsInGivenDateRange() throws Exception {
        String responseBodyJson = "{\"startDate\":\"2108-08-13T18:30:00.000Z\"," +
                "\"endDate\":\"2108-08-15T18:29:59.000Z\"}";


        List<AppointmentDefaultResponse> response = deserialize(
                handle(newPostRequest("/rest/v1/appointments/search", responseBodyJson)),
                new TypeReference<List<AppointmentDefaultResponse>>() {
            });

        assertEquals(5, response.size());
    }
}
