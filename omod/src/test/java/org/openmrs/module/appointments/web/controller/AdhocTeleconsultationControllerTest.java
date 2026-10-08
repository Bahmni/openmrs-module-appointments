package org.openmrs.module.appointments.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.service.impl.TeleconsultationAppointmentService;
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;

public class AdhocTeleconsultationControllerTest {

    @Mock
    private TeleconsultationAppointmentService teleconsultationAppointmentService;

    @InjectMocks
    private AdhocTeleconsultationController adhocTeleconsultationController;

    @BeforeEach
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldGenerateAdhocTeleconsultationLink() throws Exception {
        String patientUuid = "patientUuid";
        String provider = "";
        ResponseEntity<AdhocTeleconsultationResponse> adhocTeleconsultationResponse =
                adhocTeleconsultationController.generateAdhocTeleconsultationLink(patientUuid, provider);
        verify(teleconsultationAppointmentService,
                times(1)).generateAdhocTeleconsultationLink(eq(patientUuid), eq(provider));
    }
}
