package org.openmrs.module.appointments.service.impl;

import org.bahmni.module.email.notification.EmailNotificationException;
import org.bahmni.module.email.notification.service.EmailNotificationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class TeleconsultationAppointmentNotificationServiceImplTest {

    private TeleconsultationAppointmentNotificationService teleconsultationAppointmentNotificationService;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        teleconsultationAppointmentNotificationService =
                new TeleconsultationAppointmentNotificationServiceImpl(emailNotificationService);
    }

    @Test
    public void shouldSendTeleconsultationAppointmentLinkEmail() throws Exception {
        Appointment appointment = buildAppointment();
        String link = "https://meet.jit.si/" + appointment.getUuid();
        when(messageSourceService.getMessage("teleconsultation.appointment.email.subject", null, null)).thenReturn("Email subject");
        when(messageSourceService.getMessage("teleconsultation.appointment.email.body", new Object[]{ link }, null)).thenReturn("Link");
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        teleconsultationAppointmentNotificationService.sendTeleconsultationAppointmentLinkEmail(appointment);
        verify(emailNotificationService).send(
                eq("Email subject"),
                eq("Link"),
                AdditionalMatchers.aryEq(new String[]{ "someemail@gmail.com" }),
                any(),
                any());
    }

    @Test
    public void shouldThrowExceptionIfSendingFails() throws EmailNotificationException {
        Appointment appointment = buildAppointment();
        String link = "https://meet.jit.si/" + appointment.getUuid();
        when(messageSourceService.getMessage("teleconsultation.appointment.email.subject", null, null)).thenReturn("Email subject");
        when(messageSourceService.getMessage("teleconsultation.appointment.email.body", new Object[]{ link }, null)).thenReturn("Link");
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        doThrow(new EmailNotificationException("Some error", new Exception())).when(emailNotificationService).send(any(), any(), any(), any(), any());
        expectedException.expect(EmailNotificationException.class);

        teleconsultationAppointmentNotificationService.sendTeleconsultationAppointmentLinkEmail(appointment);
    }

    private Appointment buildAppointment() {
        Appointment appointment = new Appointment();
        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("email");
        patient.addAttribute(new PersonAttribute(personAttributeType, "someemail@gmail.com"));
        appointment.setPatient(patient);
        return appointment;
    }

}