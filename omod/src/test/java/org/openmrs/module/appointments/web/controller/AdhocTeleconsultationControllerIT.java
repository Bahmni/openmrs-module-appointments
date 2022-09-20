package org.openmrs.module.appointments.web.controller;

import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;
import org.openmrs.module.appointments.web.BaseIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

public class AdhocTeleconsultationControllerIT extends BaseIntegrationTest {

    @Autowired
    AdhocTeleconsultationController adhocTeleconsultationController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("appointmentTestData.xml");
        Context.getAdministrationService().setGlobalProperty("bahmni.adhoc.teleConsultation.id", "OpenMRS Identification Number");

    }

    @Test
    public void should_GenerateTeleconsultationLink() throws Exception {
        AdhocTeleconsultationResponse asResponses
                = deserialize(handle(newGetRequest("/rest/v1/adhocTeleconsultation/generateAdhocTeleconsultationLink",
                        new Parameter("patientUuid", "2c33920f-7aa6-48d6-998a-60412d8ff7d5"),
                        new Parameter("provider", "superman"))),
                new TypeReference<AdhocTeleconsultationResponse>() {
                });
        assertEquals("https://meet.jit.si/GAN200000", asResponses.getLink());
    }
}
