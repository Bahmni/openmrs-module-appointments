package org.openmrs.module.appointments.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdhocTeleconsultationControllerIT extends BaseIntegrationTest {

    @Autowired
    AdhocTeleconsultationController adhocTeleconsultationController;

    @BeforeEach
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
        Context.getAdministrationService().setGlobalProperty("bahmni.adhoc.teleConsultation.id", "OpenMRS Identification Number");

    }

    @Test
    public void should_GenerateTeleconsultationLink() throws Exception {
        AdhocTeleconsultationResponse asResponses
                = deserialize(handle(newGetRequest("/rest/v1/adhocTeleconsultation/generateAdhocTeleconsultationLink",
                        new Parameter("patientUuid", "2c33920f-7aa6-48d6-998a-60412d8ff7d5"),
                        new Parameter("provider", "doctor"))),
                new TypeReference<AdhocTeleconsultationResponse>() {
                });
        assertEquals("https://meet.jit.si/GAN200000", asResponses.getLink());
    }
}
