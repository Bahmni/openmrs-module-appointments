package org.openmrs.module.appointments.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TeleconsultationAppointmentServiceTest {
    @Mock
    private AdministrationService administrationService;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws Exception {
        contextMockedStatic = mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("bahmni.appointment.teleConsultation.serverUrlPattern")).thenReturn("https://test.server/{0}");
    }

    @After
    public void tearDown() {
        if (contextMockedStatic != null) contextMockedStatic.close();
    }

    @Test
    public void shouldGenerateTCLinkForAppointment() {
        TeleconsultationAppointmentService teleconsultationAppointmentService = new TeleconsultationAppointmentService();
        Appointment appointment = new Appointment();
        UUID uuid = UUID.randomUUID();
        appointment.setUuid(uuid.toString());
        String link = teleconsultationAppointmentService.generateTeleconsultationLink(appointment);
        assertEquals("https://test.server/"+appointment.getUuid(), link);

    }
}
