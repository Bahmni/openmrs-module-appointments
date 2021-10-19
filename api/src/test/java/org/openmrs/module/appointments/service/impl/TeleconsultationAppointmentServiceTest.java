package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class TeleconsultationAppointmentServiceTest {
    @Mock
    private AdministrationService administrationService;

    @Mock
    private PatientService patientService;

    @Mock
    private PatientAppointmentNotifierService patientAppointmentNotifierService;

    @InjectMocks
    private TeleconsultationAppointmentService teleconsultationAppointmentService;

    private Patient patient;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        patient = new Patient();
        patient.setUuid("patientUuid");
        PersonName name = new PersonName();
        name.setGivenName("test patient");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(name);
        patient.setNames(personNames);
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifier("GAN230901");
        patient.setIdentifiers(new HashSet<>(Arrays.asList(identifier)));
        when(patientService.getPatientByUuid("patientUuid")).thenReturn(patient);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("bahmni.appointment.teleConsultation.serverUrlPattern")).thenReturn("https://test.server/{0}");
    }

    @Test
    public void shouldGenerateTCLinkForAppointment() {
        Appointment appointment = new Appointment();
        UUID uuid = UUID.randomUUID();
        appointment.setUuid(uuid.toString());
        String link = teleconsultationAppointmentService.generateTeleconsultationLink(appointment);
        assertEquals("https://test.server/"+appointment.getUuid(), link);

    }

    @Test
    public void shouldGetAdhocTCLink() {
        String uuid = UUID.randomUUID().toString();
        String link = teleconsultationAppointmentService.generateAdhocTeleconsultationLink(uuid, "", "");
        assertEquals("https://test.server/"+uuid, link);
    }

    @Test
    public void shouldPublishAdhocTeleconsultationEvent() {
        String uuid = UUID.randomUUID().toString();
        String link = teleconsultationAppointmentService.generateAdhocTeleconsultationLink(uuid, patient.getUuid(), "");
        assertEquals("https://test.server/"+uuid, link);
        verify(patientAppointmentNotifierService, times(1)).notifyAll(patient, "", link);
    }
}
