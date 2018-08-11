package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentDefaultResponse;

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
}
