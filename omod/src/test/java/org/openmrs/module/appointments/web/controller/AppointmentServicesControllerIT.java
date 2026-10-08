package org.openmrs.module.appointments.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.openmrs.module.appointments.web.contract.AppointmentServiceFullResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppointmentServicesControllerIT extends BaseIntegrationTest {

    @BeforeEach
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
        Context.getAdministrationService().setGlobalProperty("disableDefaultAppointmentValidations", "false");
    }

    @Test
    public void shouldGetASpecificAppointmentService() throws Exception {
        AppointmentServiceFullResponse response = deserialize(
                handle(newGetRequest("/rest/v1/appointment-services/c36006e5-9fbb-4f20-866b-0ece245615a6")),
                new TypeReference<AppointmentServiceFullResponse>() {
                });
        assertEquals("Consultation", response.getName());
    }

}
