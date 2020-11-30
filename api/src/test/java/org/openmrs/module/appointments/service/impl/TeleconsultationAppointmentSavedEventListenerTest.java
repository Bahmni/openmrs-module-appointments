package org.openmrs.module.appointments.service.impl;

import org.bahmni.module.email.notification.EmailNotificationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.module.appointments.event.TeleconsultationAppointmentSavedEvent;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
public class TeleconsultationAppointmentSavedEventListenerTest {

    @Mock
    private TeleconsultationAppointmentNotificationService teleconsultationAppointmentNotificationService;

    private ConfigurableApplicationContext ctx;

    private TeleconsultationAppointmentSavedEventListener listener;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        ctx = new AnnotationConfigApplicationContext();
        listener =  new TeleconsultationAppointmentSavedEventListener(teleconsultationAppointmentNotificationService);
        ctx.addApplicationListener(listener);
        ctx.refresh();
    }

    @Test
    public void shouldSendEmailOnTeleconsultationAppointmentSavedEvent() throws EmailNotificationException {
        Appointment appointment = new Appointment();
        ctx.publishEvent(new TeleconsultationAppointmentSavedEvent(appointment));
        verify(teleconsultationAppointmentNotificationService, times(1))
                .sendTeleconsultationAppointmentLinkEmail(appointment);
    }
}