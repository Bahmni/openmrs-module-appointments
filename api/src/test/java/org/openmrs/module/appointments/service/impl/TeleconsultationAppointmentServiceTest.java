package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class TeleconsultationAppointmentServiceTest {
    @Mock
    private AdministrationService administrationService;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("bahmni.appointment.teleConsultation.serverUrlPattern")).thenReturn("https://test.server/{0}");
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
