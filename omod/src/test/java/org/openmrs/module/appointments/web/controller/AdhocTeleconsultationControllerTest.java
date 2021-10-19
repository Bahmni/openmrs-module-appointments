package org.openmrs.module.appointments.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.module.appointments.service.impl.TeleconsultationAppointmentService;
import org.openmrs.module.appointments.web.contract.AdhocTeleconsultationResponse;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AdhocTeleconsultationControllerTest {

    @Mock
    private TeleconsultationAppointmentService teleconsultationAppointmentService;

    @InjectMocks
    private AdhocTeleconsultationController adhocTeleconsultationController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGenerateAdhocTeleconsultationLink() throws Exception {
        String patientUuid = "patientUuid";
        String provider = "";
        ResponseEntity<AdhocTeleconsultationResponse> adhocTeleconsultationResponse = adhocTeleconsultationController.generateAdhocTeleconsultationLink(patientUuid, provider);
        verify(teleconsultationAppointmentService,
                times(1)).generateAdhocTeleconsultationLink(any(), eq(patientUuid), eq(provider));
        assertNotNull(adhocTeleconsultationResponse.getBody().getUuid());
    }
}
