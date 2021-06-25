package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.NotificationResult;
import org.openmrs.module.appointments.event.TeleconsultationAppointmentSavedEvent;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class TeleconsultationAppointmentSavedEventListenerTest {

    private ConfigurableApplicationContext ctx;

    private TeleconsultationAppointmentSavedEventListener listener;

    @Mock
    private AppointmentEventNotifier appointmentEventNotifier;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        ctx = new AnnotationConfigApplicationContext();
        listener =  new TeleconsultationAppointmentSavedEventListener(Collections.singletonList(appointmentEventNotifier));
        ctx.addApplicationListener(listener);
        ctx.refresh();
    }

    @Test
    public void shouldSendEmailOnTeleconsultationAppointmentSavedEvent() throws NotificationException {
        Appointment appointment = new Appointment();
        when(appointmentEventNotifier.sendNotification(appointment)).thenReturn(new NotificationResult("", 0, "Some message"));
        ctx.publishEvent(new TeleconsultationAppointmentSavedEvent(appointment));
        verify(appointmentEventNotifier, times(1)).sendNotification(appointment);
    }
}