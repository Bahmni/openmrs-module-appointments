package org.openmrs.module.appointments.service.impl;

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
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.notification.AppointmentEventNotifier;
import org.openmrs.module.appointments.notification.MailSender;
import org.openmrs.module.appointments.notification.NotificationException;
import org.openmrs.module.appointments.notification.impl.DefaultTCAppointmentPatientEmailNotifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DefaultTeleconsultationAppointmentPatientEmailNotifierTest {

    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT = "bahmni.appointment.teleConsultation.patientEmailNotificationSubject";
    private static final String BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE = "bahmni.appointment.teleConsultation.patientEmailNotificationTemplate";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationSubject";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE = "bahmni.appointment.adhocTeleConsultation.patientEmailNotificationTemplate";
    private static final String BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_CC_EMAILS = "bahmni.appointment.adhocTeleConsultation.ccEmails";
    private AppointmentEventNotifier tcAppointmentEventNotifier;

    @Mock
    private MailSender mailSender;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AdministrationService administrationService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockStatic(Context.class);
        tcAppointmentEventNotifier = new DefaultTCAppointmentPatientEmailNotifier(mailSender);
        PowerMockito.when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT)).thenReturn("Email subject");
        when(administrationService.getGlobalProperty(BAHMNI_APPOINTMENT_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE)).thenReturn("Email body");
        when(administrationService.getGlobalProperty(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_SUBJECT)).thenReturn("Email subject");
        when(administrationService.getGlobalProperty(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_TEMPLATE)).thenReturn("Email body");
        when(administrationService.getGlobalProperty(BAHMNI_ADHOC_TELE_CONSULTATION_EMAIL_NOTIFICATION_CC_EMAILS)).thenReturn("someemail1@gmail.com,someemail2@gmail.com");
    }

    @Test
    public void shouldSendTeleconsultationAppointmentLinkEmail() throws Exception {
        Appointment appointment = buildAppointment();
        tcAppointmentEventNotifier.sendNotification(appointment);
        verify(mailSender).send(
                eq("Email subject"),
                eq("Email body"),
                AdditionalMatchers.aryEq(new String[]{ "someemail@gmail.com" }),
                any(),
                any());
    }

    @Test
    public void shouldThrowExceptionIfSendingFails() throws NotificationException {
        Appointment appointment = buildAppointment();
        doThrow(new RuntimeException()).when(mailSender).send(any(), any(), any(), any(), any());
        expectedException.expect(NotificationException.class);
        tcAppointmentEventNotifier.sendNotification(appointment);
    }

    @Test
    public void shouldSendAdhocTeleconsultationLinkEmail() throws Exception {
        Patient patient = buildPatient();
        String link = "https://meet.jit.si/" + UUID.randomUUID().toString();
        tcAppointmentEventNotifier.sendNotification(patient, "provider", link);
        verify(mailSender).send(
                eq("Email subject"),
                eq("Email body"),
                AdditionalMatchers.aryEq(new String[]{ "someemail@gmail.com" }),
                AdditionalMatchers.aryEq(new String[]{ "someemail1@gmail.com", "someemail2@gmail.com" }),
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
