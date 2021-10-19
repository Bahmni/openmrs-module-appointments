package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class PatientAppointmentNotifierServiceTest {

    private PatientAppointmentNotifierService notifierService;

    @Mock
    private AppointmentEventNotifier appointmentEventNotifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        notifierService =  new PatientAppointmentNotifierService(Collections.singletonList(appointmentEventNotifier));
    }

    @Test
    public void shouldSendEmailOnTeleconsultationAppointmentSavedEvent() throws NotificationException {
        Appointment appointment = new Appointment();
        when(appointmentEventNotifier.isApplicable(appointment)).thenReturn(true);
        when(appointmentEventNotifier.sendNotification(appointment)).thenReturn(new NotificationResult("", "EMAIL", 0, "Some message"));
        notifierService.notifyAll(appointment);
        verify(appointmentEventNotifier, times(1)).sendNotification(appointment);
    }

    @Test
    public void shouldSendEmailOnAdhocTeleconsultationEvent() throws NotificationException {
        Patient patient = new Patient();
        String provider = "";
        String link = "";
        when(appointmentEventNotifier.sendNotification(patient, provider, link))
                .thenReturn(new NotificationResult("", "EMAIL", 0,
                        "Some message"));
        notifierService.notifyAll(patient, provider, link);
        verify(appointmentEventNotifier, times(1)).sendNotification(patient, provider, link);
    }
}
