package org.openmrs.module.appointments.service.impl;

import org.bahmni.module.teleconsultation.api.TeleconsultationService;
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
import org.openmrs.module.appointments.model.AdhocTeleconsultationResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.AfterEach;

public class TeleconsultationAppointmentServiceTest {
    private MockedStatic<Context> contextMockedStatic;

    @AfterEach
    public void closeStaticMocks() {
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
    }

    public static final String PATIENT_IDENTIFIER = "GAN230901";
    @Mock
    private AdministrationService administrationService;

    @Mock
    private TeleconsultationService teleconsultationService;

    @Mock
    private PatientService patientService;

    @Mock
    private PatientAppointmentNotifierService patientAppointmentNotifierService;

    @InjectMocks
    private TeleconsultationAppointmentService teleconsultationAppointmentService;

    private Patient patient;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        patient = new Patient();
        patient.setUuid("patientUuid");
        PersonName name = new PersonName();
        name.setGivenName("test patient");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(name);
        patient.setNames(personNames);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier(PATIENT_IDENTIFIER);
        PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
        patientIdentifierType.setName("Patient Identifier");
        identifier.setIdentifierType(patientIdentifierType);
        patient.setIdentifiers(new HashSet<>(Arrays.asList(identifier)));
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getService(TeleconsultationService.class)).thenReturn(teleconsultationService);
        when(administrationService.getGlobalProperty("bahmni.appointment.teleConsultation.serverUrlPattern")).thenReturn("https://test.server/{0}");
        when(administrationService.getGlobalProperty("bahmni.adhoc.teleConsultation.id")).thenReturn("Patient Identifier");
        when(teleconsultationService.generateTeleconsultationLink(PATIENT_IDENTIFIER)).thenReturn("https://test.server/GAN230901");
    }

    @Test
    public void shouldGetTCLink() {
        String link = teleconsultationAppointmentService.generateTeleconsultationLink(PATIENT_IDENTIFIER);
        assertEquals("https://test.server/GAN230901", link);
    }

    @Test
    public void shouldPublishAdhocTeleconsultationEvent() {
        AdhocTeleconsultationResponse response = teleconsultationAppointmentService.generateAdhocTeleconsultationLink(patient.getUuid(), "");
        assertEquals("https://test.server/GAN230901", response.getLink());
        verify(patientAppointmentNotifierService, times(1)).notifyAll(patient, "", response.getLink());
    }
}
