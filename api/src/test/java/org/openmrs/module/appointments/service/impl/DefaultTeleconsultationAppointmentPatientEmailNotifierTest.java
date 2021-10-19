package org.openmrs.module.appointments.service.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.impl.DefaultTCAppointmentPatientEmailNotifier;
import org.openmrs.module.appointments.model.Appointment;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DefaultTeleconsultationAppointmentPatientEmailNotifierTest {

    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT = "bahmni.appointment.teleConsultation.patientEmailNotificationSubject";
    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE = "bahmni.appointment.teleConsultation.patientEmailNotificationTemplate";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationTemplate";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationTemplate";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_CC_EMAILS = "bahmni.appointment.adhocTeleConsultation.ccEmails";
    private AppointmentEventNotifier tcAppointmentEventNotifier;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private MailSender mailSender;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        tcAppointmentEventNotifier = new DefaultTCAppointmentPatientEmailNotifier(mailSender);
    }

    @Ignore
    @Test
    public void shouldSendTeleconsultationAppointmentLinkEmail() throws Exception {
        Appointment appointment = buildAppointment();
        when(messageSourceService.getMessage(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT, null, null)).thenReturn("Email subject");
        when(messageSourceService.getMessage(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE, null, null)).thenReturn("Email body");
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        tcAppointmentEventNotifier.sendNotification(appointment);
        verify(mailSender).send(
                eq("Email subject"),
                eq("Email body"),
                AdditionalMatchers.aryEq(new String[]{ "someemail@gmail.com" }),
                any(),
                any());
    }

    @Ignore
    @Test
    public void shouldThrowExceptionIfSendingFails() throws NotificationException {
        Appointment appointment = buildAppointment();
        String link = "https://meet.jit.si/" + appointment.getUuid();
        when(messageSourceService.getMessage(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT, null, null)).thenReturn("Email subject");
        when(messageSourceService.getMessage(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE, new Object[]{ link }, null)).thenReturn("Link");
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        doThrow(new NotificationException("Some error", new Exception())).when(mailSender).send(any(), any(), any(), any(), any());
        expectedException.expect(NotificationException.class);
        tcAppointmentEventNotifier.sendNotification(appointment);
    }

    @Ignore
    @Test
    public void shouldSendAdhocTeleconsultationLinkEmail() throws Exception {
        Patient patient = buildPatient();
        String link = "https://meet.jit.si/" + UUID.randomUUID().toString();
        when(messageSourceService.getMessage(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT, null, null)).thenReturn("Email subject");
        when(messageSourceService.getMessage(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE, null, null)).thenReturn("Email body");
        when(messageSourceService.getMessage(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_CC_EMAILS, null, null)).thenReturn("someemail@gmail.com,someemail2@gmail.com");
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
        tcAppointmentEventNotifier.sendNotification(patient, "provider", link);
        verify(mailSender).send(
                eq("Email subject"),
                eq("Email body"),
                AdditionalMatchers.aryEq(new String[]{ "someemail@gmail.com" }),
                any(),
                any());
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

    private Patient buildPatient() {
        Patient patient = new Patient();
        patient.setUuid("patientUuid");
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("email");
        patient.addAttribute(new PersonAttribute(personAttributeType, "someemail@gmail.com"));
        return patient;
    }

}
