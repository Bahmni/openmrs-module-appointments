package org.openmrs.module.appointments.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.impl.DefaultTCAppointmentPatientEmailNotifier;
import org.openmrs.module.appointments.model.Appointment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultTeleconsultationAppointmentPatientEmailNotifierTest {

    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT = "bahmni.appointment.teleConsultation.patientEmailNotificationSubject";
    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE = "bahmni.appointment.teleConsultation.patientEmailNotificationTemplate";
    private AppointmentEventNotifier tcAppointmentEventNotifier;

    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private MailSender mailSender;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private MockedStatic<Context> contextMockedStatic;
    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
        contextMockedStatic = mockStatic(Context.class);
        tcAppointmentEventNotifier = new DefaultTCAppointmentPatientEmailNotifier(mailSender);
    }

    @After
    public void tearDown() throws Exception {
        if (contextMockedStatic != null) contextMockedStatic.close();
        if (mocks != null) mocks.close();
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